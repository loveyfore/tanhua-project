package com.tanhua.sso.controller;

import com.tanhua.sso.service.SmsService;
import com.tanhua.sso.vo.ErrorResult;
import com.tanhua.sso.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author Administrator
 * @create 2020/12/30 23:12
 */
@RestController
@RequestMapping("user")
public class SmsController {

    @Autowired
    private SmsService smsService;

    /**
     * POST
     * 手机号登录
     * /user/login
     * @param paramMap
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Object> login(@RequestBody Map<String,String> paramMap){
        /*获取手机号*/
        String phone = paramMap.get("phone");

        /*发送短信*/
        Result result = smsService.sendCode(phone);

        if (result.getSuccess()){
            /*发送成功*/
            return ResponseEntity.ok(null);
        }

        /*由于实体类使用的@build注解,所以这里可以使用build的方式来构建ErrorResult*/
        ErrorResult errorResult = ErrorResult
                .builder()
                .errCode("000000")
                .errMessage(result.getMsg())
                .build();

        /*500服务器错误*/
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }
}
