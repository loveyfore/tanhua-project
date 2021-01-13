package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tanhua.server.api.VideoApi;
import com.tanhua.server.pojo.Video;
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
 * @create 2021/1/12 22:20
 */
@Service
@Log4j2
public class VideoMQService {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Reference(version = "1.0.0")
    private VideoApi videoApi;


    /**
     * 向MQ发送操作-消息
     * 使用异步的方式取发送消息
     * @param type 1-发动态,3-点赞，5-评论
     * @param videoId
     * @return
     */
    @Async("asyncServiceExecutor") /*异步执行该方法*/
    public void sendMessage(User user, Integer type, String videoId){

        try {
            /*查询操作的该条动态*/
            Video video = videoApi.queryVideoById(videoId);

            /*封装消息内容*/
            Map<String,Object> message = new HashMap<>();
            message.put("userId",user.getId());
            message.put("date",System.currentTimeMillis());
            message.put("vid",video.getVid());
            message.put("videoId",videoId);
            message.put("type",type);

            log.info("----------------ThreadMessage:Start!----------------");
            log.info("操作类型:{},操作视频的id:{}",type,videoId);
            /*转换成JSON字符串并发送*/
            rocketMQTemplate.convertAndSend("tanhua-video",message);
            log.info("ThreadMessage:Success!");
        } catch (Exception e) {
            log.info("消息发送失败了,参数信息为:type={},videoId={},报错信息为:{}",type,videoId,e);
        }
    }
}
