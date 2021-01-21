package com.tanhua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
/*忽略继承来的mongodb,如果不忽略会使用默认配置连接导致报错*/
@SpringBootApplication(exclude = {MongoDataAutoConfiguration.class, MongoAutoConfiguration.class})
public class ESApplication {

    public static void main(String[] args) {
        SpringApplication.run(ESApplication.class, args);
    }
}