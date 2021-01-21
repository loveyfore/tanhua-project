package com.tanhua.server.api;

import com.tanhua.server.pojo.Visitors;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/13 19:13
 */
public interface VisitorsApi {

    /**
     * 保存来访记录
     * @param visitors
     * @return
     */
    String saveVisitor(Visitors visitors);

    /**
     * 查询访客信息
     * @param userId 用户id
     * @param pageSize 记录数
     * @return
     */
    List<Visitors> topVisitor(Long userId,Integer pageSize);


    /**
     * 按时间倒序,查询最近访客信息
     * @param userId 用户id
     * @param date 时间戳
     * @return
     */
    List<Visitors> topVisitor(Long userId,Long date);

    /**
     * 查询访客列表-谁看过我
     */
    List<Visitors> visitorList(Long userId,Integer pageNum,Integer pageSize);

}
