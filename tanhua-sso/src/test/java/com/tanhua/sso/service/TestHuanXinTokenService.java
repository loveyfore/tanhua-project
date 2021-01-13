package com.tanhua.sso.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author Administrator
 * @create 2021/1/9 19:56
 * 环信获取管理员权限测试
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TestHuanXinTokenService {

    @Autowired
    private HuanXinTokenService huanXinTokenService;

    @Test
    public void getToken(){
        String token = huanXinTokenService.getToken();
        System.out.println(token);
    }
}
