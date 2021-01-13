package com.tanhua.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.server.pojo.Users;
import com.tanhua.server.vo.PageInfo;
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
 * @create 2021/1/10 8:39
 */
@Service(version = "1.0.0")
public class UsersApiImpl implements UsersApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 添加联系人--添加好友
     * @param users
     * @return
     */
    @Override
    public boolean saveUsers(Users users) {
        if (users.getUserId()==null&&users.getFriendId()==null){
            return false;
        }

        Query query=Query.query(Criteria
                .where("userId").is(users.getUserId())
                .and("friendId").is(users.getFriendId())).limit(1);
        /*检查该好友关系是否存在*/
        Users oldUsers = mongoTemplate.findOne(query, Users.class);
        if (oldUsers!=null){
            /*关系已经存在*/
            return false;
        }


        users.setId(ObjectId.get());
        users.setDate(System.currentTimeMillis());

        mongoTemplate.save(users);

        return true;
    }

    /**
     * 根据用户id查询用户关系表
     * @param userId 用户id
     * @return
     */
    @Override
    public List<Users> queryAllUsersList(Long userId) {

        Query query=Query.query(Criteria.where("userId").is(userId));

        return mongoTemplate.find(query, Users.class);
    }

    /**
     * 根据用户id查询用户关系表-分页
     * @param userId 用户id
     * @param pageNum 页
     * @param pageSize 条
     * @return
     */
    @Override
    public PageInfo<Users> queryAllUsersList(Long userId, Integer pageNum, Integer pageSize) {
        PageInfo<Users> pageInfo =new PageInfo<>();
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(0);/*暂不提供总记录数*/

        PageRequest pageRequest =PageRequest.of(pageNum-1, pageSize, Sort.by(Sort.Order.desc("created")));

        Query query =new Query(Criteria.where("userId").is(userId)).with(pageRequest);

        List<Users> users = mongoTemplate.find(query, Users.class);

        pageInfo.setRecords(users);
        return pageInfo;
    }
}
