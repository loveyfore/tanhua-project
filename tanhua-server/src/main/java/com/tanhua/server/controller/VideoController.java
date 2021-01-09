package com.tanhua.server.controller;

import com.tanhua.server.service.VideoService;
import com.tanhua.server.vo.PageResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Author Administrator
 * @create 2021/1/7 13:40
 */
@Log4j2
@RestController
@RequestMapping("smallVideos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * POST
     * 视频上传保存
     * /smallVideos
     * @param videoThumbnail 视频封面
     * @param videoFile 视频文件
     * @return
     */
    @PostMapping
    public ResponseEntity<Object> saveVideo(@RequestParam(value = "videoThumbnail",required = false)MultipartFile videoThumbnail,
                                            @RequestParam(value = "videoFile",required = false)MultipartFile videoFile){
        try {
            Boolean saveVideo= videoService.saveVideo(videoThumbnail,videoFile);
            if (saveVideo){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * GET
     * 查询小视频列表
     * /smallVideos
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping
    public ResponseEntity<Object> queryVideoList(@RequestParam(value = "page",defaultValue = "1")Integer pageNum,
                                                 @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){
        try {
            PageResult pageResult = videoService.queryVideoList(pageNum,pageSize);
            if (pageResult!=null){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * POST
     * 视频点赞
     * /smallVideos/:id/like
     * @param videoId
     * @return
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Object> likeComment(@PathVariable("commentId") String videoId){

        try {
            Long likeCount=videoService.likeComment(videoId);
            if (likeCount!=null){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    /**
     * POST
     * 视频取消点赞
     * /smallVideos/:id/dislike
     * @param videoId
     * @return
     */
    @PostMapping("/{commentId}/dislike")
    public ResponseEntity<Object> disLikeComment(@PathVariable("commentId") String videoId){

        try {
            Long likeCount=videoService.disLikeComment(videoId);
            if (likeCount!=null){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    /**
     * GET
     * 评论列表
     * /smallVideos/:id/comments
     * @param videoId
     * @return
     */
    @GetMapping("/{commentId}/comments")
    public ResponseEntity<Object> queryCommentsList(@PathVariable("commentId")String videoId,
                                                    @RequestParam(value = "page",defaultValue = "1")Integer pageNum,
                                                    @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){

        try {
            PageResult pageResult = videoService.queryCommentsList(videoId,pageNum,pageSize);
            if (pageResult!=null){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * POST
     * 评论发布-保存
     * /smallVideos/:id/comments
     * @param videoId
     * @param params
     * @return
     */
    @PostMapping("/{videoId}/comments")
    public ResponseEntity<Object> saveComments(@PathVariable("videoId")String videoId,
                                               @RequestBody Map<String,String> params){
        try {
            Boolean saveComment=videoService.saveComments(videoId,params.get("comment"));
            if (saveComment){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * POST
     * 评论点赞
     * /smallVideos/:id/like/comments
     * @param commentId
     * @return
     */
    @PostMapping("/{commentId}/comments/like")
    public ResponseEntity<Object> commentsLikeComment(@PathVariable("commentId")String commentId){
        Long likeCount = videoService.likeComment(commentId);
        if (likeCount!=null){
            return ResponseEntity.ok(likeCount);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * POST
     * 评论取消点赞
     * /smallVideos/:id/dislike/comments
     * @param commentId
     * @return
     */
    @PostMapping("{commentId}/comments/dislike")
    public ResponseEntity<Object> disCommentsLikeComment(@PathVariable("commentId")String commentId){
        Long likeCount = videoService.disLikeComment(commentId);
        if (likeCount!=null){
            return ResponseEntity.ok(likeCount);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * POST
     * 视频用户关注
     * /smallVideos/:uid/userFocus
     * @param followUserId 被关注的用户id followUserId
     * @return
     */
    @PostMapping("/{followUserId}/userFocus")
    public ResponseEntity<Object> saveUserFocusComments(@PathVariable("followUserId")Long followUserId){
        try {
            Boolean flag= videoService.followUser(followUserId);
            if (flag){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * POST
     * 视频用户取消关注
     * /smallVideos/:uid/userUnFocus
     * @param followUserId
     * @return
     */
    @PostMapping("/{followUserId}/userUnFocus")
    public ResponseEntity<Object> saveUserUnFocusComments(@PathVariable("followUserId")Long followUserId){
        try {
            Boolean flag = videoService.disFollowUser(followUserId);
            if (flag){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
