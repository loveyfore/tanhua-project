package com.tanhua.server.utils;

import java.lang.annotation.*;

/**
 * @Author Administrator
 * @create 2021/1/5 9:29
 * 注解标识未认证的请求
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoAuth {
}
