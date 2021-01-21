package com.tanhua.sso.service;

import com.tanhua.sso.pojo.User;
import com.tanhua.sso.vo.Result;
import com.tanhua.sso.vo.VerifationResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author Administrator
 * @create 2021/1/19 23:00
 */
@Service
@Log4j2
public class UsersService {

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    /**
     * 更新用户头像
     * @param file
     * @param token
     * @return
     */
    public Boolean uploadHead(MultipartFile file, String token) {
        return userService.uploadHead(file,token);
    }

    /**
     * 修改手机号- 1 发送短信验证码
     * @param token 令牌
     * @return
     */
    public boolean sendVerificationCode(String token) {
        /**
         * 校验token
         * 发送验证码
         * 验证码存储redis,设置过期时间
         */

        User user = userService.checkToken(token);
        if (user==null){
            /*解析失败,用户未登录*/
            return false;
        }

        Result modifyPhone= smsService.sendCode(user.getMobile(), "MODIFY_PHONE_");

        return modifyPhone.getSuccess();
    }

    /**
     * 校验验证码
     * @param token
     * @param verificationCode
     * @return
     */
    public VerifationResult checkVerificationCode(String token, String verificationCode) {
        /**
         * 校验token
         * 获取redis中验证码 校验
         * 返回结果
         */

        User user = userService.checkToken(token);
        if (user==null){
            /*解析失败,用户未登录*/
            return new VerifationResult(false);
        }


        String code = redisTemplate.opsForValue().get("MODIFY_PHONE_" + user.getMobile());
        if (StringUtils.isEmpty(code)) {
            return new VerifationResult(false);
        }
        /*如果不相等*/
        if (!StringUtils.equals(verificationCode,code)){
            return new VerifationResult(false);
        }


        return new VerifationResult(true);

    }

    /**
     * 更新手机号
     * @param token
     * @param phone
     * @return
     */
    public Boolean updatePhoneCode(String token, String phone) {

        User user = userService.checkToken(token);
        if (user==null){
            /*解析失败,用户未登录*/
            return false;
        }

        return userService.updatePhoneCode(user.getId(),phone);
    }
}
