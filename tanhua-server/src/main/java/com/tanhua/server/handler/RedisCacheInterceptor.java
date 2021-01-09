package com.tanhua.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.server.utils.Cache;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author Administrator
 * @create 2021/1/3 20:12
 * 缓存拦截器类
 */
@Component
@Log4j2
public class RedisCacheInterceptor implements HandlerInterceptor {

    /**
     * 1.获取到接口路径,参数,token
     * 2.统一缓存,不是所有的接口都需要缓存
     * 3.统一开关,只针对GetMapping, @Cache自定义注解,可定义缓存时间
     */

    /*配置中缓存状态*/
    @Value("${tanhua.cache.enable}")
    private boolean cacheEnable;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private static final ObjectMapper OBJECT_MAPPER=new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*检查状态*/
        if (!cacheEnable){
            return true;
        }

        /*是否访问controller,是否是controller中的处理器*/
        if (!(handler instanceof HandlerMethod)){
            return true;
        }
        /*向下转型*/
        HandlerMethod handlerMethod =(HandlerMethod) handler;

        /*判断是否包含GETMapping和Cache注解--因为拦截器只针对get请求做拦截缓存*/
        if (!handlerMethod.hasMethodAnnotation(GetMapping.class)){
            return true;
        }

        if (!handlerMethod.hasMethodAnnotation(Cache.class)){
            return true;
        }

        try {
            /*接口路径,参数,token*/
            String interfaceMethod = request.getRequestURI();
            Map<String, String[]> parameterMap = request.getParameterMap();
            String token = request.getHeader("Authorization");
            /*生成键*/
            String key="SERVER_"+ DigestUtils.md5Hex(interfaceMethod+"_"+token+"_"+OBJECT_MAPPER.writeValueAsString(parameterMap));

            log.info("拦截器->RedisCacheInterceptorKey:{}",key);


            String cacheData = redisTemplate.opsForValue().get(key);
            if (StringUtils.isEmpty(cacheData)){
                log.info("拦截器->RedisCacheInterceptorMessage:缓存中没有数据!");
                return true;
            }



            log.info("拦截器->RedisCacheInterceptorMessage:缓存中取出了数据直接向页面返回!");
            // 将data数据进行响应
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(cacheData);


        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

        return false;
    }
}
