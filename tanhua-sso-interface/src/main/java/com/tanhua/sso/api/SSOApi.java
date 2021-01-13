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

    /**
     * 添加联系人到环信-建立好友关系添加好友
     * @param userId 当前用户id
     * @param friendId 朋友用户id
     * @return
     */
    Boolean addHuanXinContacts(Long userId,Long friendId);

    /**
     * 查询用户详细信息,以昵称模糊查询
     * @param userIdList 用户id集合
     * @param keyword 模糊查询字段
     */
    List<UserInfo> queryUserInfoLikeNickName(List<Long> userIdList,String keyword);
}