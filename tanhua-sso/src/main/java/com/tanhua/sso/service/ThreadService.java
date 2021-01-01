package com.tanhua.sso.service;

import com.tanhua.sso.pojo.User;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Administrator
 * @create 2020/12/31 0:28
 */
@Service
@Log4j2
public class ThreadService {
    /**
     * 消息队列
     */
    private RocketMQTemplate rocketMQTemplate;

    @Async("asyncServiceExecutor")
    public void sendMQ(User user) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", user.getId());
            msg.put("date", System.currentTimeMillis());
            rocketMQTemplate.convertAndSend("itcast-tanhua-login", msg);
            log.info("mq send start.......");
            Thread.sleep(5000);
            log.info("mq send success。。。。。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
