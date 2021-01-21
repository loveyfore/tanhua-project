package com.tanhua.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
//CompoundIndex 复合索引，location自段加了索引，2dsphere 位置索引，数据量大时 性能明显 维持在0.5s左右
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_location")
@CompoundIndex(name = "location_index", def = "{'location': '2dsphere'}")
public class UserLocation implements java.io.Serializable{

    private static final long serialVersionUID = 4508868382007529970L;

    @Id
    private ObjectId id;
    /**
     * 用户id
     */
    @Indexed
    private Long userId;
    /**
     * x:经度 y:纬度
     */
    private GeoJsonPoint location;
    /**
     * 位置描述
     */
    private String address;
    /**
     * 创建时间
     */
    private Long created;
    /**
     * 更新时间
     */
    private Long updated;
    /**
     * 上次更新时间
     */
    private Long lastUpdated;

}
