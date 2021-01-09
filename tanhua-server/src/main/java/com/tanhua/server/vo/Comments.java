package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 评论->包含用户信息和评论信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comments implements Serializable {

    /**
     * 评论id
     */
    private String id;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 评论
     */
    private String content;
    /**
     * 评论时间: 08:27
     */
    private String createDate;
    /**
     * 点赞数
     */
    private Integer likeCount;
    /**
     *是否点赞（1是，0否）
     */
    private Integer hasLiked;

}