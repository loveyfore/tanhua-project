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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * 环信向指定用户发送消息
     * POST	//{org_name}/{app_name}/messages
     * @param userId
     * @param message
     * @param type
     * @return
     */
    public Boolean sendMessageByUserId(Long userId, String message, String type) {
        /*拼接环信token请求路径*/
        String targetUrl = huanXinConfig.getUrl()+huanXinConfig.getOrgName()+"/"
                +huanXinConfig.getAppName()+"/messages";

        /*获取管理员权限*/
        String token = huanXinTokenService.getToken();
        /*封装请求头*/
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.add("Content-Type","application/json");
        httpHeaders.add("Authorization","Bearer "+token);

        /*封装数据*/

        /*curl -X POST -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'Authorization: Bearer YWMtP5n9zvOQEei7KclxPqJTkgAAAAAAAAAAAAAAAAAAAAGL4CTw6XgR6LaXXVmNX4QCAgMAAAFnXcBpfQBPGgDC09w5IdrfqG_H8_F53VLVTG0_82GXyEF8ZdMCt9-UpQ'
         -d '{"target_type": "users","target": ["user2","user3"],"msg": {"type": "txt","msg": "testmessage"},"from": "user1"}' 'http://a1.easemob.com/easemob-demo/testapp/messages'*/
        Map<String,Object> msg = new HashMap<>();
        msg.put("target_type","users");
        msg.put("target", Arrays.asList(userId));

        Map<String,String> msgMap = new HashMap<>();
        msgMap.put("msg",message);
        msgMap.put("type",type);

        msg.put("msg",msgMap);

        try {
            HttpEntity<String> httpEntity =new HttpEntity<>(OBJECT_MAPPER.writeValueAsString(msg),httpHeaders);

            /*发送post请求*/
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(targetUrl, httpEntity, String.class);

              return responseEntity.getStatusCode().is2xxSuccessful();/*请求成功 200OK*/

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.info("sendMessageError:UserID:{},Message:{},Type:{}",userId,message,type);
        }
        return false;
    }
}
