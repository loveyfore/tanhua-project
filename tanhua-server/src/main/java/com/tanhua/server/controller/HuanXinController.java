package com.tanhua.server.controller;

import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.HuanXinUser;
import com.tanhua.sso.pojo.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Administrator
 * @create 2021/1/9 21:53
 */
@RestController
@RequestMapping("huanxin")
public class HuanXinController {

    /**
     * GET
     * 环信用户信息
     * /huanxin/user
     * @return
     */
    @GetMapping("/user")
    public ResponseEntity<Object> queryHuanXinUser(){
        User user = UserThreadLocal.get();

        HuanXinUser huanXinUser = new HuanXinUser();
        huanXinUser.setUsername(user.getId().toString());
        huanXinUser.setPassword(DigestUtils.md5Hex(user.getId() + "_itcast_tanhua"));

        return ResponseEntity.ok(huanXinUser);

    }
}
