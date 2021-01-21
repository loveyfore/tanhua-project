package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类用于封装搜附近用户数据的返回
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearUserVo {

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 昵称
     */
    private String nickname;

}