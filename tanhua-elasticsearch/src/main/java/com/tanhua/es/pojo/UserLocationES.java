package com.tanhua.es.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "tanhua", type = "user_location", shards = 6, replicas = 2)
public class UserLocationES {

    /**
     * 用户id
     */
    @Id
    private Long userId;
    /**
     * x:经度 y:纬度
     */
    @GeoPointField
    private GeoPoint location;

    /**
     * 位置描述
     */
    @Field(type = FieldType.Keyword)
    private String address;
    /**
     * 创建时间
     */
    @Field(type = FieldType.Long)
    private Long created;
    /**
     * 更新时间
     */
    @Field(type = FieldType.Long)
    private Long updated;
    /**
     * 上次更新时间
     */
    @Field(type = FieldType.Long)
    private Long lastUpdated;
}