package com.tanhua.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.mongodb.client.result.DeleteResult;
import com.tanhua.server.pojo.FollowUser;
import com.tanhua.server.pojo.Video;
import com.tanhua.server.vo.PageInfo;
import jdk.nashorn.internal.ir.ReturnNode;
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
 * @create 2021/1/7 13:31
 */
@Service(version = "1.0.0")
public class VideoApiImpl implements VideoApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 小视频-发布保存
     * @param video
     * @return
     */
    @Override
    public Boolean saveVideo(Video video) {
        /*对用户做校验*/
        if (video.getUserId()==null){
            return false;
        }
        mongoTemplate.save(video);
        return true;
    }

    /**
     * 分页查询小视频,按时间降序
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Video> queryVideoList(Integer pageNum, Integer pageSize) {
        /**
         * 按条件查询(注意mongoDB第一页从0开始)
         * 数据封装返回
         */

        PageRequest pageRequest =PageRequest.of(pageNum-1, pageSize, Sort.by(Sort.Order.desc("created")));
        Query query =new Query().with(pageRequest);
        List<Video> videos = mongoTemplate.find(query, Video.class);

        PageInfo<Video> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(0);/*暂无*/
        pageInfo.setRecords(videos);
        return pageInfo;
    }

    /**
     * 关注用户
     * @param userId 操作的用户
     * @param followUserId 被关注的用户
     * @return
     */
    @Override
    public Boolean followUser(Long userId, Long followUserId) {
        try {
            if (userId!=null&&followUserId!=null){
                FollowUser followUser = new FollowUser();
                followUser.setId(ObjectId.get());
                followUser.setUserId(userId);
                followUser.setFollowUserId(followUserId);
                followUser.setCreated(System.currentTimeMillis());

                mongoTemplate.save(followUser);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 取消关注用户
     * @param userId 操作的用户
     * @param followUserId 被关注的用户
     * @return
     */
    @Override
    public Boolean disFollowUser(Long userId, Long followUserId) {
        try {
            if (userId!=null&&followUserId!=null){
                Query query=Query.query(Criteria
                        .where("userId").is(userId)
                        .and("followUserId").is(followUserId));
                DeleteResult deleteResult = mongoTemplate.remove(query,FollowUser.class);
                return deleteResult.getDeletedCount()>0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
