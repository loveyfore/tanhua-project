package com.tanhua.server.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.server.api.QuanZiApi;
import com.tanhua.server.config.AliyunConfig;
import com.tanhua.server.pojo.Publish;
import com.tanhua.server.utils.RelativeDateFormat;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.*;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * @Author Administrator
 * @create 2021/1/3 22:24
 */
@Service
public class MovementsService {

    /*远程注入消费*/
    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private PicUploadService picUploadService;

    @Autowired
    private AliyunConfig aliyunConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 点赞数
     */
    private static final String QUANZI_LIKE_COUNT = "QUANZI_LIKE_COUNT_";

    /**
     * 是否点赞
     */
    private static final String QUANZI_LIKE_USER = "QUANZI_LIKE_USER_";

    /**
     * 喜欢数
     */
    private static final String QUANZI_LOVE_COUNT = "QUANZI_LOVE_COUNT_";

    /**
     * 是否喜欢
     */
    private static final String QUANZI_LOVE_USER = "QUANZI_LOVE_USER_";


    /**
     * 保存发布动态
     * @param textContent
     * @param location
     * @param longitude
     * @param latitude
     * @param multipartFiles
     * @return
     */
    public boolean save(String textContent,
                        String location,
                        String longitude,
                        String latitude,
                        MultipartFile[] multipartFiles) {
        //查询当前的登录用户信息
        User user = UserThreadLocal.get();
//        if (null == user) {
//            return false;
//        }

        Publish publish = new Publish();
        publish.setUserId(user.getId());
        publish.setText(textContent);
        publish.setLocationName(location);
        publish.setLatitude(latitude);
        publish.setLongitude(longitude);
        publish.setSeeType(1);

        List<String> picUrls = new ArrayList<>();
        //图片上传
        for (MultipartFile file : multipartFiles) {
            PicUploadResult picUploadResult = this.picUploadService.upload(file);
            picUrls.add(picUploadResult.getName());
        }

        publish.setMedias(picUrls);
        return quanZiApi.savePublish(publish);
    }


    /**
     * 查询好友动态
     * @param pageNum
     * @param pageSize
     * @param isRecommend 该条件用于判单是查询好友动态还是推荐动态
     * @return
     */
    public PageResult queryPublishList(Integer pageNum, Integer pageSize,boolean isRecommend) {

        //查询当前的登录用户信息
        User user = UserThreadLocal.get();

        /*根据isRecommend 判断查询好友动态和推荐动态*/
        PageInfo<Publish> pageInfo = quanZiApi.queryPublishList(user.getId(), pageNum, pageSize,isRecommend);
        /*封装返回结果*/
        PageResult pageResult=new PageResult();
        pageResult.setItems(Collections.emptyList());
        pageResult.setPage(pageNum);
        pageResult.setPagesize(pageSize);
        pageResult.setCounts(0);//总记录数暂无
        pageResult.setPages(0);//总页数暂无

        List<Publish> records = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(records)){
            /*没有动态信息*/
            return pageResult;
        }

        /*对动态数据(动态表数据)处理*/
        List<Movements> movementsList =new ArrayList<>();
        List<Long> userIdsList = new ArrayList<>();
        for (Publish record : records) {
            /*id用于查询该动态用户的详细信息*/
            userIdsList.add(record.getUserId());

            Movements movements = new Movements();
            /*共性方法将publish对movements进行填充*/
            this.fillPublishToMovements(record, movements);

            movementsList.add(movements);
        }

        /*对用户详细信息处理--每条动态有用户的详细信息*/
        List<UserInfo> userInfoList = userService.queryUserInfoByUserIdList(userIdsList);
        /*用户id对应的用户详细信息*/
        Map<Long,UserInfo> userInfoMap=new HashMap<>();
        userInfoList.forEach(userInfo -> {
            userInfoMap.put(userInfo.getUserId(),userInfo);
        });

        movementsList.forEach((movements -> {
            Long publishUserId =movements.getUserId();
            UserInfo userInfo = userInfoMap.get(publishUserId);
            if (userInfo!=null){
                /*共性方法将UserInfo对movements进行填充*/
                this.fillUserInfoToMovements(user, movements, userInfo);
            }
        }));

