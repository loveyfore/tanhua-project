package com.tanhua.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 发布表，动态内容
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_publish")
public class Publish implements java.io.Serializable {

    private static final long serialVersionUID = 8732308321082804771L;

    /**
     * 主键id
     */
    private ObjectId id;
    /**
     * Long类型的动态id,用于推荐引擎使用  spark
     */
    private Long pid;
    /**
     * 发布用户id
     */
    private Long userId;
    /**
     * 文字
     */
    private String text;
    /**
     * 媒体数据，图片或小视频 url
     */
    private List<String> medias;
    /**
     * 谁可以看，1-公开，2-私密，3-部分可见，4-不给谁看
     */
    private Integer seeType;
    /**
     * 部分可见的列表
     */
    private List<Long> seeList;
    /**
     * 不给谁看的列表
     */
    private List<Long> notSeeList;
    /**
     * 经度
     */
    private String longitude;
    /**
     * 纬度
     */
    private String latitude;
    /**
     * 位置名称
     */
    private String locationName;
    /**
     * 发布时间
     */
    private Long created;


    /**
     * 方法用于媒体文件的访问地址拼接把URI拼接成URL
     * @param urlPrefix
     */
    public void fillMedias(String urlPrefix){
        List<String> newMedias =new ArrayList<>();
        /*媒体数据(图片,视频)不为空*/
        if (!CollectionUtils.isEmpty(medias)){
            for (String media : medias) {
                if (!media.startsWith("http")){
                    media=urlPrefix+media;
                }
                newMedias.add(media);
            }
        }
        this.medias=newMedias;
    }

}