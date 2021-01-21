package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类用于封装用户的详细信息
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVo {

    private Long id;
    /**
     *  头像
     */
    private String avatar;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 生日 2019-09-11
     */
    private String birthday;
    /**
     * 年龄
     */
    private String age;
    /**
     * 性别 man woman
     */
    private String gender;
    /**
     * 城市
     */
    private String city;
    /**
     * 学历 枚举: 本科,硕士,双硕,博士,双博
     */
    private String education;
    /**
     * 月收入 枚举: 5k,8K,15K,35K,55K,80K,100K
     */
    private String income;
    /**
     * 行业 枚举: IT行业,服务行业,公务员
     */
    private String profession;
    /**
     * 婚姻状态（0未婚，1已婚）
     */
    private Integer marriage;


    /**
     * 方法用于填充用户头像路径,否则头像无法正常显示,因为库中是URI路径
     * @param urlPrefix 阿里oss链接前缀
     * @param userLogoUri 用户头像路径
     */
    public void fillAvatar(String urlPrefix, String userLogoUri) {
        if (!userLogoUri.startsWith("http")){
            this.avatar=urlPrefix+userLogoUri;
        }

        /*路径正常,不拼接*/
        this.avatar=userLogoUri;
    }
}