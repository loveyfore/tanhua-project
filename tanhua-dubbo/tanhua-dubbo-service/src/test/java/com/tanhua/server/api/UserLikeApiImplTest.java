package com.tanhua.server.api;

import com.tanhua.server.pojo.UserLike;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author Administrator
 * @create 2021/1/15 21:20
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserLikeApiImplTest {

    @Autowired
    private UserLikeApi userLikeApi;

    @Test
    public void isMutualLikeTest(){
        Boolean aBoolean = userLikeApi.isMutualLike(121L, 20L);
        System.out.println(aBoolean);
    }
}
