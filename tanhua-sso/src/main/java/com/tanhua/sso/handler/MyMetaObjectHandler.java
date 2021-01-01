package com.tanhua.sso.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author Administrator
 * @create 2020/12/30 19:12
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 新增时自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Object created = getFieldValByName("created", metaObject);
        if (created==null){
            /*如果字段为空那么进行填充数据*/
            setFieldValByName("created", new Date(), metaObject);

        }


        Object updated = getFieldValByName("updated", metaObject);
        if (updated==null){
            /*如果字段为空那么进行填充数据*/
            setFieldValByName("updated",new Date(),metaObject);
        }

    }

    /**
     * 更新时自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        //更新数据时，直接更新字段
        setFieldValByName("updated",new Date(),metaObject);
    }
}
