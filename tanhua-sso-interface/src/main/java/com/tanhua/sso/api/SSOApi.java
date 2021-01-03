package com.tanhua.sso.api;

import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;

import java.util.List;

public interface SSOApi {

    /**
     * 验证用户token合法性,返回user
     * @param token
     * @return
     */
    User queryToken(String token);

    /**
     * 查询佳人详细信息
     * @param userId
     * @return
     */
    UserInfo queryUserInfoByUserId(Long userId);

    /**
     * 查询推荐用户的详细信息
     * @param userIdList
     * @param age
     * @param city
     * @param education
     * @param sex
     * @return
     */
    List<UserInfo> queryUserInfoByUserIdList(List<Long> userIdList,
                                             Integer age,
                                             String city,
                                             String education,
                                             Integer sex);
}