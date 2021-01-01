package com.tanhua.sso;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author Administrator
 * @create 2020/12/30 19:51
 */
@SpringBootApplication
@MapperScan("com.tanhua.sso.mapper")/*配置扫包,省去每个接口配置@mapper注解*/
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class,args);
    }
}
