package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类用于封装用户通用设置数据的返回
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsVo {

    /**
     * 编号
     */
    private Long id;
    /**
     * 问题
     */
    private String strangerQuestion = "";
    /**
     * 手机号
     */
    private String phone;
    /**
     * 喜欢通知状态
     */
    private Boolean likeNotification = true;
    /**
     * 评论通知状态
     */
    private Boolean pinglunNotification = true;
    /**
     * 公告通知状态
     */
    private Boolean gonggaoNotification = true;

}