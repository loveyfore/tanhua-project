package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.server.api.RecommendUserApi;
import com.tanhua.server.pojo.RecommendUser;
import com.tanhua.server.vo.PageInfo;
import org.springframework.stereotype.Service;

/**
 * @Author Administrator
 * @create 2021/1/2 21:46
 */
@Service
public class RecommendUserService {

    /*dubbo远程注入*/
    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;


    /**
     * 根据当前用户查询推荐分数最高的一位用户
     * @param userId
     * @return
     */
    public RecommendUser queryMaxScore(Long userId){
        return recommendUserApi.queryWithMaxScore(userId);
    }


    /**
     * 按照得分排序
     * 查询和当前用户相关的推荐列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<RecommendUser> queryPage(Long userId, Integer pageNum, Integer pageSize) {

        return recommendUserApi.queryPageInfo(userId,pageNum,pageSize);
    }

}
