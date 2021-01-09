package com.tanhua.sso.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.sso.mapper.UserInfoMapper;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import com.tanhua.sso.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/2 21:18
 */
@Service(version = "1.0.0")
public class SSOApiImpl implements SSOApi {

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfoMapper  userInfoMapper;



    /**
     * 验证用户token合法性,返回user
     * @param token
     * @return
     */
    @Override
    public User queryToken(String token) {
        return userService.checkToken(token);
    }

    /**
     * 根据用户id查询用户详细信息
     * 查询佳人详细信息
     * @param userId
     * @return
     */
    @Override
    public UserInfo queryUserInfoByUserId(Long userId) {
        QueryWrapper<UserInfo> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("user_id",userId).last("limit 1");

        return userInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 查询推荐用户的详细信息
     * @param userIdList
     * @param age
     * @param city
     * @param education
     * @param sex
     * @return
     */
    @Override
    public List<UserInfo> queryUserInfoByUserIdList(List<Long> userIdList, Integer age, String city, String education, Integer sex) {
        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("user_id",userIdList);


        //因数据问题,忽略非空判断
        //        if (age != null){
        //            queryWrapper.le("age",age);
        //        }
        //        if (StringUtils.isNotEmpty(city)){
        //            queryWrapper.like("city",city);
        //        }
        //        if (StringUtils.isNotEmpty(education)){
        //            queryWrapper.eq("edu",education);
        //        }
        //        if (sex != null){
        //            queryWrapper.eq("sex",sex);
        //        }

        return userInfoMapper.selectList(queryWrapper);
    }
}
