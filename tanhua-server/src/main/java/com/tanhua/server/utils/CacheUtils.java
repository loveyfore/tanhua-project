package com.tanhua.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * @Author Administrator
 * @create 2021/1/3 19:22
 */
@Component
@Log4j2
public class CacheUtils {

    @Autowired
    private static final ObjectMapper OBJECT_MAPPER =new ObjectMapper();

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 向缓存中取出数据
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getCache(String key,Class<T> clazz) {
        try {
            /*使用md5加密--使用密钥(盐)*/
            String md5Crypt = Md5Crypt.md5Crypt(key.getBytes(), "$1$6uikQgrK");
            log.info("MD5-Key->{}",md5Crypt);
            String pageResultJson = redisTemplate.opsForValue().get(md5Crypt);
            if (StringUtils.isNotEmpty(pageResultJson)) {
                T t = OBJECT_MAPPER.readValue(pageResultJson, clazz);
                if (t != null) {
                    log.info("CacheUtilsMessage:访问了缓存中的数据!");
                    return t;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("CacheUtilsMessage:缓存中没有数据!");
        return null;
    }


    /**
     * 向缓存中添加数据
     * @param <T> 数据
     * @param key
     * @param duration 过期时间
     * @return
     */
    public <T> void putCache(String key, T Object, Duration duration) {
        try {
            /*使用md5加密--使用密钥(盐)*/
            String md5Crypt = Md5Crypt.md5Crypt(key.getBytes(),"$1$6uikQgrK");
            log.info("MD5-Key->{}",md5Crypt);
            /*将结果放入缓存,过期时间设置为10分钟*/
            redisTemplate.opsForValue().set(md5Crypt,OBJECT_MAPPER.writeValueAsString(Object),duration);
            log.info("CacheUtilsMessage:向缓存中插入了数据!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
