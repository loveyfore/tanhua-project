package com.tanhua.recommend.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.recommend.pojo.RecommendVideo;
import com.tanhua.recommend.vo.VideoMsg;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;


/**
 * @Author Administrator
 * @create 2021/1/12 22:18
 */
@Log4j2
@Component
@RocketMQMessageListener(consumerGroup = "tanhua-video-consumer",topic = "tanhua-video")
public class VideoMessageConsumer implements RocketMQListener<String> {

    private static final ObjectMapper OBJECT_MAPPER =new ObjectMapper();

    @Autowired
    private MongoTemplate mongoTemplate;



    @Override
    public void onMessage(String message) {
        log.info("RocketMQConsumerVideoMessage:{}",message);

        try {
            VideoMsg videoMsg = OBJECT_MAPPER.readValue(message, VideoMsg.class);

            /*数据封装*/
            RecommendVideo recommendVideo = new RecommendVideo();
            recommendVideo.setId(ObjectId.get());
            recommendVideo.setDate(videoMsg.getDate());
            recommendVideo.setUserId(videoMsg.getUserId());
            recommendVideo.setVideoId(videoMsg.getVid());

            /*
            发布+2
            点赞 +5
            评论 + 10
            */
            switch (videoMsg.getType()){
                case 1:
                    recommendVideo.setScore(2d);
                    break;
                case 2://浏览动态
                    recommendVideo.setScore(1d);
                    break;
                case 3://点赞
                    recommendVideo.setScore(5d);
                    break;
                case 4://喜欢
                    recommendVideo.setScore(8d);
                    break;
                case 5://评论
                    recommendVideo.setScore(10d);
                    break;
                case 6://取消点赞
                    recommendVideo.setScore(-5d);
                    break;
                case 7://取消喜欢
                    recommendVideo.setScore(-8d);
                    break;
                default:
                    recommendVideo.setScore(0d);
                    break;
            }

            /*日志存储,按天,spark大数据处理程序,一般会拉取最近7天的数据来进行计算*/
            /*数据存入mongodb*/
            mongoTemplate.save(recommendVideo,"recommend_video_"+new DateTime().toString("yyyyMMdd"));

        } catch (Exception e) {
            e.printStackTrace();
            log.info("消息解析出错!数据为:{}",message);
        }


    }
}
