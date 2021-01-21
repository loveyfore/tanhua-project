package com.tanhua.server.controller;

import com.tanhua.server.service.UsersService;
import com.tanhua.server.vo.CountsVo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.SettingsVo;
import com.tanhua.server.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * @Author Administrator
 * @create 2021/1/16 10:11
 */
@RestController
@RequestMapping("users")
public class UsersController {

    @Autowired
    private UsersService usersService;


    /**
     * GET
     * 我的- 用户资料 - 读取
     * /users
     * @param userId
     * @param huanXinId
     * @return
     */
    @GetMapping
    public ResponseEntity<Object> userInfo(@RequestParam(value = "userId",required = false)Long userId,
                                           @RequestParam(value = "huanxinID",required = false)Long huanXinId){
        try {

            UserInfoVo userInfoVo =usersService.queryUserInfo(userId,huanXinId);
            if (userInfoVo!=null){
                return ResponseEntity.ok(userInfoVo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * PUT
     * 用户资料 - 保存
     * /users
     * @param userInfoVo
     * @return
     */
    @PutMapping
    public ResponseEntity<Object> updateUserInfo(@RequestBody UserInfoVo userInfoVo){
        try {

            Boolean saveUserInfo =usersService.updateUserInfo(userInfoVo);
            if (saveUserInfo){
                return ResponseEntity.ok(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 获取用户 互相喜欢，喜欢，粉丝 - 统计
     * /users/counts
     * @return
     */
    @GetMapping("/counts")
    public ResponseEntity<Object> queryCounts(){
        try {

            CountsVo countsVo =usersService.queryCounts();
            if (countsVo!=null){
                return ResponseEntity.ok(countsVo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 互相喜欢、喜欢、粉丝、谁看过我 - 翻页列表
     * /users/friends/:type
     * type
     * 1 互相关注
     * 2 我关注
     * 3 粉丝
     * 4 谁看过我
     * @return
     */
    @GetMapping("/friends/{type}")
    public ResponseEntity<Object> queryList(@PathVariable("type")Integer type,
                                            @RequestParam(value = "page",required = false,defaultValue = "1") Integer pageNum,
                                            @RequestParam(value = "pagesize",required = false,defaultValue = "10") Integer pageSize,
                                            @RequestParam(value = "nickname",required = false) String nickname){
        try {

            PageResult pageResult =usersService.queryList(type,pageNum,pageSize,nickname);
            if (pageResult!=null){
                return ResponseEntity.ok(pageResult);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * DELETE
     * 喜欢 - 取消 --取消关注
     * /users/like/:uid
     * @param userId
     * @return
     */
    @DeleteMapping("like/{userId}")
    public ResponseEntity<Object> disLike(@PathVariable("userId")Long userId){
        try {
            Boolean disLike =usersService.disLike(userId);
            if (disLike){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    /**
     * POST
     * 粉丝-喜欢--关注粉丝
     * /users/fans/:uid
     * @param userId
     * @return
     */
    @PostMapping("/fans/{userId}")
    public ResponseEntity<Object> fanLike(@PathVariable("userId")Long userId){
        try {
            Boolean fanLike =usersService.fanLike(userId);
            if (fanLike){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }


    /**
     * POST
     * 通用设置-通知设置保存
     * /users/notifications/setting
     *  likeNotification 喜欢通知
     *  pinglunNotification 评论通知
     *  gonggaoNotification 公告通知
     * @return
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity<Object> notificationSettings(@RequestBody Map<String,Boolean> params){
        Boolean likeNotification = params.get("likeNotification");
        Boolean pinglunNotification = params.get("pinglunNotification");
        Boolean gonggaoNotification = params.get("gonggaoNotification");
        try {
            Boolean saveSettings =usersService.updateNotificationSettings(likeNotification,pinglunNotification,gonggaoNotification);
            if (saveSettings){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    /**
     * GET
     * 用户通用设置 - 读取
     * /users/settings
     * @return
     */
    @GetMapping("/settings")
    public ResponseEntity<SettingsVo> queryUserSettings(){

        try {
            SettingsVo settingsVo =usersService.queryUserSettings();
            if (settingsVo!=null){
                return ResponseEntity.ok(settingsVo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * POST
     * 用户个人资料-陌生人问题保存更新
     * /users/questions
     * @return
     */
    @PostMapping("/questions")
    public ResponseEntity<Object> updateQuestion(@RequestBody Map<String,String> params){

        String content = params.get("content");
        try {
            Boolean updateQuestion = usersService.updateQuestion(content);
            if (updateQuestion){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * GET
     * 黑名单列表 - 翻页列表
     * /users/blacklist
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/blacklist")
    public ResponseEntity<Object> queryBlackList(@RequestParam(value = "page",required = false,defaultValue = "1") Integer pageNum,
                                                 @RequestParam(value = "pagesize",required = false,defaultValue = "10") Integer pageSize){


        try {
            PageResult pageResult = usersService.queryBlackList(pageNum,pageSize);
            if (pageResult!=null){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * DELETE
     * 黑名单用户删除
     * /users/blacklist/:uid
     * @param blackListUserId
     * @return
     */
    @DeleteMapping("/blacklist/{blackListUserId}")
    public ResponseEntity<Object> disBlack(@PathVariable("blackListUserId") Long blackListUserId){

        try {
            Boolean disBlack =usersService.disBlack(blackListUserId);
            if (disBlack){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }





}
