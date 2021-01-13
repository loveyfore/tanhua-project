package com.tanhua.server.api;

import com.tanhua.server.pojo.Comment;
import com.tanhua.server.pojo.Publish;
import com.tanhua.server.vo.PageInfo;

import java.util.List;


public interface QuanZiApi {

    /**
     * 发布动态
     * 动态全是统一的存储到了一张表中
     * @param publish
     * @return
     */
    String savePublish(Publish publish);

    /**
     * 查询当前用户的好友动态
     * 也就是查询自己的时间线表,因为好友如果发布动态那么会写到自己的时间线表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @param isRecommend 是否是推荐动态
     * @return
     */
    PageInfo<Publish> queryPublishList (Long userId,Integer pageNum,Integer pageSize,boolean isRecommend);


    /**
     * 保存 评论-点赞-喜欢
     * @param userId 用户id
     * @param publishId 视频,动态..等id
     * @param commentType 操作类型 1点赞 2评论 3喜欢
     * @param content 评论内容
     * @return
     */
    boolean saveComment(Long userId,String publishId,Integer commentType,String content);

    /**
     * 删除评论点赞喜欢
     * @param userId
     * @param publishId
     * @param commentType
     * @return
     */
    boolean removeComment(Long userId,String publishId,Integer commentType);

    /**
     * 根据喜欢,点赞,评论查询总数
     * @param publishId
     * @param commentType
     * @return
     */
    Long queryCommentCount(String publishId,Integer commentType);

    /**
     * 查询单条动态,根据动态id查询单条动态
     * @param publishId
     * @return
     */
    Publish queryPublishById(String publishId);


    /**
     * 查询评论表列表,评论点赞,评论取消详细信息
     * @param publishId 动态id
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<Comment> queryCommentList(String publishId,Integer pageNum,Integer pageSize);

    /**
     * 根据id去查询评论
     * @param publishId
     * @return
     */
    Comment queryCommentById(String publishId);

    /**
     * 根据当前用户id查询该用户下所有,被点赞,评论,喜欢的作品(动态,小视频..) 的详细信息  --分页
     * @param userId
     * @param type
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<Comment> queryCommentListByUser(Long userId, Integer type, Integer pageNum, Integer pageSize);

    /**
     * 根据pid集合去查询动态数据
     * @param pidList
     * @return
     */
    PageInfo<Publish> queryPublishByPid(List<Long> pidList);
}