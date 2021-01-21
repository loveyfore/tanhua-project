package com.tanhua.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.server.pojo.RecommendUser;
import com.tanhua.server.pojo.Visitors;
import org.apache.commons.lang3.RandomUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/13 19:18
 */
@Service(version = "1.0.0")
public class VisitorsApiImpl implements VisitorsApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存来访记录
     * @param visitors
     * @return
     */
    @Override
    public String saveVisitor(Visitors visitors) {
        visitors.setId(ObjectId.get());
        visitors.setDate(System.currentTimeMillis());
        mongoTemplate.save(visitors);
        return visitors.getId().toHexString();
    }

    /**
     * 查询来访记录
     * @param userId 用户id
     * @param pageSize 记录数
     * @return
     */
    @Override
    public List<Visitors> topVisitor(Long userId, Integer pageSize) {

        PageRequest pageRequest =PageRequest.of(0, pageSize, Sort.by(Sort.Order.desc("date")));
        Query query =Query.query(Criteria.where("userId").is(userId)).with(pageRequest);
        return this.fillScore(mongoTemplate.find(query,Visitors.class));
    }

    /**
     * 按时间查询来访记录
     * @param userId 用户id
     * @param date 时间戳
     * @return
     */
    @Override
    public List<Visitors> topVisitor(Long userId, Long date) {
        PageRequest pageRequest =PageRequest.of(0, 5, Sort.by(Sort.Order.desc("date")));
        Query query =Query.query(Criteria
                .where("userId")
                .is(userId)
                .and("date")
                /*mongo查询条件  $lt $lte $gt $gte以上四个分别表示为：< 、 <= 、 > 、 >= 。*/
                /*查询 date 时间以后的来访数据*/
                .gte(date)).with(pageRequest);
        return this.fillScore(mongoTemplate.find(query,Visitors.class));
    }

    /**
     * 分页查询访客列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public List<Visitors> visitorList(Long userId, Integer pageNum, Integer pageSize) {
        PageRequest pageRequest =PageRequest.of(pageNum, pageSize,Sort.by(Sort.Order.desc("date")));
        Query query =Query.query(Criteria.where("userId").is(userId)).with(pageRequest);
        return mongoTemplate.find(query,Visitors.class);
    }

    /**
     * 为访客填充分数
     * @param visitorsList
     * @return
     */
    private List<Visitors> fillScore(List<Visitors> visitorsList){

        for (Visitors visitors : visitorsList) {

            Query query=Query.query(Criteria
                    /*被推荐用户id*/
                    .where("userId").is(visitors.getVisitorUserId())
                    /*用户id*/
                    .and("toUserId").is(visitors.getUserId()));

            RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);

            if (recommendUser!=null){
                visitors.setScore(recommendUser.getScore());
            }else {
                /*生成随机分数*/
                visitors.setScore(RandomUtils.nextDouble(85,90));
            }

        }

        return visitorsList;

    }
}
