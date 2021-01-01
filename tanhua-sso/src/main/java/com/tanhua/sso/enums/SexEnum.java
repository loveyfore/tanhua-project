package com.tanhua.sso.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;


/**
 * @Author Administrator
 * @create 2020/12/30 18:12
 * 枚举标识用户的性别
 */

public enum  SexEnum implements IEnum<Integer> {



    MAN(1,"男"),
    WOMAN(2,"女"),
    UNKNOWN(3,"未知");



    private int value;
    private String desc;


    SexEnum(int value,String desc){
        this.value=value;
        this.desc=desc;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public String toString(){
        return this.desc;
    }
}
