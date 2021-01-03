package com.tanhua.server.service;

import com.tanhua.server.pojo.RecommendUser;
import com.tanhua.server.vo.PageInfo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.TodayBest;
import com.tanhua.server.vo.params.RecommendUserQueryParam;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    /*默认显示的佳人*/
    @Value("${tanhua.sso.default.user}")
    private Long defaultUserId;


    /**
     * 今日佳人
     * @param token
     * @return
     */
    public TodayBest todayBest(String token){
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
        User user = userService.queryToken(token);
        if (user==null){
            return null;
        }

        //2,3
        RecommendUser recommendUser = recommendUserService.queryMaxScore(user.getId());
        /*查不到生成一个*/
        if (recommendUser == null){
            recommendUser = new RecommendUser();
            recommendUser.setUserId(defaultUserId);
            recommendUser.setScore(98d);
        }

        //4
        /*获取今日佳人的用户的id--今日佳人也是一个用户*/
        Long userId = recommendUser.getUserId();

        /*查询佳人详细信息*/
        UserInfo userInfo = userService.queryUserInfoByUserId(userId);
        if (userInfo==null){
            return null;
        }

        //5--佳人数据返回
        TodayBest todayBest = new TodayBest();
        todayBest.setId(user.getId().intValue());
        todayBest.setAvatar(userInfo.getLogo());
        todayBest.setAge(userInfo.getAge());
        /*对分数进行取整*/
        BigDecimal bigDecimal = new BigDecimal(recommendUser.getScore()).setScale(0,BigDecimal.ROUND_HALF_DOWN);
        /*缘分值*/
        todayBest.setFateValue(bigDecimal.longValue());
        todayBest.setGender(userInfo.getSex().getValue()==1?"man":"woman");
        todayBest.setNickname(userInfo.getNickName());
        //单身,本科,年龄相仿
        todayBest.setTags(StringUtils.split(userInfo.getTags(),","));
        return todayBest;

    }


    /**
     * 推荐列表
     * @param token
     * @param queryParam
     * @return
     */
    public PageResult recommendation(String token, RecommendUserQueryParam queryParam){
        /**
         * 1.token认证
         * 2.User 调用dubbo服务,得到List<RecommendUser>
         * 3.遍历RecommendUser列表
         * 4.根据List<userId>向用户表userInfo查询用户的详细信息
         * 5.封装PageResult  List<TodayBest>返回
         */
        log.info("QueryParam:{}",queryParam);
        //1
        User user = userService.queryToken(token);
        if (user==null){
            return null;
        }

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
        if (CollectionUtils.isEmpty(records)){
            log.info("recommendation dubbo查询为空!!");
            return pageResult;
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

        Map<Long,UserInfo> userInfoMap =new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            userInfoMap.put(userInfo.getUserId(),userInfo);
        }
        
        //5
        /*用来存放推荐用户详细信心的列表*/
        List<TodayBest> todayBestList =new ArrayList<>();
        for (RecommendUser record : records) {
            TodayBest todayBest = new TodayBest();
            /*根据推荐列表的用户id,从map拿到当前推荐列表用户的详细个人信息*/
            UserInfo userInfo = userInfoMap.get(record.getUserId());
            if (userInfo==null){
                /*跳出本次循环*/
                continue;
            }

            todayBest.setId(user.getId().intValue());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setAge(userInfo.getAge());
            /*分数取整*/
            BigDecimal bigDecimal = new BigDecimal(record.getScore()).setScale(0,BigDecimal.ROUND_HALF_DOWN);
            todayBest.setFateValue(bigDecimal.longValue());
            todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man":"woman");
            todayBest.setNickname(userInfo.getNickName());
            //单身,本科,年龄相仿
            todayBest.setTags(StringUtils.split(userInfo.getTags(),","));

            //填入推荐列表
            todayBestList.add(todayBest);
        }

        pageResult.setItems(todayBestList);
        return pageResult;


    }
}
