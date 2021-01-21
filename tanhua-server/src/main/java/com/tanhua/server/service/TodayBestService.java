package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.extension.parsers.ITableNameHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.server.api.UserLikeApi;
import com.tanhua.server.api.UserLocationApi;
import com.tanhua.server.pojo.Question;
import com.tanhua.server.pojo.RecommendUser;
import com.tanhua.server.utils.CacheUtils;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.*;
import com.tanhua.server.vo.params.RecommendUserQueryParam;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import sun.plugin2.message.Message;

import java.math.BigDecimal;
import java.util.*;

/**
 * @Author Administrator
 * @create 2021/1/2 21:45
 */
@Service
@Log4j2
public class TodayBestService {

    @Autowired
    private UserService userService;

    @Autowired
    private RecommendUserService recommendUserService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /*默认显示的佳人*/
    @Value("${tanhua.sso.default.user}")
    private Long defaultUserId;

    /*缓存工具类*/
    @Autowired
    private CacheUtils cacheUtils;

    @Value("${tanhua.sso.default.recommend.users}")
    private String defaultRecommendUser;


    @Autowired
    private QuestionService questionService;

    //版本 1.0.1 代表 es实现 1.0.0代表 mongo实现
    @Reference(version = "1.0.0")
    private UserLocationApi userLocationApi;

    @Reference(version = "1.0.0")
    private UserLikeApi userLikeApi;

    @Autowired
    private IMService imService;

    /**
     * 今日佳人
     *
     * @return
     */
    public TodayBest todayBest() {
        //每个service 都有自己的职责范围
        /**
         * 1.token 去sso系统 token认证
         * 2.User  登录的userId，调用dubbo服务，得到RecommendUser 推荐用户
         * 3.推荐的用户id，推荐用户id和当前用户的缘分值（分数score）
         * 4.去sso系统的userInfo表中 查询用户的详细信息
         * 5.组装返回数据 TodayBest
         *
         */

        //1
        User user = UserThreadLocal.get();
//        if (user==null){
//            return null;
//        }

        //2,3
        RecommendUser recommendUser = recommendUserService.queryMaxScore(user.getId());
        /*查不到生成一个*/
        if (recommendUser == null) {
            recommendUser = new RecommendUser();
            recommendUser.setUserId(defaultUserId);
            recommendUser.setScore(98d);
        }

        //4
        /*获取今日佳人的用户的id--今日佳人也是一个用户*/
        Long userId = recommendUser.getUserId();

        /*查询佳人详细信息*/
        UserInfo userInfo = userService.queryUserInfoByUserId(userId);
        if (userInfo == null) {
            return null;
        }

        //5--佳人数据返回
        TodayBest todayBest = new TodayBest();
        todayBest.setId(userInfo.getUserId().intValue());   //TODO 这里修改了bug   这句返回的为当前登录用户的id,造成数据错误,todayBest.setId(user.getId().intValue());
        todayBest.setAvatar(userInfo.getLogo());
        todayBest.setAge(userInfo.getAge());
        /*对分数进行取整*/
        BigDecimal bigDecimal = new BigDecimal(recommendUser.getScore()).setScale(0, BigDecimal.ROUND_HALF_DOWN);
        /*缘分值*/
        todayBest.setFateValue(bigDecimal.longValue());
        todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
        todayBest.setNickname(userInfo.getNickName());
        //单身,本科,年龄相仿
        todayBest.setTags(StringUtils.split(userInfo.getTags(), ","));
        return todayBest;

    }


    /**
     * 推荐列表
     *
     * @param queryParam
     * @return
     */
    public PageResult recommendation(RecommendUserQueryParam queryParam) {


        /**
         * 缓存优化
         * 1.缓存中获取数据,得到直接返回
         * 2.没有就执行逻辑业务,在将查询结果存储到缓存中--缓存设置过期时间10分钟
         */

//        try {
//            String key="TodayBestService_recommendation_"+token+OBJECT_MAPPER.writeValueAsString(queryParam);
//            PageResult cache = cacheUtils.getCache(key, PageResult.class);
//            if (cache!=null){
//                return cache;
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }


        /**
         * 1.token认证
         * 2.User 调用dubbo服务,得到List<RecommendUser>
         * 3.遍历RecommendUser列表
         * 4.根据List<userId>向用户表userInfo查询用户的详细信息
         * 5.封装PageResult  List<TodayBest>返回
         */
        log.info("QueryParam:{}", queryParam);
        //1
        User user = UserThreadLocal.get();
//        if (user==null){
//            return null;
//        }

        //2
        Long userId = user.getId();
        Integer pageNum = queryParam.getPage();
        Integer pagesSize = queryParam.getPagesize();

        PageResult pageResult = new PageResult();
        pageResult.setPages(0);
        pageResult.setCounts(0);
        pageResult.setPage(pageNum);
        pageResult.setPagesize(pagesSize);
        pageResult.setItems(Collections.emptyList());

