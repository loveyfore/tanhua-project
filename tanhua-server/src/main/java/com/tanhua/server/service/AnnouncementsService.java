package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.server.mapper.AnnouncementsMapper;
import com.tanhua.server.pojo.Announcement;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/11 21:13
 * 公告业务层
 */
@Service
@Log4j2
public class AnnouncementsService {

    @Autowired
    private AnnouncementsMapper announcementsMapper;

    public List<Announcement> queryAnnouncementsList(Integer pageNum,Integer pageSize) {

        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created");

        Page<Announcement> page = new Page<>(pageNum, pageSize);

        IPage iPage = announcementsMapper.selectPage(page, queryWrapper);

        return iPage.getRecords();/*分页记录列表*/
    }
}
