package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 谁看过我-访客数据封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorsVo {

    private Long id;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 性别
     */
    private String gender;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 标签   本科...
     */
    private String[] tags;
    /**
     * 分数
     */
    private Integer fateValue;

}