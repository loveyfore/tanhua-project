package com.tanhua.server.api;


import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.server.pojo.RecommendUser;
import com.tanhua.server.vo.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/2 20:18
 */
@Service(version = "1.0.0")/*声明这是一个dubbo服务*/
public class RecommendUserApiImpl implements RecommendUserApi {

    /*引入mongo模板*/
    @Autowired
    private MongoTemplate mongoTemplate;




    /**
     * 根据当前用户查询推荐分数最高的一位用户
     * @param userId
     * @return
     */
    @Override
    public RecommendUser queryWithMaxScore(Long userId) {
        /*根据当前用户id查询,对应分数最高的佳人,按照分数降序排序*/
                    /*查询toUserId字段=当前用户id的数据*/
        Query query=Query.query(Criteria.where("toUserId").is(userId))
                    /*按分数排序,取一条数据*/
                    .with(Sort.by(Sort.Order.desc("score"))).limit(1);
        return mongoTemplate.findOne(query,RecommendUser.class);
    }

    /**
     * 按照得分排序
     * 查询和当前用户相关的推荐列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize) {
        /*mongoDB的分页查询第一页是从0开始的,所以要减1操作---之后按照分数降序排序*/
        PageRequest pageRequest =PageRequest.of(pageNum-1,pageSize,Sort.by(Sort.Order.desc("score")));

        /*查询用户对应的推荐列表的所有数据*/
        Query query =Query.query(Criteria.where("toUserId").is(userId))
                     /*分页*/
                     .with(pageRequest);
        List<RecommendUser> recommendUserList = mongoTemplate.find(query, RecommendUser.class);


        /*封装返回*/
        return new PageInfo<>(0,pageNum,pageSize,recommendUserList);
    }

    /**
     * 根据 id查询用户的缘分值
     * @param userId 当前用户id
     * @param recommendUserId 被推荐,或者佳人用户id
     * @return
     */
    @Override
    public RecommendUser querySocreByUserId(Long userId, Long recommendUserId) {
        if (userId==null&& recommendUserId==null){
            return null;
        }

        Query query=Query.query(Criteria
                .where("toUserId").is(userId)
                .and("userId").is(recommendUserId)).limit(1);
        return mongoTemplate.findOne(query,RecommendUser.class);
    }
}
