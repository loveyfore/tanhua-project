package com.tanhua.server.pojo;

import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.bson.types.ObjectId;
    import org.springframework.data.mongodb.core.mapping.Document;

    import java.util.Date;

    /**
     * 时间线表，用于存储发布（或推荐）的数据，每一个用户一张表进行存储
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Document(collection = "quanzi_time_line")
    public class TimeLine implements java.io.Serializable{
        private static final long serialVersionUID = 9096178416317502524L;
        private ObjectId id;

        private Long userId; // 好友id
        private ObjectId publishId; //发布id

        private Long date; //发布的时间

    }