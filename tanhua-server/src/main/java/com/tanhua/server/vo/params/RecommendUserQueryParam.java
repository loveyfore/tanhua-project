package com.tanhua.server.vo.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendUserQueryParam {

    /**
     * 当前页数
     */
    private Integer page = 1;
    /**
     * 页尺寸
     */
    private Integer pagesize = 10;
    /**
     * 性别 man woman
     */
    private String gender;
    /**
     * 近期登陆时间
     */
    private String lastLogin;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 居住地
     */
    private String city;
    /**
     * 学历
     */
    private String education;
}