package com.tanhua.es.service;

import com.tanhua.es.pojo.UserLocationES;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserLocationESService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 更新用户的实时地理位置信息
     * @param userId
     * @param longitude
     * @param latitude
     * @param address
     * @return
     */
    public boolean updateLocation(Long userId, Double longitude, Double latitude, String address){
        /**
         * 先去查询 es中 此用户是否存在位置信息
         * 如果不存在 新增 存在就更新
         */
        //在使用之前，首先判断是否有索引，无索引，进行创建
        if (!elasticsearchTemplate.indexExists(UserLocationES.class)){
            elasticsearchTemplate.createIndex(UserLocationES.class);
        }
        //判断type索引是否存在 不存在就创建，没有type es无法查询
        if (!elasticsearchTemplate.typeExists("tanhua","user_location")){
            elasticsearchTemplate.putMapping(UserLocationES.class);
        }

        GetQuery getQuery = new GetQuery();
        getQuery.setId(userId.toString());
        UserLocationES userLocationES = this.elasticsearchTemplate.queryForObject(getQuery,UserLocationES.class);

        if (userLocationES == null){
            //新增
            userLocationES = new UserLocationES();
            userLocationES.setLocation(new GeoPoint(latitude, longitude));
            userLocationES.setAddress(address);
            userLocationES.setUserId(userId);
            userLocationES.setCreated(System.currentTimeMillis());
            userLocationES.setUpdated(userLocationES.getCreated());
            userLocationES.setLastUpdated(userLocationES.getCreated());
            //保存
            IndexQuery indexQuery = new IndexQueryBuilder().withObject(userLocationES).build();
            this.elasticsearchTemplate.index(indexQuery);
        }else{
                UpdateRequest updateRequest = new UpdateRequest();
                Map<String, Object> map = new HashMap<>();
                map.put("lastUpdated", userLocationES.getUpdated());
                map.put("updated", System.currentTimeMillis());
                map.put("address", address);
                map.put("location", new GeoPoint(latitude, longitude));
                updateRequest.doc(map);
                //编辑
                UpdateQuery updateQuery = new UpdateQueryBuilder()
                        .withId(userId.toString())
                        .withClass(UserLocationES.class)
                        .withUpdateRequest(updateRequest).build();
                this.elasticsearchTemplate.update(updateQuery);
        }
        return true;
    }

    //1. 先要把登录用户的地理位置查出来
    //2. 附近 知道距离，根据距离 以及用户的地理位置 ，进行搜索

    /**
     * 根据用户id查询位置信息
     * @param userId
     * @return
     */
    public UserLocationES queryUserLocation(Long userId){
        GetQuery getQuery = new GetQuery();
        getQuery.setId(userId.toString());
        UserLocationES userLocationES = this.elasticsearchTemplate.queryForObject(getQuery,UserLocationES.class);
        return userLocationES;
    }

    /**
     * 根据经纬度和距离进行附近的人查询
     * @param longitude  经度
     * @param latitude 维度
     * @param distance 距离 单位 米
     * @param page
     * @param pageSize
     * @return
     */
    public List<UserLocationES> queryNearBy(Double longitude, Double latitude, Integer distance,Integer page, Integer pageSize){
        //SearchQuery 是一个接口，要使用实现类才行 NativeSearchQuery是SearchQuery接口的唯一实现类\
        //1. 进行分页查询  withPageable
        PageRequest pageRequest = PageRequest.of(page-1, pageSize);
        //2. 进行排序 按照距离排序 withSort GeoDistanceSortBuilder 距离排序
        GeoDistanceSortBuilder geoDistanceSortBuilder = new GeoDistanceSortBuilder("location", latitude, longitude);
        geoDistanceSortBuilder.unit(DistanceUnit.KILOMETERS); //设置距离的单位
        geoDistanceSortBuilder.order(SortOrder.ASC);// 升序
        //3. 查询附近的人  withQuery 设置查询条件，GeoDistanceQueryBuilder 按照地理位置信息查询
        //中心点，半径，圆(GeoDistanceQueryBuilder)
        GeoDistanceQueryBuilder geoDistanceQueryBuilder = new GeoDistanceQueryBuilder("location");
        geoDistanceQueryBuilder.point(latitude,longitude);//中心点
        geoDistanceQueryBuilder.distance(distance/1000,DistanceUnit.KILOMETERS);//半径
        //bool查询 查询必须满足xx条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(geoDistanceQueryBuilder);

        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withPageable(pageRequest)
                .withSort(geoDistanceSortBuilder)
                .withQuery(boolQueryBuilder)
                .build();

        AggregatedPage<UserLocationES> userLocationES = this.elasticsearchTemplate.queryForPage(nativeSearchQuery, UserLocationES.class);
        List<UserLocationES> content = userLocationES.getContent();
        return content;
    }
}