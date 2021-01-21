package com.tanhua.es.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.es.pojo.UserLocationES;
import com.tanhua.es.service.UserLocationESService;
import com.tanhua.server.api.UserLocationApi;
import com.tanhua.server.vo.UserLocationVo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;

import java.util.ArrayList;
import java.util.List;

@Service(version = "1.0.1")
@Log4j2
public class UserLocationESApiImpl implements UserLocationApi {

    @Autowired
    private UserLocationESService userLocationESService;

    private static final BeanCopier u2v = BeanCopier.create(UserLocationES.class, UserLocationVo.class,false);

    public UserLocationVo copy(UserLocationES userLocation){
        if (userLocation == null){
            return null;
        }
        UserLocationVo userLocationVo = new UserLocationVo();
        //进行了相同属性的copy
        u2v.copy(userLocation,userLocationVo,null);
        //不同属性 分别进行设置
        userLocationVo.setLongitude(userLocation.getLocation().getLon());
        userLocationVo.setLatitude(userLocation.getLocation().getLat());
        return userLocationVo;
    }

    public List<UserLocationVo> copyList(List<UserLocationES> userLocationESList){
        List<UserLocationVo> userLocationVoList = new ArrayList<>();
        for (UserLocationES userLocationES : userLocationESList) {
            userLocationVoList.add(copy(userLocationES));
        }
        return userLocationVoList;
    }


    @Override
    public Boolean updateUserLocation(Long userId, Double longitude, Double latitude, String address) {

        return userLocationESService.updateLocation(userId,longitude,latitude,address);
    }

    @Override
    public UserLocationVo queryByUserId(Long userId) {
        UserLocationES userLocationES = this.userLocationESService.queryUserLocation(userId);
        return copy(userLocationES);
    }

    @Override
    public List<UserLocationVo> queryUserFromLocation(Double longitude, Double latitude, Integer range) {
        log.info("es queryNearBy.......");
        List<UserLocationES> userLocationESList = this.userLocationESService.queryNearBy(longitude,latitude,range,1,20);
        return copyList(userLocationESList);
    }
}