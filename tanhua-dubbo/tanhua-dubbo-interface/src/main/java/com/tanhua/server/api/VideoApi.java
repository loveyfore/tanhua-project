package com.tanhua.server.api;

import com.tanhua.server.pojo.Video;
import com.tanhua.server.vo.PageInfo;

import java.util.List;

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
    String saveVideo(Video video);

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

    /**
     * 根据id视频查询
     * @param publishId
     * @return
     */
    Video queryVideoById(String publishId);

    /**
     * 根据vid集合查询详视频数据
     * @param vidList
     * @return
     */
    PageInfo<Video> queryVideoByVid(List<Long> vidList);
}
