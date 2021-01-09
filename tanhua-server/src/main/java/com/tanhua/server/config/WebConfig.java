package com.tanhua.server.config;


import com.tanhua.server.handler.RedisCacheInterceptor;
import com.tanhua.server.handler.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebConfig implements WebMvcConfigurer {

    /*缓存拦截*/
    @Autowired
    private RedisCacheInterceptor redisCacheInterceptor;

    /*token拦截*/
    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {


        /*拦截所有请求做token校验*/
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**");
        //  /user/findUser--配置拦截所有路径的请求--引入自定义拦截器类RedisCacheInterceptor
        registry.addInterceptor(redisCacheInterceptor).addPathPatterns("/**");
    }
}