package com.tanhua.server.api;

import com.tanhua.server.pojo.UserLike;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/15 19:45
 */
public interface UserLikeApi {

    /**
     * 保存用户喜欢记录
     * @param userId 用户id
     * @param likeUserId 被喜欢的用户id
     * @return
     */
    Boolean saveUserLike(Long userId, Long likeUserId);


    /**
     *
     * 判断用户是否相互喜欢,如果是,那么就环信添加为好友
     * @param userId 用户id
     * @param likeUserId 被喜欢的用户id
     * @return
     */
    Boolean isMutualLike(Long userId, Long likeUserId);

    /**
     * 删除用户喜欢
     * @param userId 用户id
     * @param likeUserId 被喜欢的用户id
     * @return
     */
    Boolean deleteUserLike(Long userId, Long likeUserId);

    /**
     * 相互喜欢的数量
     * @param userId 用户id
     * @return
     */
    Long queryEachLikeCount(Long userId);

    /**
     * 喜欢数
     *
     * @return
     */
    Long queryLikeCount(Long userId);

    /**
     * 粉丝数
     *
     * @return
     */
    Long queryFanCount(Long userId);

    /**
     * 判断当前用户是否已经喜欢过粉丝
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean isLike(Long userId,Long likeUserId);

    /**
     * 相互喜欢的详情列表
     *
     * @return
     */
    List<UserLike> queryEachLike(Long userId, Integer page, Integer pageSize);

    /**
     * 喜欢列表
     *
     * @return
     */
    List<UserLike> queryLike(Long userId,Integer page,Integer pageSize);

    /**
     * 粉丝列表
     *
     * @return
     */
    List<UserLike> queryFan(Long userId,Integer page,Integer pageSize);

}
