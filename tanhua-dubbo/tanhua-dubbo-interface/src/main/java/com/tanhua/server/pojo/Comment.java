package com.tanhua.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 评论表 -评论信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_comment")
public class Comment implements java.io.Serializable{

    private static final long serialVersionUID = -291788258125767614L;

    private ObjectId id;

    /**
     * 发布id
     */
    private ObjectId publishId;
    /**
     * 评论类型，1-点赞，2-评论，3-喜欢
     */
    private Integer commentType;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 评论人
     */
    private Long userId;
    /**
     * 是否为父节点，默认是否
     */
    private Boolean isParent = false;
    /**
     * 父节点id
     */
    private ObjectId parentId;
    /**
     * 发表时间
     */
    private Long created;
    /**
     * 动态,小视频,评论的拥有者
     */
    private Long publishUserId;

}