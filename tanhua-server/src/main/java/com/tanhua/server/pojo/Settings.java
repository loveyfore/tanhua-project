package com.tanhua.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类用于封装用户通用设置-通知设置
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings extends BasePojo {

    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 推送喜欢通知--默认开启
     * 数据库存储的时候 是 0 false 1 true
     */
    private Boolean likeNotification;
    /**
     * 推送评论通知--默认开启
     * 数据库存储的时候 是 0 false 1 true
     */
    private Boolean pinglunNotification;
    /**
     * 推送公告通知--默认开启
     * 数据库存储的时候 是 0 false 1 true
     */
    private Boolean gonggaoNotification;

}