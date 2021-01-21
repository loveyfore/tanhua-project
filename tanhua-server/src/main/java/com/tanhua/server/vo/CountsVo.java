package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对用户资料数据封装返回
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountsVo {

    /**
     * 互相喜欢
     */
    private Long eachLoveCount;
    /**
     * 喜欢
     */
    private Long loveCount;
    /**
     * 粉丝
     */
    private Long fanCount;

}