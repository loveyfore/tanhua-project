package com.tanhua.sso.controller;

import com.tanhua.sso.service.UserService;
import com.tanhua.sso.vo.ErrorResult;
import com.tanhua.sso.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Author Administrator
 * @create 2020/12/30 23:32
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * POST
     * 用户验证码校验
     * /user/loginVerification
     * @param paramMap
     * @return
     */
    @PostMapping("loginVerification")
    public ResponseEntity<Object> loginVerification(@RequestBody Map<String,String> paramMap){
        /*获取用户手机号和验证码*/
        String phone=paramMap.get("phone");
        String code=paramMap.get("verificationCode");
        
        /*调用验证*/
        Result result = userService.loginVerification(phone, code);
        if (result.getSuccess()){
            return ResponseEntity.ok(result.getData());
        }

        //@Build注解 可以用build方式来构建ErrorResult
        ErrorResult errorResult = ErrorResult
                .builder()
                .errCode("000000")
                .errMessage(result.getMsg())
                .build();
        //500错误 服务器错误
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }


    /**
     * POST
     * 新用户--选取头像
     * /user/loginReginfo/head
     * @return
     */
    @PostMapping("loginReginfo/head")
    public ResponseEntity<Object> upload(@RequestParam("headPhoto")MultipartFile file,
                                         /*用户token,携带用户的个人信息*/
                                         @RequestHeader("Authorization")String token){
        boolean isSuccess= userService.uploadHead(file,token);
        if (isSuccess){
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
     * 注册-填写用户资料
     * /user/loginReginfo
     * @param paramMap
     * @return
     */
    @PostMapping("loginReginfo")
    public ResponseEntity<Object> saveUserInfo(@RequestBody Map<String, String> paramMap,
                                               @RequestHeader("Authorization") String token){
        boolean isSave=userService.saveUserInfo(paramMap,token);
        if (isSave){
            return ResponseEntity.ok(null);
        }

        //@Build注解 可以用build方式来构建ErrorResult
        ErrorResult errorResult = ErrorResult
                .builder()
                .errCode("000000")
                .errMessage("保存失败!")
                .build();
        //500错误 服务器错误
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }
}
