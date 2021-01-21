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
     * 保存更新用户资料
     * @param userInfo
     * @return
     */
    Boolean updateUserInfo(UserInfo userInfo);

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

    /**
     * 向指定用户发送消息
     * @param userId 指定用户id
     * @param message 消息内容
     * @param type 消息类型 消息类型；txt:文本消息，img：图片消息，loc：位置消息，audio：语音消息，video：视频消息，file：文件消息
     * @return
     */
    Boolean sendMessageByUserId(Long userId, String message, String type);
}