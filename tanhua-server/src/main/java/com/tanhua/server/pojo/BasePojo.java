package com.tanhua.server.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
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