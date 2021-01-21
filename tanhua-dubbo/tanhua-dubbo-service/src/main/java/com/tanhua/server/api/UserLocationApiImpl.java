package com.tanhua.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.server.pojo.UserLocation;
import com.tanhua.server.vo.UserLocationVo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/14 20:02
 */
@Service(version = "1.0.0")
public class UserLocationApiImpl implements UserLocationApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 更新用户位置信息
     * @param userId 用户id
     * @param longitude 经度   x:经度 y:纬度
     * @param latitude 纬度
     * @param address 详细地址信息
     * @return
     */
    @Override
    public Boolean updateUserLocation(Long userId, Double longitude, Double latitude, String address) {
        /**
         * 先查询数据有的话更新没有的话新建
         *
         */

        Query query=Query.query(Criteria.where("userId").is(userId)).limit(1);
        UserLocation ul = mongoTemplate.findOne(query, UserLocation.class);

        if (ul==null){
            /*新建*/
            UserLocation userLocation = new UserLocation();
            userLocation.setId(ObjectId.get());
            userLocation.setAddress(address);
            userLocation.setCreated(System.currentTimeMillis());
            userLocation.setUpdated(System.currentTimeMillis());
            userLocation.setLastUpdated(System.currentTimeMillis());
            userLocation.setUserId(userId);
            userLocation.setLocation(new GeoJsonPoint(longitude,latitude));
            mongoTemplate.save(userLocation);
        }else {
            /*更新*/
            Update update =new Update();
            update.set("location",new GeoJsonPoint(longitude,latitude));/*更新位置*/
            update.set("updated",System.currentTimeMillis());/*更新日期*/
            update.set("lastUpdated",ul.getUpdated());/*获取上次的更新时间*/
            update.set("address",address);
            mongoTemplate.updateFirst(query,update,UserLocation.class);

        }

        return true;
    }

    /**
     * 根据用户id查询位置
     * @param userId 用户id
     * @return
     */
    @Override
    public UserLocationVo queryByUserId(Long userId) {
        Query query =Query.query(Criteria.where("userId").is(userId));
        /*将数据封装返回*/
        return UserLocationVo.format(mongoTemplate.findOne(query,UserLocation.class));
    }

    /**
     * 根据经纬度,距离,查询
     * @param longitude 经度   x:经度 y:纬度
     * @param latitude 纬度
     * @param range 查询范围 --米为单位,因为前端距离单位为米
     * @return
     */
    @Override
    public List<UserLocationVo> queryUserFromLocation(Double longitude, Double latitude, Integer range) {

        /*根据经纬度定位当前位置,也是中心点*/
        GeoJsonPoint geoJsonPoint = new GeoJsonPoint(longitude, latitude);

        /*生成距离将米转换为千米,也就是半径*/
        Distance distance = new Distance(range / 1000, Metrics.KILOMETERS);/*单位为公里/千米*/

        /*以中心点距离和半径生成一个圆*/
        Circle circle = new Circle(geoJsonPoint, distance);

        /*匹配范围内的数据*/
        Query query=Query.query(Criteria.where("location").withinSphere(circle));//Sphere:球体

        return UserLocationVo.formatToList(mongoTemplate.find(query,UserLocation.class));
    }
}
