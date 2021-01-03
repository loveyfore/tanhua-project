package com.tanhua.sso.pojo;

import com.tanhua.sso.enums.SexEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author Administrator
 * @create 2020/12/30 18:58
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
//@TableName("tb_user_info") 配置了表名前缀tb_,的配置后表名和实体类名一致mybatis-plus会自动映射
public class UserInfo extends BasePojo implements Serializable {
    /**
     * id标识
     */
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 用户头像
     */
    private String logo;
    /**
     * 用户标签
     * 多个使用,号分割
     */
    private String tags;
    /**
     * 性别,枚举类型
     */
    private SexEnum sex;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 学历
     */
    private String edu;
    /**
     * 城市
     */
    private String city;
    /**
     * 生日
     */
    private String birthday;
    /**
     * 封面图片
     */
    private String coverPic;
    /**
     * 行业
     */
    private String industry;
    /**
     * 收入
     */
    private String income;
    /**
     * 婚姻状态
     */
    private String marriage;
}
