package com.tanhua.es;

import com.tanhua.es.pojo.UserLocationES;
import com.tanhua.es.service.UserLocationESService;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestUserLocationService {

    @Autowired
    private UserLocationESService UserLocationESService;

    @Test
    public void testUpdateUserLocation() {
        this.UserLocationESService.updateLocation(1L, 121.512253, 31.24094, "金茂大厦");
        this.UserLocationESService.updateLocation(2L, 121.506377, 31.245105, "东方明珠广播电视塔");
        this.UserLocationESService.updateLocation(10L, 121.508815, 31.243844, "陆家嘴地铁站");
        this.UserLocationESService.updateLocation(12L, 121.511999, 31.239185, "上海中心大厦");
        this.UserLocationESService.updateLocation(25L, 121.493444, 31.240513, "上海市公安局");
        this.UserLocationESService.updateLocation(27L, 121.494108, 31.247011, "上海外滩美术馆");
        this.UserLocationESService.updateLocation(30L, 121.462452, 31.253463, "上海火车站");
        this.UserLocationESService.updateLocation(32L, 121.81509, 31.157478, "上海浦东国际机场");
        this.UserLocationESService.updateLocation(34L, 121.327908, 31.20033, "虹桥火车站");
        this.UserLocationESService.updateLocation(38L, 121.490155, 31.277476, "鲁迅公园");
        this.UserLocationESService.updateLocation(40L, 121.425511, 31.227831, "中山公园");
        this.UserLocationESService.updateLocation(43L, 121.594194, 31.207786, "张江高科");
    }


    @Test
    public void testQuery() {
        List<UserLocationES> userLocationES1 = this.UserLocationESService.queryNearBy(121.512253, 31.24094, 1000, 1, 100);
        userLocationES1.forEach(userLocationES -> {
            System.out.println(userLocationES);
            double distance = GeoDistance.ARC.calculate(31.24094, 121.512253, userLocationES.getLocation().getLat(), userLocationES.getLocation().getLon(), DistanceUnit.METERS);
            System.out.println("距离我 : " + distance + "米");
        });
    }

}