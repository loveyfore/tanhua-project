package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 为返回联系人封装实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contacts {
    /**
     * id
     */
    private Long id;
    /**
     * 联系人Userid
     */
    private String userId;
    /**
     * 联系人头像
     */
    private String avatar;
    /**
     * 联系人昵称
     */
    private String nickname;
    /**
     * 联系人性别
     */
    private String gender;
    /**
     * 联系人年龄
     */
    private Integer age;
    /**
     * 联系人所在城市
     */
    private String city;

}