package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.sso.api.SSOApi;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/2 21:46
 */
@Service
public class UserService {

    /*远程注入*/
    @Reference(version = "1.0.0")
    private SSOApi ssoApi;


    /**
     * 验证用户token合法性,放回token中存储的用户信息
     * @param token
     * @return
     */
    public User queryToken(String token){
        return ssoApi.queryToken(token);
    }


    /**
     * 查询佳人详细信息
     * @param userId
     * @return
     */
    public UserInfo queryUserInfoByUserId(Long userId) {
        return ssoApi.queryUserInfoByUserId(userId);
    }


    /**
     * 根据id集合,查询推荐列表中的用户详细数据
     * @param userIdList
     * @param age
     * @param city
     * @param education
     * @param gender
     * @return
     */
    public List<UserInfo> queryUserInfoByUserIdList(List<Long> userIdList,
                                                    Integer age,
                                                    String city,
                                                    String education,

                                                    String gender) {
        Integer sex = null;
        if (!StringUtils.isEmpty(gender)){
            sex = StringUtils.equals(gender,"man") ? 1:2;
        }
        return ssoApi.queryUserInfoByUserIdList(userIdList,age,city,education,sex);
    }
}
