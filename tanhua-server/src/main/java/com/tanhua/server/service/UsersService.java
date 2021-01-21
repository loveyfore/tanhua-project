package com.tanhua.server.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.server.api.RecommendUserApi;
import com.tanhua.server.api.UserLikeApi;
import com.tanhua.server.api.VisitorsApi;
import com.tanhua.server.config.AliyunConfig;
import com.tanhua.server.pojo.*;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.*;
import com.tanhua.sso.enums.SexEnum;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/16 10:20
 */
@Service
public class UsersService {

    @Autowired
    private UserService userService;

    @Autowired
    private AliyunConfig aliyunConfig;

    @Reference(version = "1.0.0")
    private UserLikeApi userLikeApi;

    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private BlackListService blackListService;

    /**
     * 查询用户详细信息
     * 通用方法,既可以通过传参的形式查询,也可以使用环信的id查询,如果没有参数,那么使用当前用户查询
     * @param userId 用户id
     * @param huanXinId 用户环信id--环信用户的id也是用户的id
     * @return
     */
    public UserInfoVo queryUserInfo(Long userId, Long huanXinId) {

        User user = UserThreadLocal.get();

        /*默认查询当前登录用户*/
        Long uid=user.getId();


        /*校验参数,如果都为空,查询当前用户*/
        if (userId!=null){
            uid=userId;
        }else if (huanXinId!=null){
            uid=huanXinId;
        }

        UserInfo userInfo = userService.queryUserInfoByUserId(uid);
        if (userInfo==null){
            return null;
        }

        /*数据封装*/
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setAge(userInfo.getAge() != null ? userInfo.getAge().toString() : null);
        userInfoVo.fillAvatar(aliyunConfig.getUrlPrefix(),userInfo.getLogo());
        userInfoVo.setBirthday(userInfo.getBirthday());
        userInfoVo.setEducation(userInfo.getEdu());
        userInfoVo.setCity(userInfo.getCity());
        userInfoVo.setGender(userInfo.getSex().name().toLowerCase());  /*枚举名转小写*/
        userInfoVo.setId(userInfo.getUserId());
        userInfoVo.setIncome(userInfo.getIncome() + "K");  /*薪资拼接 K*/
        userInfoVo.setMarriage(StringUtils.equals(userInfo.getMarriage(), "已婚") ? 1 : 0);
        userInfoVo.setNickname(userInfo.getNickName());
        userInfoVo.setProfession(userInfo.getIndustry());

        /*返回对象*/
        return userInfoVo;
    }


    /**
     * 保存用户资料
     * @param userInfoVo
     * @return
     */
    public Boolean updateUserInfo(UserInfoVo userInfoVo) {

        User user = UserThreadLocal.get();

        /*封装UserInfo对象*/
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setAge(Integer.valueOf(userInfoVo.getAge()));
        userInfo.setSex(StringUtils.equalsIgnoreCase(userInfoVo.getGender(), "man") ? SexEnum.MAN : SexEnum.WOMAN);/*字符串转枚举*/
        userInfo.setBirthday(userInfoVo.getBirthday());
        userInfo.setCity(userInfoVo.getCity());
        userInfo.setEdu(userInfoVo.getEducation());
        //替换 将K替换为空字符串
        userInfo.setIncome(StringUtils.replace(userInfoVo.getIncome(), "K", ""));
        userInfo.setIndustry(userInfoVo.getProfession());
        userInfo.setMarriage(userInfoVo.getMarriage() == 1 ? "已婚" : "未婚");


        return this.userService.updateUserInfo(userInfo);


    }

    /**
     * 获取用户 互相喜欢，喜欢，粉丝 - 统计
     * @return
     */
    public CountsVo queryCounts() {
        /**
         * 获取当前用户
         * 查询数据
         * 封装
         */

        User user = UserThreadLocal.get();
        Long uid = user.getId();

        /*数据初始化*/
        CountsVo countsVo = new CountsVo();
        countsVo.setEachLoveCount(userLikeApi.queryEachLikeCount(uid));
        countsVo.setFanCount(userLikeApi.queryFanCount(uid));
        countsVo.setLoveCount(userLikeApi.queryLikeCount(uid));

        return countsVo;
    }

