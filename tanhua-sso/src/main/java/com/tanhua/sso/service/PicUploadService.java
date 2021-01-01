package com.tanhua.sso.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.sso.config.AliyunConfig;
import com.tanhua.sso.vo.PicUploadResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * @Author Administrator
 * @create 2021/1/1 15:00
 * 用于专门处理上传头像的公用业务
 */
@Service
@Log4j2
public class PicUploadService {

    /*定义可以上传的文件格式*/
    private static final String[] IMAGE_TYPE = new String[]{".bmp", ".jpg",
            ".jpeg", ".gif", ".png"};
    /*注入阿里oss*/
    @Autowired
    private OSSClient ossClient;

    /*注入oss配置类用于获取参数*/
    @Autowired
    private AliyunConfig aliyunConfig;

    /*json处理*/
    private static final ObjectMapper OBJECT_MAPPER =new ObjectMapper();

    public PicUploadResult upload(MultipartFile file){
        /**
         * 判断文件合法
         * 生成文件的存储路径
         * 调用oss上传
         * 上传成功返回图片地址
         */
        /*获取上传文件名*/
        String fileName = file.getOriginalFilename();
        /*封装上传图片的结果*/
        PicUploadResult fileUploadResult = new PicUploadResult();
        boolean isLegal = false;
        for (String suffix : IMAGE_TYPE) {
            /*判读文件名是否以合法的后缀结尾*/
            if(StringUtils.endsWithIgnoreCase(fileName,suffix)){
                isLegal=true;
                break;
            }
        }

        /*不合法*/
        if (!isLegal){
            fileUploadResult.setStatus("error");
            return fileUploadResult;
        }

        /*合法--生成路径*/
        String path = genFilePath(fileName);

        try {
            /*调用oss上传*/
            PutObjectResult putObjectResult = ossClient.putObject(aliyunConfig.getBucketName(), path, file.getInputStream());

            log.info("ossResult----->{}",OBJECT_MAPPER.writeValueAsString(putObjectResult));
        } catch (IOException e) {
            /*上传失败*/
            e.printStackTrace();
        }

        fileUploadResult.setName(path);
        fileUploadResult.setStatus("done");
        fileUploadResult.setUid(String.valueOf(System.currentTimeMillis()));
        return fileUploadResult;
    }


    /**
     * 生成图片存储路径
     * @return
     */
    public String genFilePath(String fileName){
        DateTime dateTime = new DateTime();
        return "images/"
                +dateTime.toString("yyyy")
                +"/" +dateTime.toString("MM")
                +"/" +dateTime.toString("dd")
                +"/"+System.currentTimeMillis()+ RandomUtils.nextInt(100,9999)
                /*方法根据"."去切割文件后缀*/
                +"."+StringUtils.substringAfterLast(fileName,".");
    }
}
