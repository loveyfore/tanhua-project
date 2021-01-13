package com.tanhua.server.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.server.api.QuanZiApi;
import com.tanhua.server.api.UsersApi;
import com.tanhua.server.config.AliyunConfig;
import com.tanhua.server.pojo.Announcement;
import com.tanhua.server.pojo.Comment;
import com.tanhua.server.pojo.Users;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.*;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author Administrator
 * @create 2021/1/10 18:52
 */
@Service
@Log4j2
public class IMService {

    @Reference(version = "1.0.0")
    private UsersApi usersApi;

    @Autowired
    private UserService userService;

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private AliyunConfig aliyunConfig;

    @Autowired
    private AnnouncementsService announcementsService;

    /**
     * 添加联系人-好友
     * 好友关系写入MongoDB
     * 好友关系注册到环信
     * @param friendId
     * @return
     */
    public Boolean addContacts(Long friendId) {
        /**
         * 1. 获取当前的登录用户 拿到userId
         * 2. 调用dubbo服务 tanhua_users 数据添加
         * 3. 调用sso服务，进行环信的好友添加
         */

        User user = UserThreadLocal.get();

        Users users = new Users();
        users.setUserId(user.getId());
        users.setFriendId(friendId);

        log.info("addContactsUserId:{}",user.getId());
        log.info("addContactsFriendId:{}",friendId);

        boolean addToMongo = usersApi.saveUsers(users);
        /*如果数据库建立好友关系成功,执行环信好友关系的注册*/
        if (addToMongo){
            return userService.addHuanXinContacts(user.getId(),friendId);
        }
        return false;
    }

    /**
     * 查询联系人列表
     * @param pageNum 页
     * @param pageSize 条
     * @param keyword 查询关键字 --模糊查询
     * @return
     */
    public PageResult queryContactsList(Integer pageNum, Integer pageSize, String keyword) {
        /**
         * 1. 获取当前登录用户
         * 2. 调用dubbo服务 ，获取 好友信息
         * 3. 好友的id列表，根据id列表去sso系统 查询用户信息
         * 4. 包装为 List<Contacts>
         */

        User user = UserThreadLocal.get();

        PageResult pageResult =new PageResult();
        pageResult.setCounts(0);/*总记录数暂无*/
        pageResult.setPages(0);/*总页数暂无*/
        pageResult.setPage(pageNum);
        pageResult.setPagesize(pageSize);

        /*用来存储查询返回users数据--users表存储当前用户与其他用户的关系,好友*/
        List<Users> usersList = null;
        /*关键字为空使用分页查询,关键字不为空使用id查询*/
        if (StringUtils.isEmpty(keyword)){
            PageInfo<Users> pageInfo = usersApi.queryAllUsersList(user.getId(), pageNum, pageSize);
            usersList=pageInfo.getRecords();
        }else {
            usersList = usersApi.queryAllUsersList(user.getId());
        }

        if (usersList==null){
            /*数据为空*/
            return pageResult;
        }

        /*获取当前用户的所有联系人id*/
        List<Long> friendIds = new ArrayList<>();
        for (Users users : usersList) {
            friendIds.add(users.getFriendId());
        }

        /*查询UserInfo详细信息*/
        List<UserInfo> userInfoList = userService.queryUserInfoLikeNickName(friendIds, keyword);

        /*存放封装结果*/
        List<Contacts> contactsList =new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {

            Contacts contacts = new Contacts();
            contacts.setAge(userInfo.getAge());
            /*性别为枚举*/
            contacts.setGender(userInfo.getSex().name().toLowerCase());
            contacts.setNickname(userInfo.getNickName());
            contacts.setUserId(String.valueOf(userInfo.getId()));
            contacts.setAvatar(userInfo.getLogo());
            contacts.setCity(StringUtils.substringBefore(userInfo.getCity(),"-"));

            contactsList.add(contacts);
        }
        pageResult.setItems(contactsList);
        return pageResult;
    }

