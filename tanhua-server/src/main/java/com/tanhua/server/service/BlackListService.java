package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.server.mapper.BlackListMapper;
import com.tanhua.server.pojo.BlackList;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/21 13:58
 */
@Service
@Log4j2
public class BlackListService {

    @Autowired
    private BlackListMapper blackListMapper;

    /**
     * 根据用户id,查询该用户的黑名单列表
     * 分页查询
     * @param userId
     * @return
     */
    public IPage<BlackList> queryBlackListByUserId(Long userId,Integer pageNum,Integer pageSize){
        QueryWrapper<BlackList> queryWarpper =new QueryWrapper<>();
        queryWarpper.eq("user_id",userId);
        /*按时间排序*/
        queryWarpper.orderByDesc("created");

        /*mybatis-分页,springboot必须设置配置类,否则分页将不会生效*/  //TODO 注意!
        IPage<BlackList> page =new Page<>(pageNum,pageSize);

        return blackListMapper.selectPage(page, queryWarpper);
    }

    /**
     * 根据用户id,删除黑名单用户
     * @param userId
     * @return
     */
    public Boolean deleteBlackListUser(Long userId,Long blackUserId){
        QueryWrapper<BlackList> queryWarpper =new QueryWrapper<>();
        queryWarpper.eq("user_id",userId);
        queryWarpper.eq("black_user_id",blackUserId);
        return blackListMapper.delete(queryWarpper)>0;
    }
}
