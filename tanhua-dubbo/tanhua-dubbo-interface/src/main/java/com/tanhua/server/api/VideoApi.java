package com.tanhua.server.api;

import com.tanhua.server.pojo.Video;
import com.tanhua.server.vo.PageInfo;

/**
 * @Author Administrator
 * @create 2021/1/7 13:24
 */
public interface VideoApi {

    /**
     * 小视频-发布保存
     * @param video
     * @return
     */
    Boolean saveVideo(Video video);

    /**
     * 分页查询小视频,按时间降序
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<Video> queryVideoList(Integer pageNum,Integer pageSize);

    /**
     * 关注用户
     * @param userId 操作的用户
     * @param followUserId 被关注的用户
     * @return
     */
    Boolean followUser(Long userId,Long followUserId);

    /**
     * 取消关注用户
     * @param userId 操作的用户
     * @param followUserId 被关注的用户
     * @return
     */
    Boolean disFollowUser(Long userId,Long followUserId);
}
