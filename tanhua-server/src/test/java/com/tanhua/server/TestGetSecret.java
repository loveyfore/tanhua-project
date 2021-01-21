package com.tanhua.server;

import org.apache.commons.codec.digest.DigestUtils;


/**
 * @Author Administrator
 * @create 2021/1/14 10:01
 */
public class TestGetSecret {

    public static void main(String[] args) {
        System.out.println(DigestUtils.md5Hex(35 + "_itcast_tanhua"));
    }
}
