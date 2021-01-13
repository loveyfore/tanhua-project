package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HuanXinUser {

    /**
     * 注册用户名
     */
    private String username;
    /**
     * 注册密码
     */
    private String password;

}