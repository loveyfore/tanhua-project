package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 返回小视频列表封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoVo {

    private String id;
    private Long userId;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 封面
     */
    private String cover;
    /**
     * 视频URL
     */
    private String videoUrl;
    /**
     * 签名
     */
    private String signature;
    /**
     * 点赞数量
     */
    private Integer likeCount;
    /**
     * 是否已赞（1是，0否）
     */
    private Integer hasLiked;
    /**
     * 是否关注 （1是，0否）
     */
    private Integer hasFocus;
    /**
     * 评论数量
     */
    private Integer commentCount;

    /**
     * 方法用于视频访问地址的拼接,库里存的是URI,要将地址拼接伟URL否则无法访问
     * @param webServerUrl
     * @param videoUrl
     */
    public void fillVideoUrl(String webServerUrl, String videoUrl) {
        if (!videoUrl.startsWith("http")){
            videoUrl=webServerUrl+videoUrl;
        }

        this.videoUrl=videoUrl;

    }

    /**
     * 方法用于视频封面访问地址的拼接,库里存的是URI,要将地址拼接伟URL否则无法访问
     * @param urlPrefix
     * @param picUrl
     */
    public void fillCover(String urlPrefix, String picUrl) {
        if (!picUrl.startsWith("http")){
            picUrl=urlPrefix+picUrl;
        }

        this.cover=picUrl;
    }

    /**
     * 方法用于填充头像的访问地址的拼接,库里存的是URI,要将地址拼接伟URL否则无法访问
     * @param urlPrefix
     * @param logoUrl
     */
    public void fillAvatar(String urlPrefix, String logoUrl) {
        if (!logoUrl.startsWith("http")){
            logoUrl=urlPrefix+logoUrl;
        }

        this.avatar=logoUrl;
    }
}