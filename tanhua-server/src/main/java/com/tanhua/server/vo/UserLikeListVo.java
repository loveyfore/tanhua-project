package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLikeListVo {

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
     * 城市
     */
    private String city;
    /**
     * 学历
     */
    private String education;
    /**
     * 婚姻状态（0未婚，1已婚）
     */
    private Integer marriage;
    /**
     * 匹配度,缘分值
     */
    private Integer matchRate;
    /**
     * 是否已经喜欢
     */
    private boolean alreadyLove;

}