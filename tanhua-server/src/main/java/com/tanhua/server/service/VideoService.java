package com.tanhua.server.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.server.api.QuanZiApi;
import com.tanhua.server.api.VideoApi;
import com.tanhua.server.config.AliyunConfig;
import com.tanhua.server.pojo.Comment;
import com.tanhua.server.pojo.Video;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.*;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Administrator
 * @create 2021/1/7 13:41
 */
@Service
@Log4j2
public class VideoService {

    @Reference(version ="1.0.0")
    private VideoApi videoApi;

    /*FastDFS*/
    @Autowired
    private FdfsWebServer fdfsWebServer;
    @Autowired
    private FastFileStorageClient storageClient;

    /*阿里OSS图片上传*/
    @Autowired
    private PicUploadService picUploadService;

    @Autowired
    private UserService userService;

    @Autowired
    private AliyunConfig aliyunConfig;

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 点赞次数
     */
    private static final String VIDEO_COMMENT_LIKE_COUNT = "VIDEO_COMMENT_LIKE_COUNT_";
    /**
     * 点赞用户状态 是否已赞（1是，0否）
     */
    private static final String VIDEO_COMMENT_LIKE_USER = "VIDEO_COMMENT_LIKE_USER_";

    /**
     * 用户关注状态 是否关注 （1是，0否）
     */
    private static final String VIDEO_FOLLOW_USER = "VIDEO_FOLLOW_USER_";


    /**
     * 视频上传保存
     * @param videoThumbnail 视频封面
     * @param videoFile 视频文件
     * @return
     */
    public String saveVideo(MultipartFile videoThumbnail, MultipartFile videoFile) {


        /*获取用户信息*/
        User user = UserThreadLocal.get();

        /*上传数据封装*/
        Video video = new Video();
        video.setId(new ObjectId());
        video.setCreated(System.currentTimeMillis());
        video.setSeeType(1);
        video.setUserId(user.getId());

        try {
            /*保存视频封面*/
            PicUploadResult uploadResult = picUploadService.upload(videoThumbnail);
            if (!"done".equals(uploadResult.getStatus())){
                return null;
            }
            /*oss图片路径*/
            video.setPicUrl(uploadResult.getName());

            /*上传视频文件到fdfs*/
            StorePath storePath = storageClient.uploadFile(videoFile.getInputStream(),
                                                           videoFile.getSize(), /*文件大小*/
                                                           StringUtils.substringAfterLast(videoFile.getOriginalFilename(), "."),/*切割文件后缀*/
                                                            null);
            video.setVideoUrl(storePath.getFullPath());
            log.info("saveVideo:{}",video);

            return videoApi.saveVideo(video);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询小视频列表
     * @param pageNum 页
     * @param pageSize 每页*条;
     * @return
     */
    public PageResult queryVideoList(Integer pageNum, Integer pageSize) {
        /**
         * 获取当前用户
         * 查询数据
         * 数据封装
         */

        User user = UserThreadLocal.get();

        PageInfo<Video> pageInfo = new PageInfo<>();

        String vidStr = redisTemplate.opsForValue().get("QUANZI_VIDEO_RECOMMEND_" + user.getId());
        if (StringUtils.isNotEmpty(vidStr)) {
            String[] vidArr = StringUtils.split(vidStr, ",");
            /*分页的方式取出数据*/
            int startIndex = (pageNum - 1) * pageSize; //0,1  2,3  4,5  6,7
            if (startIndex < vidArr.length) {
                int endIndex = startIndex + pageSize - 1;
                if (endIndex >= vidArr.length) {
                    endIndex = vidArr.length - 1;
                }
                List<Long> vidList = new ArrayList<>();
                for (int i = startIndex; i <= endIndex; i++) {
                    vidList.add(Long.valueOf(vidArr[i]));
                }

                pageInfo = videoApi.queryVideoByVid(vidList);
            }
        }else {
            /*远程调用查询*/
            pageInfo = videoApi.queryVideoList(pageNum, pageSize);
        }


        /*返回数据初始化*/
        PageResult pageResult = new PageResult();
        pageResult.setCounts(0);/*总条数,暂无*/
        pageResult.setPage(pageNum);
        pageResult.setPages(0);/*总页数暂无*/
        pageResult.setPagesize(pageSize);


        List<Video> records = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(records)){
            return pageResult;
        }

        /*返回数据的封装,videoVoList*/
        List<VideoVo> videoVoList =new ArrayList<>();

        /*获取小视频对象id查询用户详细信息*/
        List<Long> userIdList = new ArrayList<>();
        for (Video record : records) {
            if (!userIdList.contains(record.getUserId())){
                userIdList.add(record.getUserId());
            }
        }

        List<UserInfo> userInfoList = userService.queryUserInfoByUserIdList(userIdList);
        /*封装用户id对应用户个人详细信息*/
        Map<Long,UserInfo> userInfoMap =new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            userInfoMap.put(userInfo.getUserId(),userInfo);
        }

        /*数据准备完成--封装*/
        for (Video record : records) {
            UserInfo userInfo = userInfoMap.get(record.getUserId());

            VideoVo videoVo = new VideoVo();
            videoVo.setNickname(userInfo.getNickName());
            videoVo.setUserId(userInfo.getUserId());
            videoVo.setSignature("测试数据---(签名)");
            videoVo.setId(record.getId().toHexString());

            /*获取服务器Url,填充拼接数据--头像,视频,封面*/
            videoVo.fillAvatar(aliyunConfig.getUrlPrefix(),userInfo.getLogo());
            log.info("fillAvatarURL:{}",videoVo.getAvatar());
            videoVo.fillVideoUrl(fdfsWebServer.getWebServerUrl(),record.getVideoUrl());
            log.info("fillVideoURL:{}",videoVo.getVideoUrl());
            videoVo.fillCover(aliyunConfig.getUrlPrefix(),record.getPicUrl());
            log.info("fillPicURL:{}",videoVo.getCover());

            /*点赞操作like*/
            String likeCount = redisTemplate.opsForValue().get(VIDEO_COMMENT_LIKE_COUNT + record.getId());
            if(StringUtils.isEmpty(likeCount)){
                /*没有就是0个*/
                videoVo.setLikeCount(0);
            }else {
                /*有的话转换赋值*/
                videoVo.setLikeCount(Integer.parseInt(likeCount));
            }

            /*是否点赞*/
            /*缓存中是否有当前登录用户的点赞记录*/
            videoVo.setHasLiked(redisTemplate.hasKey(VIDEO_COMMENT_LIKE_USER+user.getId()+"_"+record.getId())?1:0);


            videoVo.setCommentCount(quanZiApi.queryCommentCount(videoVo.getId(),CommentTypeEnum.COMMENT.getCode()).intValue()); //评论数量 暂无

            videoVo.setHasFocus(redisTemplate.hasKey(VIDEO_FOLLOW_USER+user.getId()+"_"+record.getUserId())?1:0); //是否关注 1是 0否


            videoVoList.add(videoVo);
        }

        pageResult.setItems(videoVoList);
        return pageResult;

    }

