package com.tanhua.recommend.vo;

import lombok.Data;

/**
 * 实体类用于映射获取消息中的参数
 */
@Data
public class QuanziMsg {
    // msg.put("userId", user.getId());
    //            msg.put("date", System.currentTimeMillis());
    //            msg.put("publishId", publishId);
    //            msg.put("pid", publish.getPid());
    //            msg.put("type", type);
    /**
     * 操作用户id
     */
    private Long userId;
    /**
     * 操作时间
     */
    private Long date;
    /**
     * 操作动态id
     */
    private String publishId;
    /**
     *  pid spark 使用
     */
    private Long pid;
    /**
     * 操作类型
     */
    private Integer type;
}