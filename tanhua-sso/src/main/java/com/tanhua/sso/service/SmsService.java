package com.tanhua.sso.service;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.sso.vo.Result;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Administrator
 * @create 2020/12/30 20:22
 */
@Log4j2
@Service
public class SmsService {


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private static final ObjectMapper MAPPER=new ObjectMapper();

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 云之讯平台短信发送
     * @param mobile
     * @param code
     * @return
     */
    public Boolean sendSMSYunzhixun(String mobile,String code){
        /*存储向短信平台请求的参数*/
        Map<String,String> params =new HashMap<>();
        params.put("sid","0c71daeb7f24a6ef768cb00b132c5624");
        params.put("token","38c5b2f652edec2cd5d7d26e58ab510e");
        params.put("appid","70cabdc8fa3349429417d0580bcead2f");
        params.put("templateid","564697");
        params.put("mobile",mobile);
        /*生成4位数验证码,多个参数用,号分割*/
        params.put("param",code+","+300);
        /*向平台发送POST请求,携带params map参数*/
        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity("https://open.ucpaas.com/ol/sms/sendsms", params, String.class);
        log.info("平台返回的原始参数{}",stringResponseEntity);

        if(stringResponseEntity.getStatusCode().is2xxSuccessful()){
            String body = stringResponseEntity.getBody();
            log.info("send body{}",body);

            try {
                JsonNode jsonNode = MAPPER.readTree(body);
                /*000000标识发送成功*/
                if (StringUtils.equals(jsonNode.get("code").textValue(),"000000")){
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    /**
     * 发送验证码
     * @param phone
     * @return
     */
    public Result sendCode(String phone){
        /**
         * 处理短信业务
         * 6位数短信验证码
         * 使用平台发送短信
         * 储存redis,登录校验时使用
         *
         */

        //int code = RandomUtils.nextInt(100000, 999999);

        //Boolean flag = this.sendSMSYunzhixun(phone, String.valueOf(code));

        //TODO 方便测试暂时写死变量测试

        boolean flag =true;
        int code=123456;

        if (!flag){
            return new Result(false,"短信发送失败!",null);
        }

        /*验证码存入redis,以便登录时校验,5分钟内有效--方便测试60秒*/
        redisTemplate.opsForValue().set("LOGIN_CODE_"+phone,String.valueOf(code), Duration.ofSeconds(60));

        /*短信发送成功*/
        return new Result(true,"OK",null);
    }

    /**
     * 发送验证码
     * 方法重载,传如redis中获取数据的key
     * @param phone
     * @return
     */
    public Result sendCode(String phone,String redisKeyPrefix){
        /**
         * 处理短信业务
         * 6位数短信验证码
         * 使用平台发送短信
         * 储存redis,登录校验时使用
         *
         */

        //int code = RandomUtils.nextInt(100000, 999999);

        //Boolean flag = this.sendSMSYunzhixun(phone, String.valueOf(code));

        //TODO 方便测试暂时写死变量测试
        boolean flag =true;
        int code=123456;

        if (!flag){
            return new Result(false,"短信发送失败!",null);
        }

        /*验证码存入redis,以便登录时校验,5分钟内有效--方便测试60秒*/
        redisTemplate.opsForValue().set(redisKeyPrefix+phone,String.valueOf(code), Duration.ofSeconds(60));

        /*短信发送成功*/
        return new Result(true,"OK",null);
    }



}
