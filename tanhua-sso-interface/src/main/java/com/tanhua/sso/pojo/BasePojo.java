package com.tanhua.sso.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.util.Date;

/**
 * @Author Administrator
 * @create 2020/12/30 18:53
 * 该类对pojo共同有的字段进行了抽取使用mybatis-plus自动填充
 */
public abstract class BasePojo {

    /**
     * 记录新增时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date created;
    /**
     * 记录修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updated;
}
