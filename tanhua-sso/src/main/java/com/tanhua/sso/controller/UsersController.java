package com.tanhua.sso.controller;

import com.tanhua.sso.service.UsersService;
import com.tanhua.sso.vo.ErrorResult;
import com.tanhua.sso.vo.VerifationResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Author Administrator
 * @create 2021/1/19 22:48
 */
@RestController
@Log4j2
@RequestMapping("users")
public class UsersController {

    @Autowired
    private UsersService usersService;

    /**
     * POST
     * 用户资料 - 保存头像
     * /users/header
     * @param file 头像文件
     * @param token 令牌  sso无全局token
     * @return
     */
    @PostMapping("/header")
    public ResponseEntity<Object> updateUserLogo(@RequestParam("headPhoto") MultipartFile file,
                                                    /*用户token,携带用户的个人信息*/
                                                 @RequestHeader("Authorization") String token){

            Boolean saveUserInfo =usersService.uploadHead(file,token);
            if (saveUserInfo){
                return ResponseEntity.ok(null);
            }

        //@Build注解 可以用build方式来构建ErrorResult
        ErrorResult errorResult = ErrorResult
                .builder()
                .errCode("000000")
                .errMessage("请上传正确的头像!")
                .build();

        //500错误 服务器错误
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }

    /**
     * POST
     * 修改手机号- 1 发送短信验证码
     * /users/phone/sendVerificationCode
     * @param token 令牌  sso无全局token
     * @return
     */
    @PostMapping("/phone/sendVerificationCode")
    public ResponseEntity<Object> sendVerificationCode(@RequestHeader("Authorization") String token){

        boolean isSuccess =  this.usersService.sendVerificationCode(token);
        if (isSuccess){
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * POST
     * 修改手机号 - 2 校验验证码
     * /users/phone/checkVerificationCode
     * @param token 令牌  sso无全局token
     * @return
     */
    @PostMapping("/phone/checkVerificationCode")
    public ResponseEntity<Object> checkVerificationCode(@RequestHeader("Authorization") String token,
                                                        @RequestBody Map<String,String> params){
        String verificationCode = params.get("verificationCode");
        VerifationResult verifationResult =  this.usersService.checkVerificationCode(token,verificationCode);
        if (verifationResult!=null){
            return ResponseEntity.ok(verifationResult);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * POST
     * 修改手机号 - 3 保存
     * /users/phone
     * @param token 令牌  sso无全局token
     * @return
     */
    @PostMapping("/phone")
    public ResponseEntity<Object> updatePhoneCode(@RequestHeader("Authorization") String token,
                                                        @RequestBody Map<String,String> params){
        String phone = params.get("phone");

        Boolean updatePhoneCode =  this.usersService.updatePhoneCode(token,phone);
        if (updatePhoneCode){
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
