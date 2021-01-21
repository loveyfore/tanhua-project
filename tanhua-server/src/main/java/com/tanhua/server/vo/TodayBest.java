package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodayBest {

    /**
     * 当前推荐用户id
     */
    private Integer id;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 性别 man woman
     */
    private String gender;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 标签
     */
    private String[] tags;
    /**
     * 缘分值
     */
    private Long fateValue;
}