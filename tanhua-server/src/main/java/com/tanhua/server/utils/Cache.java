package com.tanhua.server.utils;

import java.lang.annotation.*;
import java.time.Duration;

/**
 * 被标记为Cache的Controller进行缓存，其他情况不进行缓存
 */

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    /**
     * 表示默认过期时长
     * @return
     */
    long time() default 60;
}