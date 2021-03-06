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
     * 验证用户token合法性,反回token中存储的用户信息
     * @param token
     * @return
     */
    public User queryToken(String token){
        return ssoApi.queryToken(token);
    }


    /**
     * 根据用户id查询用户详细信息
     * 可用于查询佳人详细信息
     * @param userId
     * @return
     */
    public UserInfo queryUserInfoByUserId(Long userId) {
        return ssoApi.queryUserInfoByUserId(userId);
    }


    /**
     * 根据id集合,按条件筛选数据  //TODO 改功能未实现 ,sso层查询条件已经注释
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


    /**
     * 方法重载
     * 根据id集合,查询用户详细信息
     * @param userIdList
     * @return
     */
    public List<UserInfo> queryUserInfoByUserIdList(List<Long> userIdList) {
        return this.queryUserInfoByUserIdList(userIdList, null, null, null, null);
    }

    /**
     * 方法重载
     * 根据id集合,性别
     * @param userIdList 用户id集合
     * @param gender 性别
     * @return
     */
    public List<UserInfo> queryUserInfoByUserIdList(List<Long> userIdList,String gender) {
        return this.queryUserInfoByUserIdList(userIdList, null, null, null, gender);
    }

    /**
     * 保存更新,用户资料
     * @param userInfo
     * @return
     */
    public Boolean updateUserInfo(UserInfo userInfo){
        return this.ssoApi.updateUserInfo(userInfo);
    }

    /**
     * 环信好友添加 好友建立关系
     * @param userId
     * @param friendId
     * @return
     */
    public Boolean addHuanXinContacts(Long userId,Long friendId){
        return ssoApi.addHuanXinContacts(userId,friendId);
    }

    /**
     * 模糊-查询用户UserInfo详细信息
     * @param userIdList
     * @param keyword
     * @return
     */
    List<UserInfo> queryUserInfoLikeNickName(List<Long> userIdList,String keyword){
        return ssoApi.queryUserInfoLikeNickName(userIdList,keyword);
    }

    /**
     * 环信向指定用户发送消息
     * @param userId 指定用户id
     * @param message 消息内容
     * @param type 消息类型 	消息类型；txt:文本消息，img：图片消息，loc：位置消息，audio：语音消息，video：视频消息，file：文件消息
     * @return
     */
    public Boolean replyQuestionMessage(Long userId,String message,String type){
        return ssoApi.sendMessageByUserId(userId,message,type);
    }
}
