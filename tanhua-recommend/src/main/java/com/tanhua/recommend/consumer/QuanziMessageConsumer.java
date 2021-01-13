package com.tanhua.recommend.consumer;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.recommend.pojo.RecommendQuanZi;
import com.tanhua.recommend.vo.QuanziMsg;
import com.tanhua.server.api.QuanZiApi;
import com.tanhua.server.pojo.Publish;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @Author Administrator
 * @create 2021/1/12 16:37
 */
@Log4j2
@Component
@RocketMQMessageListener(consumerGroup = "tanhua-quanzi-consumer",topic = "tanhua-quanzi")
public class QuanziMessageConsumer implements RocketMQListener<String> {

    private static final ObjectMapper OBJECT_MAPPER =new ObjectMapper();

    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void onMessage(String message) {
        log.info("RocketMQConsumerDynamicMessage:{}",message);

        try {
            /*解析消息数据*/
            QuanziMsg msg = OBJECT_MAPPER.readValue(message, QuanziMsg.class);

            /*数据封装*/
            RecommendQuanZi recommendQuanZi = new RecommendQuanZi();
            recommendQuanZi.setId(ObjectId.get());
            recommendQuanZi.setPublishId(msg.getPid());
            recommendQuanZi.setDate(msg.getDate());
            recommendQuanZi.setUserId(msg.getUserId());

            /*分数计算*/
            //分数 需要根据规则进行计算
            //- 浏览 +1
            //- 点赞 +5
            //- 喜欢 +8
            //- 评论 + 10
            //- 文字长度：50以内1分，50~100之间2分，100以上3分
            //- 图片个数：每个图片一分


            switch (msg.getType()){
                case 1://发动态
                    Publish publish = quanZiApi.queryPublishById(msg.getPublishId());
                    int length = StringUtils.length(publish.getText());
                    int score=0;
                    /*对文字分数获取*/
                    if (length<50) {
                        score+=1;
                    }else if (length<100){
                        score+=2;
                    }else if (length>100){
                        score+=3;
                    }

                    /*对照片分数获取*/
                    List<String> medias = publish.getMedias();
                    if (CollectionUtils.isNotEmpty(medias)){
                        score+=medias.size();
                    }
                    /*获得结果*/
                    recommendQuanZi.setScore(Double.valueOf(score));
                    break;
                case 2://浏览动态
                    recommendQuanZi.setScore(1d);
                    break;
                case 3://点赞
                    recommendQuanZi.setScore(5d);
                    break;
                case 4://喜欢
                    recommendQuanZi.setScore(8d);
                    break;
                case 5://评论
                    recommendQuanZi.setScore(10d);
                    break;
                case 6://取消点赞
                    recommendQuanZi.setScore(-5d);
                    break;
                case 7://取消喜欢
                    recommendQuanZi.setScore(-8d);
                    break;
                default:
                    recommendQuanZi.setScore(0d);
                    break;
            }

            /*日志存储,按天,spark大数据处理程序,一般会拉取最近7天的数据来进行计算*/
            /*数据存入mongodb*/
            mongoTemplate.save(recommendQuanZi,"recommend_quanzi_"+new DateTime().toString("yyyyMMdd"));

        } catch (Exception e) {
            e.printStackTrace();
            log.info("消息解析出错!数据为:{}",message);

        }


    }
}
