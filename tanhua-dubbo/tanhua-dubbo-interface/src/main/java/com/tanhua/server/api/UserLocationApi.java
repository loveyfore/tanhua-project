package com.tanhua.server.api;

import com.tanhua.server.vo.UserLocationVo;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/14 19:59
 */
public interface UserLocationApi {

    /**
     * 更新用户地理位置信息-前端五分钟会调用一次
     * @param userId 用户id
     * @param longitude 经度
     * @param latitude 纬度
     * @param address 详细地址信息
     * @return
     */
    Boolean updateUserLocation(Long userId, Double longitude, Double latitude, String address);

    /**
     * 查询用户地理位置
     *
     * @param userId 用户id
     * @return
     */
    UserLocationVo queryByUserId(Long userId);

    /**
     * 根据地理位置查询用户
     *
     * @param longitude 经度
     * @param latitude 纬度
     * @param range 查询范围 --米为单位,因为前端距离单位为米
     * @return
     */
    List<UserLocationVo> queryUserFromLocation(Double longitude, Double latitude, Integer range);
}
