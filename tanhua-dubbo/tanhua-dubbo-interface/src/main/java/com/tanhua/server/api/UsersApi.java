package com.tanhua.server.api;

import com.tanhua.server.pojo.Users;
import com.tanhua.server.vo.PageInfo;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/10 8:36
 */
public interface UsersApi {

    /**
     * 保存好友--添加好友
     * @param users
     * @return
     */
    boolean saveUsers(Users users);

    /**
     * 根据用户id查询Users列表
     * 查询该用户的好友关系
     * @param userId 用户id
     * @return
     */
    List<Users> queryAllUsersList(Long userId);

    /**
     * 根据用户id查询Users列表--分页查询
     * 查询该用户的好友关系
     * @param userId 用户id
     * @param pageNum 页
     * @param pageSize 条
     * @return
     */
    PageInfo<Users> queryAllUsersList(Long userId, Integer pageNum, Integer pageSize);

}
