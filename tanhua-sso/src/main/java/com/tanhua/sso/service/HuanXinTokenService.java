package com.tanhua.sso.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.sso.config.HuanXinConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author Administrator
 * @create 2021/1/9 18:56
 */
@Service
@Log4j2
public class HuanXinTokenService {

    @Autowired
    private HuanXinConfig huanXinConfig;

    /*rest模板-发送请求*/
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private static final ObjectMapper OBJECT_MAPPER =new ObjectMapper();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_REDIS_KEY="TOKEN_REDIS_KEY";


    /**
     * 方法用于刷新token
     * redis中获取不到token或者token过期调用该方法
     * @return
     */
    private String refreshToken(){
        /*拼接环信token请求路径->{org_name}/{app_name}/token*/
        String targetUrl = huanXinConfig.getUrl()+huanXinConfig.getOrgName()+"/"
                            +huanXinConfig.getAppName()+"/token";

        /*封装请求参数RequestBody(JSON)*/
        Map<String, String> params = new HashMap<>();
        params.put("grant_type","client_credentials"); /*固定写法*/
        params.put("client_id",huanXinConfig.getClientId());
        params.put("client_secret",huanXinConfig .getClientSecret());

        /**
         * 请求环信接口
         * targetUrl:请求路径
         * params:请求参数JSON
         * String.class:返回参数类型
         */
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(targetUrl,params, String.class);

        log.info("环信StatusCode:{}",responseEntity.getStatusCodeValue());
        if (responseEntity.getStatusCodeValue() !=200){
            /*请求失败*/
            return null;
        }


        try {
            /*获取返回数据*/
            String body = responseEntity.getBody();
            JsonNode jsonNode = OBJECT_MAPPER.readTree(body);
            /*token*/
            String accessToken = jsonNode.get("access_token").asText();
            /*token过期时间-环信默认6天,这里提前一天过期  86400s=1day*/
            Long expiresIn = jsonNode.get("expires_in").asLong()-86400;

            /*避免重复获取token,使用redis保存*/
            if (StringUtils.isEmpty(accessToken)){
                return null;
            }
            redisTemplate.opsForValue().set(TOKEN_REDIS_KEY,accessToken,expiresIn,TimeUnit.SECONDS);

            return accessToken;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取环信token(管理员权限)
     * @return
     */
    public String getToken(){

        /*先从redis中获取*/
        String token = redisTemplate.opsForValue().get(TOKEN_REDIS_KEY);
        if (StringUtils.isBlank(token)){
            return this.refreshToken();
        }
        log.info("huanxinMessage:redis中取出了token");
        return token;
    }
}
