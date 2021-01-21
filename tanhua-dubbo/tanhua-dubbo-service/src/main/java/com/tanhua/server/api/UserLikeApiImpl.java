package com.tanhua.server.api;


import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.server.pojo.UserLike;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @Author Administrator
 * @create 2021/1/15 19:49
 */
@Service(version = "1.0.0")
public class UserLikeApiImpl implements UserLikeApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 添加喜欢记录
     *
     * @param userId     用户id
     * @param likeUserId 被喜欢的用户id
     * @return
     */
    @Override
    public Boolean saveUserLike(Long userId, Long likeUserId) {

        Query query = Query.query(Criteria
                .where("userId").is(userId)
                .and("likeUserId").is(likeUserId));
        UserLike one = mongoTemplate.findOne(query, UserLike.class);
        if (one != null) {
            return true;
        }

        UserLike userLike = new UserLike();
        userLike.setId(ObjectId.get());
        userLike.setCreated(System.currentTimeMillis());
        userLike.setUserId(userId);
        userLike.setLikeUserId(likeUserId);

        mongoTemplate.save(userLike);

        return true;
    }

    /**
     * 判断双方是否右喜欢记录,如果有加为好友
     *
     * @param userId     用户id
     * @param likeUserId 被喜欢的用户id
     * @return
     */
    @Override
    public Boolean isMutualLike(Long userId, Long likeUserId) {

        /*当前用户是否有喜欢对方的记录*/
        Query q1 = Query.query(Criteria
                .where("userId").is(userId)
                .and("likeUserId").is(likeUserId));

        /*对方是否有喜欢当前用户的记录*/
        Query q2 = Query.query(Criteria
                .where("userId").is(likeUserId)
                .and("likeUserId").is(userId));

        UserLike u1 = mongoTemplate.findOne(q1, UserLike.class);
        UserLike u2 = mongoTemplate.findOne(q2, UserLike.class);

        if (u1 != null && u2 != null) {
            /*双方都有记录,加为好友*/
            return true;
        }

        /*其中一方没有记录*/
        return false;


        /*简化写法   --如果数据库有重复数据,那么条件就会永远不成立*/

        /*当前用户是否有喜欢对方的条件*//*
        Criteria criteria1 = Criteria.where("userId").is(userId).and("likeUserId").is(likeUserId);
        *//*对方是否有喜欢当前用户的条件*//*
        Criteria criteria2 = Criteria.where("userId").is(likeUserId).and("likeUserId").is(userId);

        *//*构建查询条件   orOperator:或运算符   还有andOperator.... *//*
        Criteria criteria = new  Criteria.orOperator(criteria1,criteria2);

        Query query = Query.query(criteria);
        *//*记录数等于两条说明相互喜欢了*//*
        return this.mongoTemplate.count(query,UserLike.class) == 2;*/
    }

    /**
     * 删除喜欢记录
     *
     * @param userId     用户id
     * @param likeUserId 被喜欢的用户id
     * @return
     */
    @Override
    public Boolean deleteUserLike(Long userId, Long likeUserId) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("likeUserId").is(likeUserId));
        /*删除记录*/
        this.mongoTemplate.remove(query,UserLike.class);
        return true;
    }

    /**
     * 相互喜欢数量-获取当前用户和其他用户相互喜欢的数量
     * @param userId 用户id
     * @return
     */
    @Override
    public Long queryEachLikeCount(Long userId) {

        /*获取喜欢列表*/
        Query query=Query.query(Criteria.where("userId").is(userId));
        List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);
        if (CollectionUtils.isEmpty(userLikeList)){
            return 0L;
        }

        /*获取被喜欢的用户id*/
        List<Long> likeUserIdList = new ArrayList<>();
        for (UserLike userLike : userLikeList) {
            likeUserIdList.add(userLike.getLikeUserId());
        }

        /*筛选数据*/
        Query query1 =query.query(Criteria.where("userId").in(likeUserIdList).and("likeUserId").in(userId));
        return mongoTemplate.count(query1,UserLike.class);
    }

    /**
     * 喜欢数-获取当前用户喜欢了多少粉丝
     * @param userId
     * @return
     */
    @Override
    public Long queryLikeCount(Long userId) {
        Query query =Query.query(Criteria.where("userId").is(userId));
        return mongoTemplate.count(query,UserLike.class);
    }

    /**
     * 粉丝数量-获取当前用户的粉丝数量,也就是多少用户喜欢了当前用户
     * @param userId
     * @return
     */
    @Override
    public Long queryFanCount(Long userId) {
        /*查询当前用户被多少用户喜欢了*/
        Query query=Query.query(Criteria.where("likeUserId").is(userId));
        return mongoTemplate.count(query,UserLike.class);
    }

    /**
     * 判断用户是否已经喜欢过该粉丝
     * @param userId
     * @param likeUserId
     * @return
     */
    @Override
    public Boolean isLike(Long userId, Long likeUserId) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("likeUserId").is(likeUserId));
        return this.mongoTemplate.count(query,UserLike.class) > 0;
    }


    /**
     * 相互喜欢列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public List<UserLike> queryEachLike(Long userId, Integer pageNum, Integer pageSize) {

        /*获取喜欢列表*/
        Query query=Query.query(Criteria.where("userId").is(userId));
        List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);
        if (CollectionUtils.isEmpty(userLikeList)){
            /*返回空集合*/
            return Collections.emptyList();
        }

        /*获取被喜欢的用户id*/
        List<Long> likeUserIdList = new ArrayList<>();
        for (UserLike userLike : userLikeList) {
            likeUserIdList.add(userLike.getLikeUserId());
        }

        /*筛选数据,注意mongodb起始页是0  按时间降序*/
        PageRequest pageRequest =PageRequest.of(pageNum-1, pageSize,Sort.by(Sort.Order.desc("created")));
        Query query1 =query.query(
                Criteria.where("userId").
                        in(likeUserIdList).
                        and("likeUserId").
                        in(userId)).with(pageRequest);
        return mongoTemplate.find(query1,UserLike.class);
    }

    /**
     * 喜欢列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public List<UserLike> queryLike(Long userId, Integer pageNum, Integer pageSize) {
        /*按时间降序*/
        PageRequest pageRequest =PageRequest.of(pageNum-1,pageSize ,Sort.by(Sort.Order.desc("created")));
        Query query =Query.query(Criteria.where("userId").is(userId)).with(pageRequest);
        return mongoTemplate.find(query,UserLike.class);
    }

    /**
     * 粉丝列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public List<UserLike> queryFan(Long userId, Integer pageNum, Integer pageSize) {
        /*按时间降序*/
        PageRequest pageRequest =PageRequest.of(pageNum-1,pageSize , Sort.by(Sort.Order.desc("created")));
        /*查询当前用户被多少用户喜欢了*/
        Query query=Query.query(Criteria.where("likeUserId").is(userId)).with(pageRequest);
        return mongoTemplate.find(query,UserLike.class);
    }
}
