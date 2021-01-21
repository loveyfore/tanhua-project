package com.tanhua.sso.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.doclint.Checker;
import com.tanhua.sso.config.AliyunConfig;
import com.tanhua.sso.enums.SexEnum;
import com.tanhua.sso.mapper.UserInfoMapper;
import com.tanhua.sso.mapper.UserMapper;
import com.tanhua.sso.pojo.User;
import com.tanhua.sso.pojo.UserInfo;
import com.tanhua.sso.vo.PicUploadResult;
import com.tanhua.sso.vo.Result;
import io.jsonwebtoken.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author Administrator
 * @create 2020/12/30 23:41
 */
@Service
@Log4j2
public class UserService {

    /**
     * redis
     */
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * mapper
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 配置文件中的密钥(盐值)token解密关键
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 注入图片上传业务
     */
    @Autowired
    private PicUploadService picUploadService;

    /**
     * json处理类
     */
    private static final ObjectMapper OBJECT_MAPPER=new ObjectMapper();

    /**
     * 人脸识别
     */
    @Autowired
    private FaceEngineService faceEngineService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * oss配置文件
     */
    @Autowired
    private AliyunConfig aliyunConfig;

    @Autowired
    private HuanXinService huanXinService;

    @Autowired
    private ThreadService threadService;

    /**
     * 登录验证
     * 验证码校验,失败返回错误信息
     * 判断用户是否存在
     * 不存在注册,存在登录
     * 生成token令牌
     * token放入redis,value用户信息
     * @param phone
     * @param code
     * @return
     */
    public Result loginVerification(String phone,String code){
        String redisCode = redisTemplate.opsForValue().get("LOGIN_CODE_" + phone);
        /*为空-验证码失效*/
        if (StringUtils.isEmpty(redisCode)){
            return new Result(false,"验证码失效!",null);
        }
        /*有数据,验证码不一致*/
        if (!redisCode.equals(code)){
            return new Result(false,"验证码错误!",null);
        }

        /*登录结束,判断用户时新用户还是老用户--默认老用户*/
        boolean isNew=false;

        /*构造查询条件*/
        QueryWrapper<User> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("mobile",phone).last("limit 1");
        User user = userMapper.selectOne(queryWrapper);

        if (user==null){
            /*新用户-注册*/
            user = new User();
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex("test-password"));
            userMapper.insert(user);
            isNew=true;

            /*针对新用户取注册环信*/
            log.info("isNewUserId:{}",user.getId());
            huanXinService.register(user.getId());

        }

        Long userId = user.getId();

        /**
         * - JWT就是一个字符串，经过加密处理与校验处理的字符串，形式为：A.B.C
         * - A由JWT头部信息header加密得到
         * - B由JWT用到的身份验证信息json数据加密得到
         * - C由A和B加密得到，是校验部分
         */
        /*登录成功,生成token*/
        /*A*/
        Map<String,Object> header=new HashMap<>();
        header.put(JwsHeader.TYPE,JwsHeader.JWT_TYPE);
        header.put(JwsHeader.ALGORITHM,"HS256");

        /*B*/
        Map<String,Object> claims=new HashMap<>();
        claims.put("id",userId);

        /*C    .setExpiration(new Date(System.currentTimeMillis() + 24*3600*1000)) //设置过期时间，3秒后过期-----这里不使用过期方法*/
        String token = Jwts.builder()
                .setHeader(header)
                .setClaims(claims)//payload，存放数据的位置，不能放置敏感数据，如：密码等
                .signWith(SignatureAlgorithm.HS256, jwtSecret)//设置加密方法和加密盐
                .compact();

        try {
            /*token存入redis 和用户信息做匹配  --7天数据过期*/
            redisTemplate.opsForValue().set("TOKEN_"+token,OBJECT_MAPPER.writeValueAsString(user), Duration.ofDays(7));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Result(true,"系统异常",null);
        }

        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("token",token);
        resultMap.put("isNew",isNew);

        /*为不影响登录,使用多线程解决登录成功日志消息的发送*/
        //threadService.sendMQ(user);



