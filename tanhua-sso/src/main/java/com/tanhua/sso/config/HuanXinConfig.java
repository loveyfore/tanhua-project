package com.tanhua.sso.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @Author Administrator
 * @create 2021/1/9 18:51
 */
@Configuration
@PropertySource("classpath:huanxin.properties")
@ConfigurationProperties(prefix = "tanhua.huanxin")
@Data
public class HuanXinConfig {
    /**
     * 环信请求地址
     */
    private String url;
    /**
     * 环信应用唯一标识
     */
    private String orgName;
    /**
     * 自己的app名称
     */
    private String appName;
    /**
     * 客户端id
     */
    private String clientId;
    /**
     * 客户端密钥
     */
    private String clientSecret;
}