    /**
     * 互相喜欢、喜欢、粉丝、谁看过我 - 翻页列表
     * type
     *      * 1 互相关注
     *      * 2 我关注
     *      * 3 粉丝
     *      * 4 谁看过我
     * @param type
     * @param pageNum
     * @param pageSize
     * @param nickname
     * @return
     */
    public PageResult queryList(Integer type, Integer pageNum, Integer pageSize, String nickname) {

        User user = UserThreadLocal.get();
        Long uid = user.getId();

        PageResult pageResult = new PageResult();
        pageResult.setPage(pageNum);
        pageResult.setCounts(0);/*总记录数暂不提供*/
        pageResult.setPages(0);/*总页数暂不提供*/
        pageResult.setPagesize(pageSize);

        /*这里是需要获取的是用户id,用id去查询详细信息,定义集合来存放id*/
        List<Long> userIds = new ArrayList<>();
        switch (type){
            case 1:/*互相关注*/
                for (UserLike userLike : userLikeApi.queryEachLike(uid, pageNum, pageSize)) {
                    userIds.add(userLike.getUserId());
                }
                break;
            case 2:/*我关注*/
                for (UserLike userLike : userLikeApi.queryLike(uid, pageNum, pageSize)) {
                    userIds.add(userLike.getLikeUserId());
                }
                break;
            case 3:/*粉丝*/
                for (UserLike userLike : userLikeApi.queryFan(uid, pageNum, pageSize)) {
                    userIds.add(userLike.getUserId());
                }
                break;
            case 4:/*谁看过我*/
                for (Visitors visitors : visitorsApi.visitorList(uid, pageNum, pageSize)) {
                    userIds.add(visitors.getVisitorUserId());
                }

                /*
                这里需要使用redis记录用户这次访问访客列表的时间,下次访问的时候直接从这次记录的时间开始查看访客,这样就不必再查看之前浏览过的访客
                查询一次,就要更新一次时间
                */
                redisTemplate.opsForValue().set("VISITORS_TIME_" + uid,String.valueOf(System.currentTimeMillis()));

                break;
            default:
                break;
        }

        if (CollectionUtils.isEmpty(userIds)){
            /*返回空数据*/
            return pageResult;
        }

        /*根据id集合,查询userInfo用户详细信息,和模糊条件查询*/
        List<UserInfo> userInfoList = this.userService.queryUserInfoLikeNickName(userIds, nickname);

        /*数据封装*/
        List<UserLikeListVo> userLikeListVoList =new ArrayList<>();

        for (UserInfo userInfo : userInfoList) {
            UserLikeListVo userLikeListVo = new UserLikeListVo();
            //userInfo的信息封装
            userLikeListVo.setAge(userInfo.getAge());
            userLikeListVo.setAvatar(userInfo.getLogo());
            userLikeListVo.setCity(userInfo.getCity());
            userLikeListVo.setEducation(userInfo.getEdu());
            userLikeListVo.setGender(userInfo.getSex().name().toLowerCase());
            userLikeListVo.setId(userInfo.getUserId());
            userLikeListVo.setMarriage(StringUtils.equals(userInfo.getMarriage(), "已婚") ? 1 : 0);
            userLikeListVo.setNickname(userInfo.getNickName());

            /*查询缘分值*/
            RecommendUser recommendUser = recommendUserApi.querySocreByUserId(uid,userInfo.getUserId());
            if (recommendUser!=null){
                userLikeListVo.setMatchRate(recommendUser.getScore().intValue());
            }else {
                /*生成随机缘分值*/
                userLikeListVo.setMatchRate(RandomUtils.nextInt(75,96));
            }

            /*查询粉丝是否已经被当前用户喜欢*/
            userLikeListVo.setAlreadyLove(userLikeApi.isLike(uid,userInfo.getUserId()));


            userLikeListVoList.add(userLikeListVo);
        }

        pageResult.setItems(userLikeListVoList);
        return pageResult;
    }

    /**
     * 取消喜欢--取消关注
     * @param userId
     * @return
     */
    public Boolean disLike(Long userId) {

        User user = UserThreadLocal.get();
        Long uid = user.getId();

        /*删除当前用户对该用户的喜欢记录*/
        return userLikeApi.deleteUserLike(uid,userId);
    }

    /**
     * 粉丝喜欢--关注粉丝
     * @param userId
     * @return
     */
    public Boolean fanLike(Long userId) {
        User user = UserThreadLocal.get();
        Long uid = user.getId();

        /*记录关注,喜欢*/
        return userLikeApi.saveUserLike(uid,userId);
    }