    /**
     * 查询点赞列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult queryLikesList(Integer pageNum, Integer pageSize) {
        return fillPageResult(pageNum, pageSize,CommentTypeEnum.LIKE);
    }

    /**
     * 查询喜欢列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult queryLovesList(Integer pageNum, Integer pageSize) {
        return fillPageResult(pageNum, pageSize,CommentTypeEnum.LOVE);
    }

    /**
     * 查询评论列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult queryCommentList(Integer pageNum, Integer pageSize) {
        return fillPageResult(pageNum, pageSize,CommentTypeEnum.COMMENT);
    }


    /**
     *  填充PageResult数据
     * @param pageNum 页
     * @param pageSize 条
     * @param commentTypeEnum 查询类型  点赞  评论   喜欢
     * @return
     */
    private PageResult fillPageResult(Integer pageNum, Integer pageSize,CommentTypeEnum commentTypeEnum) {
        /**
         * 获取用户
         * 根据UserId  查询评论列表quanzi_comment
         * 获取评论用户id列表
         * 查询用户详细信息
         * 封装数据
         */

        /*初始化返回数据*/
        PageResult pageResult = new PageResult();
        pageResult.setCounts(0);/*总记录数暂不提供*/


        User user = UserThreadLocal.get();
        /*查询该用户作品,被其他用户点赞操作的所有数据  comment表*/
        PageInfo<Comment> pageInfo = quanZiApi.queryCommentListByUser(user.getId(), commentTypeEnum.getCode(), pageNum, pageSize);
        List<Comment> records = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(records)){
            return pageResult;
        }

        /*对动态,小视频,评论 操作的用户id集合*/
        List<Long> commentUserIds = new ArrayList<>();
        for (Comment record : records) {
                commentUserIds.add(record.getUserId());
        }

        List<UserInfo> userInfoList = userService.queryUserInfoByUserIdList(commentUserIds);
        Map<Long,UserInfo> userInfoMap = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            userInfoMap.put(userInfo.getUserId(),userInfo);
        }

        /*结果封装*/
        List<MessageLike> messageLikeList =new ArrayList<>();

        /*数据封装*/
        for (Comment record : records) {
            UserInfo userInfo = userInfoMap.get(record.getUserId());

            MessageLike messageLike = new MessageLike();
            messageLike.setId(record.getId().toHexString());
            messageLike.fillAvatar(aliyunConfig.getUrlPrefix(),userInfo.getLogo());
            messageLike.setNickname(userInfo.getNickName());
            /*时间格式化*/
            messageLike.setCreateDate(new DateTime(record.getCreated()).toString("yyyy-MM-dd HH:mm"));

            messageLikeList.add(messageLike);

        }
        pageResult.setItems(messageLikeList);
        return pageResult;
    }

    /**
     * 查询公告列表
     * @param pageNum 页
     * @param pageSize 条
     * @return
     */
    public PageResult queryAnnouncementsList(Integer pageNum, Integer pageSize) {
        /**
         * 查询数据
         * 封装数据
         * 返回
         */

        PageResult pageResult = new PageResult();
        pageResult.setPage(pageNum);
        pageResult.setPagesize(pageSize);
        pageResult.setPages(0);/*总页数暂无*/
        pageResult.setCounts(0);/*总记录数暂无*/

        List<Announcement> announcements = announcementsService.queryAnnouncementsList(pageNum, pageSize);

        if (CollectionUtils.isEmpty(announcements)){
            return pageResult;
        }

        List<MessageAnnouncement> messageAnnouncementList =new ArrayList<>();

        for (Announcement announcement : announcements) {

            MessageAnnouncement messageAnnouncement = new MessageAnnouncement();
            messageAnnouncement.setId(String.valueOf(announcement.getId()));
            messageAnnouncement.setTitle(announcement.getTitle());
            messageAnnouncement.setDescription(announcement.getDescription());
            messageAnnouncement.setCreateDate(new DateTime(announcement.getCreated()).toString("yyyy年MM月dd日 HH:mm:ss"));

            messageAnnouncementList.add(messageAnnouncement);
        }

        pageResult.setItems(messageAnnouncementList);

        return pageResult;
    }
}
