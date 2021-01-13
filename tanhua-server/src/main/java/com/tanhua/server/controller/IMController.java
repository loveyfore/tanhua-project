package com.tanhua.server.controller;

import com.tanhua.server.service.IMService;
import com.tanhua.server.utils.NoAuth;
import com.tanhua.server.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author Administrator
 * @create 2021/1/10 15:01
 */
@RestController
@RequestMapping("messages")
public class IMController {

    @Autowired
    private IMService imService;


    /**
     * POST
     * 联系人添加
     * /messages/contacts
     *
     * @param params Json参数 userId
     * @return
     */
    @PostMapping("/contacts")
    public ResponseEntity<Object> addContacts(@RequestBody Map<String, Long> params) {
        try {
            Long userId = params.get("userId");
            Boolean addResult = imService.addContacts(userId);
            if (addResult) {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 联系人列表
     * /messages/contacts
     * @param pageNum 页
     * @param pageSize 条
     * @param keyword 查询关键字
     * @return
     */
    @GetMapping("/contacts")
    public ResponseEntity<Object> queryContactsList(@RequestParam(value = "page",defaultValue = "1")Integer pageNum,
                                                    @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize,
                                                    @RequestParam(value = "keyword",required = false)String keyword){
        try {
            PageResult pageResult = imService.queryContactsList(pageNum,pageSize,keyword);
            if (pageResult!=null){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 点赞列表
     * /messages/likes
     * @param pageNum 页
     * @param pageSize 条
     * @return
     */
    @GetMapping("/likes")
    public ResponseEntity<Object> queryLikesList(@RequestParam(value = "page",defaultValue = "1")Integer pageNum,
                                                    @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){
        try {
            PageResult pageResult = imService.queryLikesList(pageNum,pageSize);
            if (pageResult!=null){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 喜欢列表
     * /messages/loves
     * @param pageNum 页
     * @param pageSize 条
     * @return
     */
    @GetMapping("/loves")
    public ResponseEntity<Object> queryLovesList(@RequestParam(value = "page",defaultValue = "1")Integer pageNum,
                                                 @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){
        try {
            PageResult pageResult = imService.queryLovesList(pageNum,pageSize);
            if (pageResult!=null){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 评论列表
     * /messages/comments
     * @param pageNum 页
     * @param pageSize 条
     * @return
     */
    @GetMapping("/comments")
    public ResponseEntity<Object> queryCommentList(@RequestParam(value = "page",defaultValue = "1")Integer pageNum,
                                                 @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){
        try {
            PageResult pageResult = imService.queryCommentList(pageNum,pageSize);
            if (pageResult!=null){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 公告列表
     * /messages/announcements
     * @param pageNum 页
     * @param pageSize 条
     * @return
     */
    @GetMapping("/announcements")
    @NoAuth /*允许未认证,也就是未登录的情况下请求数据*/
    public ResponseEntity<Object> queryAnnouncementsList(@RequestParam(value = "page",defaultValue = "1")Integer pageNum,
                                                   @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){
        try {
            PageResult pageResult = imService.queryAnnouncementsList(pageNum,pageSize);
            if (pageResult!=null){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


}
