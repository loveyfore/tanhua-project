package com.tanhua.server.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;

public enum SendMessageTypeEnum implements IEnum<Integer> {
    //1-发动态，2-浏览动态， 3-点赞， 4-喜欢， 5-评论，6-取消点赞，7-取消喜欢

    /**
     * 1发动态
     */
    PUBLISH(1,"发动态"),
    /**
     * 2浏览动态
     */
    WATCH_PUBLISH(2,"浏览动态"),
    /**
     * 3点赞
     */
    LIKE(3,"点赞"),
    /**
     * 4喜欢
     */
    LOVE(4,"喜欢"),
    /**
     * 5评论
     */
    COMMENT(5,"评论"),
    /**
     * 6取消点赞
     */
    CANCEL_LIKE(6,"取消点赞"),
    /**
     * 7取消喜欢
     */
    CANCEL_LOVE(7,"取消喜欢");

    private int value;
    private String desc;

    SendMessageTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.desc;
    }
}