package com.tanhua.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.server.service.UserService;
import com.tanhua.server.utils.NoAuth;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.sso.pojo.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author Administrator
 * @create 2021/1/5 9:25
 */
@Component
@Log4j2
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private static final ObjectMapper OBJECT_MAPPER=new ObjectMapper();

    /**
     * 前置拦截方法
     * 1.不需要进行登录的直接放行
     * 2.需要进行登录的,获取token,sso认证
     * 3.认证通过,将用户放入ThreadLocal中
     * 4.认证未通过,返回false,状态码401 未认证   403 无权限
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        /*日志打印*/
        log.info("----------------Request Start----------------");
        log.info("RequestUri:{}",request.getRequestURI());
        log.info("RequestMethod:{}",request.getMethod());
        log.info("RequestParam:{}",OBJECT_MAPPER.writeValueAsString(request.getParameterMap()));



        /*是否未controller中的处理器*/
        if (!(handler instanceof HandlerMethod)){
            return true;
        }

        HandlerMethod handlerMethod =(HandlerMethod) handler;
        /*判断处理器是否包含NoAuth注解*/

        // TODO
        if(handlerMethod.hasMethodAnnotation(NoAuth.class)){
            return true;
        }

        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)){
            return false;
        }

        log.info("UserToken:{}",token);
        User user = userService.queryToken(token);
        log.info("RequestUser:{}",user);
        if (user==null){
            return false;
        }

        UserThreadLocal.set(user);
        log.info("----------------Request End----------------");
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        /*将用户信息销毁,防止内存泄露*/
        UserThreadLocal.del();
    }
}
