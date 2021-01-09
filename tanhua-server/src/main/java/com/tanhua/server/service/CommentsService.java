package com.tanhua.server.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.server.api.QuanZiApi;
import com.tanhua.server.pojo.Comment;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.CommentTypeEnum;
import com.tanhua.server.vo.Comments;
import com.tanhua.server.vo.PageInfo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Administrator
 * @create 2021/1/6 19:40
 */
@Service
@Log4j2
public class CommentsService {

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    /**
     * 点赞次数
     */
    private static final String QUANZI_COMMENT_LIKE_COUNT = "QUANZI_COMMENT_LIKE_COUNT_";
    /**
     * 点赞用户状态
     */
    private static final String QUANZI_COMMENT_LIKE_USER = "QUANZI_COMMENT_LIKE_USER_";

    public PageResult queryCommentsList(String publishId, Integer pageNum, Integer pageSize) {
        /**
         * 获取当前user
         * 根据publishId从评论表中查询评论列表
         * 封装pageResult items->List<Comments>
         */

        User user = UserThreadLocal.get();

        PageResult pageResult = new PageResult();
        pageResult.setPage(pageNum);
        pageResult.setPagesize(pageSize);
        pageResult.setPages(0);

        PageInfo<Comment> pageInfo = quanZiApi.queryCommentList(publishId, pageNum, pageSize);
        /*为总记录数赋值--pageInfo实现已经做了总数的查询*/
        pageResult.setCounts(pageInfo.getTotal());

        /*获取评论表数据*/
        List<Comment> records = pageInfo.getRecords();
        //log.info("ResultRecords:{}",records);
        if (CollectionUtils.isEmpty(records)){
            /*默认的items是空list---没有评论数据*/
            return pageResult;
        }

        /*返回数据封装*/
        List<Comments> commentsList =new ArrayList<>();

        /*获取评论用户id集合*/
        List<Long> userIdList = new ArrayList<>();
        for (Comment record : records) {
            /*过滤已经存在的用户*/
            if (!userIdList.contains(record.getUserId())){
                userIdList.add(record.getUserId());
            }
        }

        List<UserInfo> userInfoList = userService.queryUserInfoByUserIdList(userIdList);
        /**/
        Map<Long,UserInfo> userInfoMap =new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            userInfoMap.put(userInfo.getUserId(),userInfo);
        }

        /*封装Start*/
        for (Comment record : records) {
            /*获取当前评论用户的详细信息*/
            UserInfo userInfo = userInfoMap.get(record.getUserId());

            Comments comments = new Comments();
            comments.setId(record.getId().toHexString());
            comments.setAvatar(userInfo.getLogo());
            comments.setContent(record.getContent());
            comments.setNickname(userInfo.getNickName());
            /*格式化日期*/
            comments.setCreateDate(new DateTime(record.getCreated()).toString("HH:mm"));

            /*点赞操作like*/
            String likeCount = redisTemplate.opsForValue().get(QUANZI_COMMENT_LIKE_COUNT + record.getId());
            if(StringUtils.isEmpty(likeCount)){
                /*没有就是0个*/
                comments.setLikeCount(0);
            }else {
                /*有的话转换赋值*/
                comments.setLikeCount(Integer.parseInt(likeCount));
            }

            /*是否点赞*/
            /*缓存中是否有当前登录用户的点赞记录*/
            comments.setHasLiked(redisTemplate.hasKey(QUANZI_COMMENT_LIKE_USER+user.getId()+"_"+record.getId())?1:0);

            commentsList.add(comments);

        }
        pageResult.setItems(commentsList);
        //log.info("commentsList:{}",commentsList);
        return pageResult;
    }

    /**
     * 评论-提交
     * @param publishId
     * @param comment
     * @return
     */
    public Boolean saveComments(String publishId, String comment) {
        /**
         * 获取登录user
         * 保存评论
         */

        User user = UserThreadLocal.get();
        return quanZiApi.saveComment(user.getId(), publishId, CommentTypeEnum.COMMENT.getCode(), comment);

    }

    /**
     * 评论-点赞
     * @param commentId
     * @return
     */
    public Long likeComment(String commentId) {
        /**
         * 获取用户
         * 根据用户id commentId 点赞 保存评论
         * 点赞成功 redis记录点赞数  变更是否点赞状态
         */

        User user = UserThreadLocal.get();
        boolean saveComment = quanZiApi.saveComment(user.getId(), commentId, CommentTypeEnum.COMMENT.getCode(), null);
        Long likeCount = 0L;
        if (saveComment){
            /*点赞成功*/
            String countKey=QUANZI_COMMENT_LIKE_COUNT+commentId;
            if(!redisTemplate.hasKey(countKey)){
                likeCount = quanZiApi.queryCommentCount(commentId, CommentTypeEnum.LIKE.getCode());
                redisTemplate.opsForValue().set(countKey,String.valueOf(likeCount));
            }else {
                /*如果取到了那么递增*/
                likeCount = redisTemplate.opsForValue().increment(countKey);
            }
            /*设置当前用户的点赞状态*/
            this.redisTemplate.opsForValue().set(QUANZI_COMMENT_LIKE_USER+user.getId()+"_"+commentId,"1");
        }
        return likeCount;

    }

    /**
     * 评论-取消点赞
     * @param commentId
     * @return
     */
    public Long cancelLikeComment(String commentId) {
        User user = UserThreadLocal.get();
        /*删除用户点赞记录*/
        boolean removeComment = quanZiApi.removeComment(user.getId(), commentId, CommentTypeEnum.LIKE.getCode());
        Long likeCount = 0L;
        String countKey = QUANZI_COMMENT_LIKE_COUNT+commentId;
        if (removeComment){
            /*如果去到了就递减*/
            likeCount = redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.delete(QUANZI_COMMENT_LIKE_USER+user.getId()+"_"+commentId);
        }else {
            String s = this.redisTemplate.opsForValue().get(countKey);
            if (StringUtils.isEmpty(s)){
                likeCount = 0L;
            }else{
                likeCount = Long.parseLong(s);
            }
        }
        return likeCount;
    }
}
