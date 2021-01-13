package com.tanhua.sso.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.sso.mapper.UserInfoMapper;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import com.tanhua.sso.service.HuanXinService;
import com.tanhua.sso.service.UserService;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private HuanXinService huanXinService;



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

    /**
     * 添加环信联系人,建立好友关系
     * @param userId 当前用户id
     * @param friendId 朋友用户id
     * @return
     */
    @Override
    public Boolean addHuanXinContacts(Long userId, Long friendId) {

        return huanXinService.addHuanXinContacts(userId,friendId);
    }

    /**
     * 查询用户详细信息,以昵称模糊查询
     * @param userIdList 用户id集合
     * @param keyword 模糊查询字段
     * @return
     */
    @Override
    public List<UserInfo> queryUserInfoLikeNickName(List<Long> userIdList, String keyword) {
        /*构建查询条件*/
        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("user_id",userIdList);
        if (StringUtils.isNotEmpty(keyword)){
            /*如果该字段不为空才执行对昵称的模糊查询操作*/
            queryWrapper.like("nick_name",keyword);
        }
        return userInfoMapper.selectList(queryWrapper);
    }
}
