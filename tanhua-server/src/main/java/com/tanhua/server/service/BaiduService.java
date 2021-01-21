package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.server.api.UserLocationApi;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.sso.pojo.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * @Author Administrator
 * @create 2021/1/14 20:27
 */
@Service
@Log4j2
public class BaiduService {

    @Reference(version = "1.0.0")
    private UserLocationApi userLocationApi;
    /**
     * 更新用户地理位置
     * @param longitude 经度
     * @param latitude 纬度
     * @param address 地址
     * @return
     */
    public Boolean updateLocation(Double longitude, Double latitude, String address) {

        try {
            User user = UserThreadLocal.get();
            return userLocationApi.updateUserLocation(user.getId(), longitude, latitude, address);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
