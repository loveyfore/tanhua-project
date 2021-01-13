package com.tanhua.sso.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.sso.config.HuanXinConfig;
import com.tanhua.sso.vo.HuanXinUser;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @Author Administrator
 * @create 2021/1/9 20:04
 */
@Service
@Log4j2
public class HuanXinService {

    @Autowired
    private HuanXinConfig huanXinConfig;

    @Autowired
    private HuanXinTokenService huanXinTokenService;

    @Autowired
    private static final ObjectMapper OBJECT_MAPPER =new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;


    /**
     * 为每个用户注册环信用户
     * POST	   /{org_name}/{app_name}/users
     * @param userId
     * @return
     */
    public Boolean register(Long userId){
        /*拼接环信token请求路径*/
        String targetUrl = huanXinConfig.getUrl()+huanXinConfig.getOrgName()+"/"
                +huanXinConfig.getAppName()+"/users";
        /*获取权限*/
        String token = huanXinTokenService.getToken();

        try {
            /*封装请求体-**可以使用List批量注册用户*/
            HuanXinUser huanXinUser = new HuanXinUser(String.valueOf(userId), DigestUtils.md5Hex(userId + "_itcast_tanhua"));
            String body=OBJECT_MAPPER.writeValueAsString(huanXinUser);

            /*封装请求头*/
            HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.add("Content-Type","application/json");
            httpHeaders.add("Authorization","Bearer "+token);

            HttpEntity<String> httpEntity=new HttpEntity(body,httpHeaders);

            /*发起请求*/
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(targetUrl, httpEntity, String.class);

            /*注册成功*/
            return responseEntity.getStatusCodeValue()==200;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 环信添加好友
     * POST   /{org_name}/{app_name}/users/{owner_username}/contacts/users/{friend_username}
     * @param userId
     * @param friendId
     * @return
     */
    public Boolean addHuanXinContacts(Long userId,Long friendId){
        /*拼接环信token请求路径->/{org_name}/{app_name}/users/{owner_username}/contacts/users/{friend_username}*/
        String targetUrl = huanXinConfig.getUrl()+huanXinConfig.getOrgName()+"/"
                +huanXinConfig.getAppName()+"/users/"+userId+"/contacts/users/"+friendId;

        /*获取管理员权限*/
        String token = huanXinTokenService.getToken();

        /*封装请求头*/
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.add("Content-Type","application/json");
        httpHeaders.add("Authorization","Bearer "+token);

        HttpEntity<String> httpEntity =new HttpEntity<>(httpHeaders);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(targetUrl, httpEntity, String.class);

        /*请求成功(200 OK)返回true失败为false*/
        return responseEntity.getStatusCode().is2xxSuccessful();


    }
}
