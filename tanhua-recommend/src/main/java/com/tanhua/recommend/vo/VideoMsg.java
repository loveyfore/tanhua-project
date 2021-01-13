package com.tanhua.recommend.vo;

import lombok.Data;

/**
 * 实体类用于映射获取消息中的参数
 */
@Data
public class VideoMsg {

    /**
     * 操作用户id
     */
    private Long userId;
    /**
     * 操作时间
     */
    private Long date;
    /**
     * 操作的视频id
     */
    private String videoId;
    /**
     * vid spark 使用
     */
    private Long vid;
    /**
     * 操作类型
     */
    private Integer type;
}