package com.tanhua.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Author Administrator
 * @create 2021/1/12 10:43
 */
@Service
public class IDService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 通过redis,生成自增长id,作为推荐引擎使用
     * @param tableType 类型,是动态还是视频
     * @param mongodbId 数据库中动态的id
     * @return
     */
    public Long createId(String tableType,String mongodbId){

        String hashKey="TANHUA_HASH_ID_"+tableType;

        /*因为一条动态只能对应一个pid关系,对pid进行处理*/
        if (redisTemplate.opsForHash().hasKey(hashKey,mongodbId)){
            /*取到了就返回*/
            return Long.valueOf(redisTemplate.opsForHash().get(hashKey,mongodbId).toString());
        }


        /*生成自增长id,对不同的类型生成不同id*/
        Long pid = redisTemplate.opsForValue().increment("TANHUA_ID_" + tableType);



        /*一种类型的一个动态对应一个pid,这里使用hash排除*/
        redisTemplate.opsForHash().put(hashKey,mongodbId,String.valueOf(pid));


        return pid;
    }
}
