package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.server.mapper.SettingsMapper;
import com.tanhua.server.pojo.Settings;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Author Administrator
 * @create 2021/1/21 11:54
 */
@Service
@Log4j2
public class SettingsService {

    @Autowired
    private SettingsMapper settingsMapper;

    /**
     * 根据用户id查询用户通知设置
     * @param userId
     * @return
     */
    public Settings querySettings(Long userId){
        QueryWrapper<Settings> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return settingsMapper.selectOne(queryWrapper);
    }

    /**
     * 保存设置
     * @param settings
     * @return
     */
    public Boolean saveSettings(Settings settings){
        return settingsMapper.insert(settings)>0;
    }

    public Boolean updateSettings(Settings settings){
        return settingsMapper.updateById(settings)>0;
    }


}
