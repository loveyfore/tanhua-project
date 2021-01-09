package com.tanhua.server.controller;

import com.tanhua.server.service.CommentsService;
import com.tanhua.server.vo.PageResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.ResourceBundle;

/**
 * @Author Administrator
 * @create 2021/1/6 19:33
 */
@Log4j2
@RestController
@RequestMapping("comments")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    /**
     * GET
     * 评论列表
     * /comments
     * @param publishId 动态id
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping
    public ResponseEntity<Object> queryCommentsList(@RequestParam("movementId")String publishId,
                                                    @RequestParam(value = "page",defaultValue = "1")Integer pageNum,
                                                    @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){
        try {
            PageResult pageResult = commentsService.queryCommentsList(publishId,pageNum,pageSize);
                return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * POST
     * 评论-提交
     * /comments
     * @return
     */
    @PostMapping
    public ResponseEntity<Object> saveComments(@RequestBody Map<String,String> params){
        try {
            String publishId = params.get("movementId");
            String comment = params.get("comment");
            Boolean flag= commentsService.saveComments(publishId,comment);
            if (flag){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 评论-点赞
     * /comments/:id/like
     * @return
     */
    @GetMapping("/{id}/like")
    public ResponseEntity<Object> likeComment(@PathVariable("id") String commentId){
        try {
            Long likeCount = commentsService.likeComment(commentId);
            if (likeCount != null) {
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 评论-取消点赞
     * /comments/:id/dislike
     * @return
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity<Object> disLikeComment(@PathVariable("id") String commentId){
        try {
            Long likeCount = commentsService.cancelLikeComment(commentId);
            if (null != likeCount) {
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
