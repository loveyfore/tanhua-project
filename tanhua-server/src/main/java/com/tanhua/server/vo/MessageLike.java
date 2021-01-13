package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageLike {

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
     * 操作时间
     */
    private String createDate;


    /**
     * 对头像链接进行填充
     * @param logo
     */
    public void fillAvatar(String urlPrefix,String logo) {
        if (!logo.startsWith("http")){
            logo=urlPrefix+logo;
        }
        this.avatar=logo;
    }
}