package com.tanhua.server.vo;

import com.tanhua.server.pojo.UserLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 由于UserLocation不能序列化，所以要再定义UserLocationVo进行返回数据     该对象无法序列化 GeoJsonPoint
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationVo implements java.io.Serializable {

    private static final long serialVersionUID = 4133419501260037769L;

    private String id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 经度
     */
    private Double longitude;
    /**
     * 维度
     */
    private Double latitude;
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

    public static final UserLocationVo format(UserLocation userLocation) {
        UserLocationVo userLocationVo = new UserLocationVo();
        userLocationVo.setAddress(userLocation.getAddress());
        userLocationVo.setCreated(userLocation.getCreated());
        userLocationVo.setId(userLocation.getId().toHexString());
        userLocationVo.setLastUpdated(userLocation.getLastUpdated());
        userLocationVo.setUpdated(userLocation.getUpdated());
        userLocationVo.setUserId(userLocation.getUserId());
        userLocationVo.setLongitude(userLocation.getLocation().getX());
        userLocationVo.setLatitude(userLocation.getLocation().getY());
        return userLocationVo;
    }

    public static final List<UserLocationVo> formatToList(List<UserLocation> userLocations) {
        List<UserLocationVo> list = new ArrayList<>();
        for (UserLocation userLocation : userLocations) {
            list.add(format(userLocation));
        }
        return list;
    }
}