    /**
     * 视频点赞
     * @param videoId
     * @return
     */
    public Long likeComment(String videoId) {
        /**
         * 获取用户
         * 根据用户id videoId 点赞 保存
         * 点赞成功 redis记录点赞数 变更是否点赞状态
         */
        User user = UserThreadLocal.get();

        /*保存点赞*/
        boolean saveComment = quanZiApi.saveComment(user.getId(), videoId, CommentTypeEnum.LIKE.getCode(), null);
        Long likeCount =0L;
        String countKey=VIDEO_COMMENT_LIKE_COUNT+videoId;
        if (saveComment){
            if (!redisTemplate.hasKey(countKey)){
                likeCount = quanZiApi.queryCommentCount(videoId, CommentTypeEnum.LIKE.getCode());
                redisTemplate.opsForValue().set(countKey,String.valueOf(likeCount));
            }else {
                /*如果取到了就递增*/
                redisTemplate.opsForValue().increment(countKey);
            }
            /*设置当前用户为点赞状态*/
            redisTemplate.opsForValue().set(VIDEO_COMMENT_LIKE_USER+user.getId()+"_"+videoId,"1");/*1已点赞,0未点赞*/
        }
        return likeCount;
    }

    /**
     * 取消视频点赞
     * @param videoId
     * @return
     */
    public Long disLikeComment(String videoId) {

        User user = UserThreadLocal.get();
        /*删除用户对视频的点赞操作*/
        boolean removeComment = quanZiApi.removeComment(user.getId(), videoId, CommentTypeEnum.LIKE.getCode());
        Long likeCount =0L;
        String countKey=VIDEO_COMMENT_LIKE_COUNT+videoId;
        if (removeComment){
            /*将redis中的数据递减,并删除该用户点赞的状态*/
            likeCount = redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.delete(VIDEO_COMMENT_LIKE_USER+user.getId()+"_"+videoId);
        }else {
            String count = redisTemplate.opsForValue().get(countKey);
            if (StringUtils.isEmpty(count)){
                likeCount = 0L;
            }else {
                likeCount = Long.parseLong(count);
            }
        }
        return likeCount;
    }

