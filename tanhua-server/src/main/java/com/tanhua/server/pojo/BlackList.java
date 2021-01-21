package com.tanhua.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类封装黑名单记录对象
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlackList extends BasePojo {

    /**
     * 编号
     */
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 当前用户的黑名单用户id
     */
    private Long blackUserId;
}