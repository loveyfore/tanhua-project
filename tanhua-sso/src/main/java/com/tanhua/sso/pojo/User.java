package com.tanhua.sso.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author Administrator
 * @create 2020/12/30 18:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
//@TableName("tb_user") 配置了表名前缀tb_,的配置后表名和实体类名一致mybatis-plus会自动映射
public class User extends BasePojo implements Serializable {
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户手机号
     */
    private String mobile;

    /**
     * 用户密码
     *  -@JsonIgnore:该注解忽略json操作时对密码的序列化
     */
    @JsonIgnore
    private String password;
}