        PageInfo<RecommendUser> pageInfo = recommendUserService.queryPage(userId, pageNum, pagesSize);
        /*获取数据列表*/
        List<RecommendUser> records = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            log.info("recommendation查询为空执行默认配置推荐!");

            /*如果当前用户没有推荐用户,通过配置文件中的默认参数获取*/
            String[] userIdArr = StringUtils.split(defaultRecommendUser, ",");
            for (String id : userIdArr) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Long.valueOf(id));
                recommendUser.setToUserId(user.getId());
                recommendUser.setScore(RandomUtils.nextDouble(75, 96));

                records.add(recommendUser);
            }
        }

        //3
        ArrayList<Long> userIdList = new ArrayList<>();
        for (RecommendUser record : records) {
            /*取值添加*/
            userIdList.add(record.getUserId());

        }

        //4
        /*根据id集合,查询推荐列表中的用户详细数据*/
        List<UserInfo> userInfoList = userService.queryUserInfoByUserIdList(userIdList, queryParam.getAge(), queryParam.getCity(), queryParam.getEducation(), queryParam.getGender());

        Map<Long, UserInfo> userInfoMap = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            userInfoMap.put(userInfo.getUserId(), userInfo);
        }

        //5
        /*用来存放推荐用户详细信息的列表*/
        List<TodayBest> todayBestList = new ArrayList<>();
        for (RecommendUser record : records) {
            TodayBest todayBest = new TodayBest();
            /*根据推荐列表的用户id,从map拿到当前推荐列表用户的详细个人信息*/
            UserInfo userInfo = userInfoMap.get(record.getUserId());
            if (userInfo == null) {
                /*跳出本次循环*/
                continue;
            }

            todayBest.setId(userInfo.getUserId().intValue());  //TODO 这里修改了bug   这句返回的为当前登录用户的id,造成数据错误,todayBest.setId(user.getId().intValue());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setAge(userInfo.getAge());
            /*分数取整*/
            BigDecimal bigDecimal = new BigDecimal(record.getScore()).setScale(0, BigDecimal.ROUND_HALF_DOWN);
            todayBest.setFateValue(bigDecimal.longValue());
            todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
            todayBest.setNickname(userInfo.getNickName());
            //单身,本科,年龄相仿
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ","));

            //填入推荐列表
            todayBestList.add(todayBest);
        }

        pageResult.setItems(todayBestList);

