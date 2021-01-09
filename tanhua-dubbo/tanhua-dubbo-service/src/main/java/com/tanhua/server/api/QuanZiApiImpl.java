package com.tanhua.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.server.pojo.*;
import com.tanhua.server.vo.CommentTypeEnum;
import com.tanhua.server.vo.PageInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/3 21:53
 * 提供者
 */
@Service(version = "1.0.0")
public class QuanZiApiImpl implements QuanZiApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean savePublish(Publish publish) {
        /**
         * 1.写入发布表
         * 2.写入用户表
         * 3.写入好友的时间线
         */

        /*参数校验保证参数正确性
        * text一般内容的校验会替换敏感关键字为****
        * */
        Long userId = publish.getUserId();
        if (userId==null){
            return false;
        }

        //1
        /*统一数据发布时间*/
        long create = System.currentTimeMillis();
        publish.setCreated(create);
        publish.setId(ObjectId.get());
        publish.setSeeType(1);
        mongoTemplate.save(publish);

        //2
        Album album = new Album();
        album.setCreated(create);
        album.setPublishId(publish.getId());
        album.setId(ObjectId.get());
        mongoTemplate.save(album,"quanzi_album_"+userId);


        //3
        /*获取当前发布用户的好友*/
        Query query =Query.query(Criteria.where("userId").is(userId));
        List<Users> users = mongoTemplate.find(query, Users.class);
        for (Users user : users) {
            TimeLine timeLine = new TimeLine();
            timeLine.setDate(create);
            timeLine.setUserId(userId);
            timeLine.setPublishId(publish.getId());
            timeLine.setId(ObjectId.get());

            /*写入好友时间线表*/
            mongoTemplate.save(timeLine,"quanzi_time_line_"+user.getFriendId());
        }

        return true;
    }



    /**
     * 查询当前用户的好友动态
     * 也就是查询自己的时间线表,因为好友如果发布动态那么会"写到自己的时间线表"
     * @param userId
     * @param pageNum
     * @param pageSize
     * @param isRecommend 是否是推荐动态
     * @return
     */
    @Override
    public PageInfo<Publish> queryPublishList(Long userId, Integer pageNum, Integer pageSize,boolean isRecommend) {

        /*判断是要查询推荐动态还是好友动态,来查询不同的表--默认查询"推荐动态"*/
        String collectionName = "quanzi_time_line_recommend";
        if (!isRecommend){
            /*好友动态*/
            collectionName = "quanzi_time_line_" + userId;
        }






        /*分页查询对象--将当前用户的时间线表按日期的降序排序--需要注意mongoDB的分页索引是从0开始的*/
        PageRequest pageRequest =PageRequest.of(pageNum-1,pageSize, Sort.by(Sort.Order.desc("date")));
        Query query =new Query().with(pageRequest);
        /*查询当前用户的时间线表*/
        List<TimeLine> timeLineList = mongoTemplate.find(query, TimeLine.class, collectionName);

        /*获取该用户时间线中所有的动态id*/
        List<ObjectId> publishIds=new ArrayList<>();
        for (TimeLine timeLine : timeLineList) {
            publishIds.add(timeLine.getPublishId());
        }

        /*查询动态--按发布时间的降序排序*/

        Query queryPublish=Query.query(Criteria.where("id").in(publishIds)).with(Sort.by(Sort.Order.desc("created")));
        List<Publish> publishList = mongoTemplate.find(queryPublish, Publish.class);

        /*封装返回数据*/
        PageInfo<Publish> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setRecords(publishList);
        pageInfo.setTotal(0); //暂不提供 TODO

        return pageInfo;
    }


    /**
     * 保存 评论-点赞-喜欢
     * @param userId 用户id
     * @param publishId 动态id
     * @param commentType 评论类型   1点赞 2评论 3喜欢
     * @param content 评论内容
     * @return
     */
    @Override
    public boolean saveComment(Long userId, String publishId, Integer commentType, String content) {
        /*对点赞和喜欢做判断,因为每个用户对一条动态只能做一次操作,且只有一条记录*/
        if (commentType.equals(CommentTypeEnum.LIKE.getCode()) || commentType.equals(CommentTypeEnum.LOVE.getCode())){
            Criteria criteria = Criteria
                    .where("userId")
                    .is(userId)
                    .and("publishId").is(new ObjectId(publishId))
                    .and("commentType").is(commentType);
            Query query=Query.query(criteria).limit(1);
            Comment commentOne = mongoTemplate.findOne(query, Comment.class);
            /*如果查到了数据,不做操作*/
            if (commentOne!=null){
                return true;
            }
        }


        /*如果没有记录,为该用户创建*/
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCommentType(commentType);
        comment.setUserId(userId);
        comment.setParentId(new ObjectId(publishId));
        comment.setPublishId(new ObjectId(publishId));
        comment.setCreated(System.currentTimeMillis());
        comment.setIsParent(true);
        comment.setId(ObjectId.get());
        mongoTemplate.save(comment);
        return true;
    }

    /**
     * 删除评论点赞喜欢
     * @param userId
     * @param publishId
     * @param commentType
     * @return
     */
    @Override
    public boolean removeComment(Long userId, String publishId, Integer commentType) {
        Criteria criteria = Criteria
                .where("userId")
                .is(userId)
                .and("publishId").is(new ObjectId(publishId))
                .and("commentType").is(commentType);
        Query query = Query.query(criteria);
        this.mongoTemplate.remove(query, Comment.class);
        return true;
    }


    /**
     * 根据喜欢,点赞,评论查询总数
     * @param publishId
     * @param commentType
     * @return
     */
    @Override
    public Long queryCommentCount(String publishId, Integer commentType) {
        Criteria criteria = Criteria
                .where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(commentType);
        Query query = Query.query(criteria);
        return this.mongoTemplate.count(query,Comment.class);
    }


    /**
     * 根据动态id查询指定动态
     * @param publishId
     * @return
     */
    @Override
    public Publish queryPublishById(String publishId) {
        return mongoTemplate.findById(new ObjectId(publishId),Publish.class);
    }


    /**
     * 查询评论表列表,评论点赞,评论取消详细信息
     * 特定用于获取所有评论数据的方法
     * @param publishId 动态id
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Comment> queryCommentList(String publishId, Integer pageNum, Integer pageSize) {
        /**
         * 注意mongoDB分页索引从0开始
         * 查询评论表--分页升序
         * CommentType为评论类型的数据 //评论类型，1-点赞，2-评论，3-喜欢
         * 将评论列表返回
         */


        PageRequest pageRequest =PageRequest.of(pageNum-1, pageSize,Sort.by(Sort.Order.asc("created")));
        Query query =new Query(Criteria
                /*查询指定动态,下的所有评论*/ //TODO
                .where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(CommentTypeEnum.COMMENT.getCode())
        ).with(pageRequest);
        List<Comment> comments = mongoTemplate.find(query, Comment.class);

        /*数据封装*/
        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(this.queryCommentCount(publishId,CommentTypeEnum.COMMENT.getCode()).intValue());
        pageInfo.setRecords(comments);
        return pageInfo;
    }
}
