package com.tanhua.server.utils;

import com.tanhua.sso.pojo.User;


/**
 * @Author Administrator
 * @create 2021/1/4 9:35
 */
public class UserThreadLocal {

    private static final ThreadLocal<User> LOCAL =new ThreadLocal<>();

    /*私有构造,保证不能被new出来---单例*/
    private UserThreadLocal(){}

    /**
     * 设置
     * @param user
     */
    public static void set(User user){
        LOCAL.set(user);
    }

    /**
     * 获取
     * @return
     */
    public static User get(){
        return LOCAL.get();
    }

    /**
     * 删除
     */
    public static void del(){
        LOCAL.remove();
    }
}