    /**
     * 查询小视频评论列表
     * @param videoId
     * @return
     */
    public PageResult queryCommentsList(String videoId,Integer pageNum,Integer pageSize) {
        /**
         * 获取用户
         * 查询数据  --评论数据  --每条评论对应的userInfo信息
         * 封装数据
         */

        User user = UserThreadLocal.get();

        /*返回数据初始化*/
        PageResult pageResult =new PageResult();
        pageResult.setPage(pageNum);
        pageResult.setPagesize(pageSize);
        pageResult.setPages(0);/*总页数暂无*/


        PageInfo<Comment> pageInfo = quanZiApi.queryCommentList(videoId, pageNum, pageSize);

        /*这个总记录数写不写无所谓,用的是小视频列表中返回的数据*/  //TODO
        pageResult.setCounts(pageInfo.getTotal());/*总记录数*/

        /*获取评论列表数据*/
        List<Comment> records = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(records)){
            /*空列表数据*/
            return pageResult;
        }

        /*获取每条评论的用户id用于查询用户详细信息*/
        List<Long> userIdList = new ArrayList<>();
        for (Comment record : records) {
            /*一个用户可以评论多条数据,过滤重复的id*/
            if (!userIdList.contains(record.getUserId())){
                userIdList.add(record.getUserId());
            }
        }

        /*用户详细信息*/
        List<UserInfo> userInfoList = userService.queryUserInfoByUserIdList(userIdList);
        Map<Long,UserInfo> userInfoMap  =new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            userInfoMap.put(userInfo.getUserId(),userInfo);
        }

        List<Comments> commentsList =new ArrayList<>();

        /*数据封装*/
        for (Comment record : records) {
            /*用户信息获取-map*/
            UserInfo userInfo = userInfoMap.get(record.getUserId());

            Comments comments = new Comments();
            comments.setId(record.getId().toHexString());
            comments.setAvatar(userInfo.getLogo());
            comments.setNickname(userInfo.getNickName());
            comments.setContent(record.getContent());
            /*格式化日期*/
            comments.setCreateDate(new DateTime(record.getCreated()).toString("HH:mm"));

            comments.setHasLiked(redisTemplate.hasKey(VIDEO_COMMENT_LIKE_USER+user.getId()+"_"+record.getId())?1:0);//是否点赞（1是，0否）

            String likeCount = redisTemplate.opsForValue().get(VIDEO_COMMENT_LIKE_COUNT + record.getId());
            if (StringUtils.isEmpty(likeCount)){
                comments.setLikeCount(0);//点赞数
            }else {
                comments.setLikeCount(Integer.parseInt(likeCount));//点赞数
            }

            commentsList.add(comments);
        }

        pageResult.setItems(commentsList);
        return pageResult;

    }

    /**
     * 评论发布-保存
     * @param videoId
     * @param comment
     * @return
     */
    public Boolean saveComments(String videoId, String comment) {

        User user = UserThreadLocal.get();

        return quanZiApi.saveComment(user.getId(), videoId, CommentTypeEnum.COMMENT.getCode(), comment);
    }

    /**
     * 视频用户关注
     * @param followUserId 被关注用户
     * @return
     */
    public Boolean followUser(Long followUserId) {
        /**
         * 获取操作用户
         * 存储记录
         * 改变用户关注状态
         */
        User user = UserThreadLocal.get();

        Boolean followUser = videoApi.followUser(user.getId(), followUserId);
        if (followUser){
            String followUserKey=VIDEO_FOLLOW_USER+user.getId()+"_"+followUserId;
            redisTemplate.opsForValue().set(followUserKey,"1"); /*用户关注状态 是否关注 （1是，0否）*/

            return true;
        }
        return false;
    }

    /**
     * 视频用户取消关注
     * @param followUserId 被关注用户
     * @return
     */
    public Boolean disFollowUser(Long followUserId) {
        User user = UserThreadLocal.get();
        Boolean disFollowUser = videoApi.disFollowUser(user.getId(), followUserId);
        if (disFollowUser){
            String followUserKey=VIDEO_FOLLOW_USER+user.getId()+"_"+followUserId;
            redisTemplate.delete(followUserKey);
            return true;
        }
        return false;
    }
}