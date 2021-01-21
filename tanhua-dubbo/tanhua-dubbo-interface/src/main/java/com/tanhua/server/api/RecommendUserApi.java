package com.tanhua.server.api;

import com.tanhua.server.pojo.RecommendUser;
import com.tanhua.server.vo.PageInfo;

/**
 * @Author Administrator
 * @create 2021/1/2 20:08
 */
public interface RecommendUserApi {

    /**
     * 根据当前用户查询推荐分数最高的一位用户
     * @param userId
     * @return
     */
    RecommendUser queryWithMaxScore(Long userId);


    /**
     * 按照得分排序
     * 查询和当前用户相关的推荐列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<RecommendUser> queryPageInfo(Long userId,Integer pageNum,Integer pageSize);

    /**
     * 根据 id查询用户的缘分值
     * @param userId 当前用户id
     * @param recommendUserId 被推荐,或者佳人用户id
     * @return
     */
    RecommendUser querySocreByUserId(Long userId, Long recommendUserId);
}