        return new Result(true,"OK",resultMap);
    }


    /**
     * 新用户上传头像业务处理
     * @param file
     * @param token
     * @return
     */
    public boolean uploadHead(MultipartFile file, String token) {
        /*检测token--用户必须在登录的情况下操作*/
        User user = checkToken(token);
        if (user==null){
            return false;
        }

        PicUploadResult upload = picUploadService.upload(file);
        log.info("uploadResult----->{}",upload);
        if ("done".equals(upload.getStatus())){
            try {
                //boolean flag = faceEngineService.checkIsPortrait(file.getBytes());
                boolean flag=true;   //TODO 为测试方便暂时砍掉人像验证
                /*如果是人像,存入数据库*/
                if (flag){
                    QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
                    queryWrapper.eq("user_id",user.getId()).last("limit 1");
                    UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
                    if (userInfo==null){
                        userInfo = new UserInfo();
                        userInfo.setUserId(user.getId());
                        /*拼接头像在oss中的网络路径,PicUploadResult对象已经封装数据*/
                        userInfo.setLogo(aliyunConfig.getUrlPrefix()+upload.getName());
                        userInfo.setCoverPic(userInfo.getLogo());
                        userInfoMapper.insert(userInfo);
                    }else {
                        /**
                         * 因为注册的流程是先填写用户信息再去设置头像
                         * 所以这里走判断的时候就不为null(添加了更为严谨些)
                         * 所以只需要去将用户的头像信息进行更新操作
                         * upload.getName() --> "images/2021/01/01/1609490847868530.jpg"
                         */
                        /*拼接头像在oss中的网络路径,PicUploadResult对象已经封装数据*/
                        userInfo.setLogo(aliyunConfig.getUrlPrefix()+upload.getName());
                        /*更新数据*/
                        UpdateWrapper<UserInfo> updateWrapper =new UpdateWrapper<>();
                        updateWrapper.eq("user_id",user.getId());
                        userInfo.setCoverPic(userInfo.getLogo());
                        userInfoMapper.update(userInfo, updateWrapper);
                    }
                    
                }
                return flag;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 检测token是否合法
     * @param token
     * @return
     */
    public User checkToken(String token){
        try {
            /*解析token数据*/
            Map<String,Object> body=Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
            log.info("tokenBody----->{}",body);

            /*从redis取出数据*/
            String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
            if (StringUtils.isEmpty(userJson)){
                return null;
            }
            User user = OBJECT_MAPPER.readValue(userJson, User.class);

            /*当用户发起请求,为用户做续期的操作,解决频繁的登录的操作*/
            redisTemplate.expire("TOKEN_" + token,7, TimeUnit.DAYS);


            return user;
        } catch (ExpiredJwtException e) {
            /*如果token过期,则会抛这个异常*/
            log.info("token以过期!");
        } catch (Exception e) {
            log.info("token不合法!");
        }
        return null;
    }

    /**
     * 新用户注册资料业务
     * @param paramMap
     * @param token
     * @return
     */
    public boolean saveUserInfo(Map<String, String> paramMap, String token) {

        /*检测token--用户必须在登录的情况下操作*/
        User user = checkToken(token);
        if (user==null){
            return false;
        }

        String gender = paramMap.get("gender");
        String nickname = paramMap.get("nickname");
        String birthday = paramMap.get("birthday");
        String city = paramMap.get("city");

        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",user.getId()).last("limit 1");
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
        if (userInfo==null){
            /*添加*/
            userInfo=new UserInfo();
            userInfo.setUserId(user.getId());
            userInfo.setNickName(nickname);
            userInfo.setBirthday(birthday);
            userInfo.setCity(city);
            /*这里是枚举类型,使用三元运算符判断*/
            userInfo.setSex(StringUtils.equals(gender, "man") ? SexEnum.MAN : SexEnum.WOMAN);

            /*暂时写死数据  //TODO --*/
            userInfo.setTags("单身,本科,年龄相仿");
            userInfo.setAge(20);
            userInfo.setEdu("本科");
            userInfo.setIndustry("计算机行业");
            userInfo.setIncome("40");
            userInfo.setMarriage("未婚");

            userInfoMapper.insert(userInfo);
        }else {
            /*更新*/
            userInfo.setNickName(nickname);
            userInfo.setBirthday(birthday);
            userInfo.setCity(city);
            /*这里是枚举类型,使用三元运算符判断*/
            userInfo.setSex(StringUtils.equals(gender, "man") ? SexEnum.MAN : SexEnum.WOMAN);
            userInfoMapper.updateById(userInfo);
        }
        return true;
    }

    /**
     * 更新用户手机号
     * @param userId
     * @param phone
     * @return
     */
    public Boolean updatePhoneCode(Long userId, String phone) {
        User user = new User();
        /*这里,mybatis字段有值才做更新,无值不做操作*/
        user.setId(userId);
        user.setMobile(phone);
        return userMapper.updateById(user)>0;
    }
}