//        /*结果放入缓存*/
//        try {
//            String key="TodayBestService_recommendation_"+token+OBJECT_MAPPER.writeValueAsString(queryParam);
//            /*使用缓存工具类,过期时间10分钟*/
//            cacheUtils.putCache(key,pageResult,Duration.ofMinutes(10));
//        } catch (Exception e){
//            e.printStackTrace();
//        }

        return pageResult;


    }

    /**
     * 点击后,查询今日佳人详细信息
     *
     * @param userId 被推荐用户id
     * @return
     */
    public TodayBest queryTodayBest(Long userId) {

        User user = UserThreadLocal.get();

        UserInfo userInfo = userService.queryUserInfoByUserId(userId);
        TodayBest todayBest = new TodayBest();
        todayBest.setAge(userInfo.getAge());
        todayBest.setAvatar(userInfo.getLogo());
        todayBest.setGender(userInfo.getSex().name().toLowerCase());
        todayBest.setId(userInfo.getUserId().intValue());
        todayBest.setNickname(userInfo.getNickName());
        todayBest.setTags(StringUtils.split(userInfo.getTags(), ","));

        /*查询分数*/
        RecommendUser recommendUser = recommendUserService.querySocreByUserId(user.getId(), userId);
        if (recommendUser != null) {
            todayBest.setFateValue(recommendUser.getScore().longValue());
        } else {
            /*生成随机缘分值*/
            todayBest.setFateValue(RandomUtils.nextLong(93, 99));
        }
        return todayBest;
    }

    /**
     * 查询陌生人问题,mysql
     *
     * @param userId
     * @return
     */
    public String queryQuestion(Long userId) {
        Question question = questionService.queryQuestion(userId);
        if (question != null) {
            return question.getTxt();
        }
        return null;
    }

    /**
     * 回答问题
     *
     * @param userId 目标id
     * @param reply  答案
     * @return
     */
    public Boolean replyQuestion(Long userId, String reply) {
        /**
         * 这里写的json是发送者的信息
         * {"userId": "1","nickname":"黑马小妹","strangerQuestion": "你喜欢去看蔚蓝的大海还是去爬巍峨的高山？"
         * ,"reply": "我喜欢秋天的落叶，夏天的泉水，冬天的雪地，只要有你一切皆可~"}
         */


        User user = UserThreadLocal.get();

        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("userId", String.valueOf(user.getId()));
        UserInfo userInfo = userService.queryUserInfoByUserId(user.getId());
        msgMap.put("nickname", userInfo.getNickName());
        Question question = questionService.queryQuestion(userId);
        msgMap.put("strangerQuestion", question.getTxt());
        msgMap.put("reply", reply);

        try {
            String msgMapJson = OBJECT_MAPPER.writeValueAsString(msgMap);
            Boolean flag = userService.replyQuestionMessage(userId, msgMapJson, "txt"); //	消息类型；txt:文本消息，img：图片消息，loc：位置消息，audio：语音消息，video：视频消息，file：文件消息
            return flag;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 搜附近
     *
     * @param gender   性别
     * @param distance 距离 单位 米
     * @return
     */
    public List<NearUserVo> queryNearUser(String gender, Integer distance) {
        /**
         * 查询当前用户的位置信息
         * 根据用户当前信息查询附近的人
         */

        User user = UserThreadLocal.get();

        UserLocationVo userLocationVo = userLocationApi.queryByUserId(user.getId());
        /*经度 纬度*/
        Double longitude = userLocationVo.getLongitude();
        Double latitude = userLocationVo.getLatitude();

        List<UserLocationVo> userLocationVoList = userLocationApi.queryUserFromLocation(longitude, latitude, distance);


        if (CollectionUtils.isEmpty(userLocationVoList)) {
            /*附近的人为空,返回空集合*/
            return Collections.emptyList();
        }

        /*获取userId查询用户详细信息*/
        List<Long> userIdList = new ArrayList<>();
        for (UserLocationVo locationVo : userLocationVoList) {
            /*过滤掉自己的信息,也就是当前用户的信息*/
            if (!user.getId().equals(locationVo.getUserId())) {
                userIdList.add(locationVo.getUserId());
            }
        }

        /*如果没有附近的好友,又过滤掉了自己的信息,userId集合就会为空*/
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyList();
        }

        List<UserInfo> userInfoList = userService.queryUserInfoByUserIdList(userIdList, gender);

        List<NearUserVo> nearVoList = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            NearUserVo nearVo = new NearUserVo();
            nearVo.setAvatar(userInfo.getLogo());
            nearVo.setNickname(userInfo.getNickName());
            nearVo.setUserId(userInfo.getUserId());

            nearVoList.add(nearVo);
        }

        return nearVoList;
    }

    /**
     * 探花-卡片展示列表
     *
     * @return
     */
    public List<TodayBest> queryCardsList() {
        /**
         * 查询推荐列表
         * 获取用户id
         * 查询用户详细信息
         * 封装数据
         */

        User user = UserThreadLocal.get();

        PageInfo<RecommendUser> pageInfo = recommendUserService.queryPage(user.getId(), 1, 50);

        List<RecommendUser> records = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            /*默认推荐数据*/
            /*如果当前用户没有推荐用户,通过配置文件中的默认参数获取*/
            String[] userIdArr = StringUtils.split(defaultRecommendUser, ",");
            for (String id : userIdArr) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Long.valueOf(id));
                recommendUser.setToUserId(user.getId());
                recommendUser.setScore(RandomUtils.nextDouble(75, 96));

                records.add(recommendUser);
            }
        }

        //3. 卡片数据有限，获取前10条就可以
        //int showCount = Math.min(10, records.size());

        List<Long> userIdList = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            /*生成随机索引*/
            int index = RandomUtils.nextInt(0, records.size() - 1);

            userIdList.add(records.get(index).getUserId());

            /*添加后删除,防止随机索引又获取到了重复数据*/
            records.remove(index);
        }

        List<UserInfo> userInfoList = userService.queryUserInfoByUserIdList(userIdList);


        /*数据封装*/
        List<TodayBest> todayBestList = new ArrayList<>();

        for (UserInfo userInfo : userInfoList) {

            TodayBest todayBest = new TodayBest();
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setFateValue(0L);/*缘分值页面无要求暂不设置*/
            todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man":"woman");
            todayBest.setId(userInfo.getUserId().intValue());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(),","));

            todayBestList.add(todayBest);
        }

        return todayBestList;
    }

    /**
     * 探花 - 喜欢
     * @param likeUserId
     */
    public Boolean likeUser(Long likeUserId) {
        /**
         * 获取当前用户信息
         * 判断双方是否喜欢,喜欢-加好友,没有记录正常添加
         */
        User user = UserThreadLocal.get();

        Boolean saveLikeUser = userLikeApi.saveUserLike(user.getId(), likeUserId);
        if(saveLikeUser){

            if (userLikeApi.isMutualLike(user.getId(), likeUserId)){
                /*环信添加好友,mongoDB建立好友关系*/
                return imService.addContacts(likeUserId);
            }
        }
        return saveLikeUser;
    }

    /**
     * 探花 -不喜欢
     * @param likeUserId
     */
    public Boolean disLikeUser(Long likeUserId) {
        User user = UserThreadLocal.get();
        return userLikeApi.deleteUserLike(user.getId(),likeUserId);
    }
}