    /**
     * 通用设置-通知设置保存
     * @param likeNotification 喜欢通知
     * @param pinglunNotification 评论通知
     * @param gonggaoNotification 公告通知
     * @return
     */
    public Boolean updateNotificationSettings(Boolean likeNotification, Boolean pinglunNotification, Boolean gonggaoNotification) {
        /**
         * 获取用户信息
         * 判断是否已经有记录
         * 有-更新,无-新增
         */

        User user = UserThreadLocal.get();
        Long uid = user.getId();

        Settings settings = settingsService.querySettings(uid);
        if (settings==null){
            /*新增*/
            settings = new Settings();
            settings.setGonggaoNotification(gonggaoNotification);
            settings.setLikeNotification(likeNotification);
            settings.setPinglunNotification(pinglunNotification);
            settings.setUserId(user.getId());
            return settingsService.saveSettings(settings);

        }
            /*更新*/
            settings.setGonggaoNotification(gonggaoNotification);
            settings.setLikeNotification(likeNotification);
            settings.setPinglunNotification(pinglunNotification);
            return settingsService.updateSettings(settings);

    }

    /**
     * 读取用户通用设置数据
     * @return
     */
    public SettingsVo queryUserSettings() {
        /**
         * 获取用户信息
         * 查询数据
         * 封装数据
         * 返回
         */

        User user = UserThreadLocal.get();
        Long uid = user.getId();

        SettingsVo settingsVo = new SettingsVo();
        settingsVo.setId(uid);
        settingsVo.setPhone(user.getMobile());

        /*查询问题*/
        Question question = questionService.queryQuestion(uid);
        if (question!=null){
            settingsVo.setStrangerQuestion(question.getTxt());
        }

        /*查询用户通知设置*/
        Settings settings = settingsService.querySettings(uid);
        if (settings!=null){
            settingsVo.setLikeNotification(settings.getLikeNotification());
            settingsVo.setPinglunNotification(settings.getPinglunNotification());
            settingsVo.setGonggaoNotification(settings.getGonggaoNotification());
        }

        return settingsVo;



    }

    /**
     * 用户个人资料-陌生人问题保存更新
     * @return
     */
    public Boolean updateQuestion(String content) {

        /**
         * 获取用户信息
         * 判断是否已经有记录
         * 有-更新,无-新增
         */

        User user = UserThreadLocal.get();
        Long uid = user.getId();

        Question question = questionService.queryQuestion(uid);
        if (question==null){
            /*新增*/
            question = new Question();
            /*id配置了自增不需要设置,创建时间和跟新时间也配置了自动填充无需设置*/
            question.setTxt(content);
            question.setUserId(uid);
            return questionService.saveQuestions(question);

        }
        /*更新 update更新时间无需填充已经配置自动填充*/
        question.setTxt(content);
        return questionService.updateQuestion(question);

    }

    /**
     * 查询黑名单列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult queryBlackList(Integer pageNum, Integer pageSize) {

        User user = UserThreadLocal.get();
        Long uid = user.getId();

        PageResult pageResult = new PageResult();



        IPage<BlackList> blackListIPage = blackListService.queryBlackListByUserId(uid, pageNum, pageSize);
        List<BlackList> records = blackListIPage.getRecords();
        pageResult.setCounts((int)blackListIPage.getTotal());
        pageResult.setPage(pageNum);
        pageResult.setPages((int)blackListIPage.getPages());
        pageResult.setPagesize(pageSize);


        if (CollectionUtils.isEmpty(records)){
            return pageResult;
        }

        /*获取黑名单用户的详细信息*/
        List<Long> userIds=new ArrayList<>();
        for (BlackList record : records) {
            userIds.add(record.getBlackUserId());
        }

        List<UserInfo> userInfoList = userService.queryUserInfoByUserIdList(userIds);

        /*数据封装*/
        List<BlackListVo> blackListVoList=new ArrayList<>();

        for (UserInfo userInfo : userInfoList) {

            BlackListVo blackListVo = new BlackListVo();
            blackListVo.setAge(userInfo.getAge());
            blackListVo.setAvatar(userInfo.getLogo());
            blackListVo.setGender(userInfo.getSex().name().toLowerCase());
            blackListVo.setId(userInfo.getUserId());
            blackListVo.setNickname(userInfo.getNickName());

            blackListVoList.add(blackListVo);

        }

        pageResult.setItems(blackListVoList);

        return pageResult;


    }

    /**
     * 删除黑名单用户
     * @param blackListUserId
     * @return
     */
    public Boolean disBlack(Long blackListUserId) {

        User user = UserThreadLocal.get();
        Long uid = user.getId();

        return blackListService.deleteBlackListUser(uid, blackListUserId);
    }
}
