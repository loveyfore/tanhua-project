package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.server.api.QuanZiApi;
import com.tanhua.server.pojo.Publish;
import com.tanhua.sso.pojo.User;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Administrator
 * @create 2021/1/12 14:23
 */
@Service
@Log4j2
public class QuanziMQService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;


    /**
     * 向MQ发送操作-消息
     * 使用异步的方式取发送消息
     * @param type 1-发动态，2-浏览动态， 3-点赞， 4-喜欢， 5-评论，6-取消点赞，7-取消喜欢
     * @param publishId
     * @return
     */
    @Async("asyncServiceExecutor") /*异步执行该方法*/
    public void sendMessage(User user,Integer type,String publishId){

        try {
            /*查询操作的该条动态*/
            Publish publish = quanZiApi.queryPublishById(publishId);

            /*封装消息内容*/
            Map<String,Object> message = new HashMap<>();
            message.put("userId",user.getId());
            message.put("date",System.currentTimeMillis());
            message.put("pid",publish.getPid());
            message.put("publishId",publishId);
            message.put("type",type);

            log.info("----------------ThreadMessage:Start!----------------");
            log.info("操作类型:{},操作动态的id:{}",type,publishId);
            /*转换成JSON字符串并发送*/
            rocketMQTemplate.convertAndSend("tanhua-quanzi",message);
            log.info("ThreadMessage:Success!");
        } catch (Exception e) {
            log.info("消息发送失败了,参数信息为:type={},publishId={},报错信息为:{}",type,publishId,e);
        }
    }
}