        /*数据返回*/
        pageResult.setItems(movementsList);
        return pageResult;

    }

    /**
     * 抽取通用方法,对数据进行填充
     * 主要处理publish(动态)数据填充
     * Publish------>Movements
     * @param record
     * @param movements
     */
    private void fillPublishToMovements(Publish record, Movements movements) {
        movements.setId(record.getId().toHexString());
        /*用于拼接没有以http开头的媒体文件--因为要访问oss所以地址必须是URL*/
        record.fillMedias(aliyunConfig.getUrlPrefix());
        /*动态图片信息--链接处理后的数据,正常赋值*/
        movements.setImageContent(record.getMedias().toArray(new String[]{}));
        movements.setTextContent(record.getText());
        movements.setUserId(record.getUserId());
        /*发布时间,用到了工具类转换   1天前---1小时前--一年前*/
        movements.setCreateDate(RelativeDateFormat.format(new Date(record.getCreated())));
    }

    /**
     * 抽取通用方法,对数据进行填充
     * 主要处理UserInfo(用户详细信息)数据填充
     * UserInfo------>Movements
     * @param user
     * @param movements
     * @param userInfo
     */
    private void fillUserInfoToMovements(User user, Movements movements, UserInfo userInfo) {
        movements.setAge(userInfo.getAge());
        movements.setAvatar(userInfo.getLogo());
        movements.setGender(userInfo.getSex().name().toLowerCase());
        movements.setNickname(userInfo.getNickName());
        movements.setTags(StringUtils.split(userInfo.getTags(),","));
        movements.setCommentCount(quanZiApi.queryCommentCount(movements.getId(),CommentTypeEnum.COMMENT.getCode()).intValue());//TODO 评论数暂无数据
        movements.setDistance("5.2公里");//TODO 距离暂无数据

        movements.setHasLiked(redisTemplate.hasKey(QUANZI_LIKE_USER+user.getId()+"_"+movements.getId())?1:0); //TODO 是否点赞（1是，0否）
        movements.setHasLoved(redisTemplate.hasKey(QUANZI_LOVE_USER+user.getId()+"_"+movements.getId())?1:0);//TODO 是否喜欢（1是，0否）

        String likeCount = redisTemplate.opsForValue().get(QUANZI_LIKE_COUNT + movements.getId());// 点赞数量
        if(StringUtils.isEmpty(likeCount)){
            movements.setLikeCount(0);  //点赞数
        }else {
            movements.setLikeCount(Integer.parseInt(likeCount));//点赞数
        }


        String loveCount = redisTemplate.opsForValue().get(QUANZI_LOVE_COUNT + movements.getId());// 喜欢数量
        if(StringUtils.isEmpty(loveCount)){
            movements.setLoveCount(0);  //喜欢数
        }else {
            movements.setLoveCount(Integer.parseInt(loveCount));//喜欢数
        }
        //movements.setLoveCount(1352); //TODO 喜欢数量--如上已完成
    }

    /**
     * 点赞
     * @param publishId
     * @return
     */
    public Long likeComment(String publishId) {
        /**
         * 获取用户
         * 根据用户id   publishId 点赞操作
         * 点赞成功,redis记录点赞数 是否点赞
         */
        User user = UserThreadLocal.get();

        boolean saveComment = quanZiApi.saveComment(user.getId(),
                publishId,
                CommentTypeEnum.LIKE.getCode(), null);

        Long likeCount=1L;

        if (saveComment){
            /*redis*/
            String countKey=QUANZI_LIKE_COUNT+publishId;
            if (!redisTemplate.hasKey(countKey)){
                likeCount = quanZiApi.queryCommentCount(publishId, CommentTypeEnum.LIKE.getCode());
                /*放入redis*/
                redisTemplate.opsForValue().set(countKey,String.valueOf(likeCount));

            }else {
                /*如果有数据,那么进行自增*/
                likeCount = redisTemplate.opsForValue().increment(countKey);
            }

            /*标识当前用户的点赞状态*/
            redisTemplate.opsForValue().set(QUANZI_LIKE_USER+user.getId()+"_"+publishId,"1");
        }
        return likeCount;

    }

    /**
     * 取消点赞
     * @param publishId
     * @return
     */
    public Long dislike(String publishId) {
        User user = UserThreadLocal.get();

        boolean removeComment = quanZiApi.removeComment(user.getId(), publishId, CommentTypeEnum.LIKE.getCode());
        Long likeCount =0L;
        String countKey=QUANZI_LIKE_COUNT+publishId;
        /*取消成功   点赞总数减1,   是否点赞 删除      ,如果没有删除成功(else),从redis获取之前的点赞数 返回*/
        if (removeComment){
            likeCount = redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.delete(QUANZI_LIKE_USER+user.getId()+"_"+publishId);

        }else{
            String c = redisTemplate.opsForValue().get(countKey);
            if (StringUtils.isEmpty(c)){
                likeCount=0L;
            }else {
                likeCount=Long.parseLong(c);
            }
        }
        return likeCount;
    }


    /**
     * 动态喜欢
     * @param publishId
     * @return
     */
    public Long loveComment(String publishId) {
        /**
         * 获取用户
         * 根据用户id   publishId 喜欢操作
         * 喜欢成功,redis记录喜欢数 是否喜欢
         */
        User user = UserThreadLocal.get();

        boolean saveComment = quanZiApi.saveComment(user.getId(),
                publishId,
                CommentTypeEnum.LOVE.getCode(), null);

        Long loveCount=1L;

        if (saveComment){
            /*redis*/
            String countKey=QUANZI_LOVE_COUNT+publishId;
            if (!redisTemplate.hasKey(countKey)){
                loveCount = quanZiApi.queryCommentCount(publishId, CommentTypeEnum.LOVE.getCode());
                /*放入redis*/
                redisTemplate.opsForValue().set(countKey,String.valueOf(loveCount));

            }else {
                /*如果有数据,那么进行自增*/
                loveCount = redisTemplate.opsForValue().increment(countKey);
            }

            /*标识当前用户的点赞状态*/
            redisTemplate.opsForValue().set(QUANZI_LOVE_USER+user.getId()+"_"+publishId,"1");
        }
        return loveCount;
    }


    /**
     * 动态取消喜欢
     * @param publishId
     * @return
     */
    public Long disLove(String publishId) {
        User user = UserThreadLocal.get();

        boolean removeComment = quanZiApi.removeComment(user.getId(), publishId, CommentTypeEnum.LOVE.getCode());
        Long loveCount =0L;
        String countKey=QUANZI_LOVE_COUNT+publishId;
        /*取消成功   喜欢总数减1,   是否点喜欢 删除      ,如果没有删除成功(else),从redis获取之前的喜欢数 返回*/
        if (removeComment){
            loveCount = redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.delete(QUANZI_LOVE_USER+user.getId()+"_"+publishId);

        }else{
            String c = redisTemplate.opsForValue().get(countKey);
            if (StringUtils.isEmpty(c)){
                loveCount=0L;
            }else {
                loveCount=Long.parseLong(c);
            }
        }
        return loveCount;
    }

    /**
     * 查询单条动态方法
     * @param publishId
     * @return
     */
    public Movements queryById(String publishId) {
        /**
         * 获取动态
         * 获取用户信息
         * 数据封装返回
         */
         User user = UserThreadLocal.get();

        Publish publish = quanZiApi.queryPublishById(publishId);
        if (publish==null){
            return null;
        }
        UserInfo userInfo = userService.queryUserInfoByUserId(publish.getUserId());
        if (userInfo==null){
            return null;
        }

        /*数据封装*/
        Movements movements = new Movements();
        /*注意数据封装*/  // <- TODO Movements封装过程
        this.fillPublishToMovements(publish,movements);
        this.fillUserInfoToMovements(user,movements,userInfo);
        return movements;
    }
}

