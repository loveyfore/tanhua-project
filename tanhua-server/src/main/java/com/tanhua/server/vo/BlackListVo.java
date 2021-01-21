package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类用于返回黑名单列表
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlackListVo {

    /**
     * id
     */
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

}