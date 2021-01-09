package com.tanhua.server.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.server.utils.Cache;
import com.tanhua.server.utils.UserThreadLocal;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author Administrator
 * @create 2021/1/3 20:55
 * 统一缓存放入处理类
 */
@ControllerAdvice/*controller做切面处理,增强*/
public class MyResponseBodyAdvice implements ResponseBodyAdvice {

    /*配置中缓存状态*/
    @Value("${tanhua.cache.enable}")
    private boolean cacheEnable;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private static final ObjectMapper OBJECT_MAPPER=new ObjectMapper();


    //ResponseBodyAdvice 对返回数据的 处理
    //supports:是否去调用beforeBodyWrite 重写返回数据
    @Override
    public boolean supports(MethodParameter returnType, Class aClass) {
        return cacheEnable
                && returnType.hasMethodAnnotation(Cache.class)
                && returnType.hasMethodAnnotation(GetMapping.class);
    }


    /**
     * 重写返回数据
     * @param body
     * @param returnType
     * @param selectedContentType
     * @param selectedConverterType
     * @param request
     * @param response
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        //只做缓存的放入
        try {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            HttpServletRequest servletRequest = servletServerHttpRequest.getServletRequest();
            String interfaceMethod = servletRequest.getRequestURI();
            Map<String, String[]> parameterMap = servletRequest.getParameterMap();
            String token = servletRequest.getHeader("Authorization");

            String key = "SERVER_"+ DigestUtils.md5Hex(interfaceMethod+"_"+token+"_"+OBJECT_MAPPER.writeValueAsString(parameterMap));

            Cache cache = returnType.getMethodAnnotation(Cache.class);

            redisTemplate.opsForValue().set(key,OBJECT_MAPPER.writeValueAsString(body),cache.time(), TimeUnit.SECONDS);
        }catch (Exception e){
            e.printStackTrace();
        }
        return body;
    }
}
