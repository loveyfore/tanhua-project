package com.tanhua.server.api;

import com.tanhua.server.pojo.Publish;
import com.tanhua.server.pojo.TimeLine;
import com.tanhua.server.vo.PageInfo;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;


/**
 * @Author Administrator
 * @create 2021/1/3 22:15
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class QuanZiApiImplTest {

    @Autowired
    private QuanZiApi quanZiApi;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void savePublish() {
        Publish publish = new Publish();
        publish.setUserId(1L);
        publish.setLocationName("上海市");
        publish.setSeeType(1);
        publish.setText("今天天气不错~");
        publish.setMedias(Arrays.asList("https://itcast-tanhua.oss-cn-shanghai.aliyuncs.com/images/quanzi/1.jpg"));
        boolean result = this.quanZiApi.savePublish(publish);
        System.out.println(result);
    }


    /**
     * 测试用于插入圈子推荐数据。
     */
    @Test
    public void testRecommendPublish(){
        //查询用户id为2的动态作为推荐动态的数据
        PageInfo<Publish> pageInfo = this.quanZiApi.queryPublishList(2L, 1, 10,false);
        for (Publish record : pageInfo.getRecords()) {

            TimeLine timeLine = new TimeLine();
            timeLine.setId(ObjectId.get());
            timeLine.setPublishId(record.getId());
            timeLine.setUserId(record.getUserId());
            timeLine.setDate(System.currentTimeMillis());

            this.mongoTemplate.save(timeLine, "quanzi_time_line_recommend");
        }
    }
}