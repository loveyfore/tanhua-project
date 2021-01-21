package com.tanhua.server.controller;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.tanhua.server.enums.SendMessageTypeEnum;
import com.tanhua.server.service.MovementsService;
import com.tanhua.server.service.QuanziMQService;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.Movements;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.VisitorsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author Administrator
 * @create 2021/1/3 22:23
 */
@RestController
@RequestMapping("movements")
public class MovementsController {

    @Autowired
    private MovementsService movementsService;

    @Autowired
    private QuanziMQService quanziMQService;

    /**
     * POST
     * 动态-发布
     * /movements
     * @param textContent
     * @param location
     * @param latitude
     * @param longitude
     * @param multipartFiles
     * @return
     */
    @PostMapping
    public ResponseEntity<Object> save(@RequestParam("textContent") String textContent,
                                       @RequestParam(value = "location", required = false) String location,
                                       @RequestParam(value = "latitude", required = false) String latitude,
                                       @RequestParam(value = "longitude", required = false) String longitude,
                                       @RequestParam(value = "imageContent", required = false) MultipartFile[] multipartFiles){

        try {
            String publishId = movementsService.save(textContent,location,longitude,latitude,multipartFiles);
            if (StringUtils.isNotEmpty(publishId)){
                /*发送消息,使用线程的方式,不会影响返回值的执行*/
                quanziMQService.sendMessage(UserThreadLocal.get(),SendMessageTypeEnum.PUBLISH.getValue(),publishId);
                return ResponseEntity.ok(null);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }


    /**
     * GET
     * 好友动态
     * /movements
     * @return
     */
    @GetMapping
    public ResponseEntity<Object> queryPublishList(@RequestParam(value ="page",defaultValue = "1")Integer pageNum,
                                                   @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){
        try {
            PageResult pageResult = movementsService.queryPublishList(pageNum,pageSize,false);//false表示是好友动态
            if (pageResult!=null){
                return ResponseEntity.ok(pageResult);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 推荐动态
     * /movements/recommend
     * @return
     */
    @GetMapping("recommend")
    public ResponseEntity<Object> queryRecommendPublishList(@RequestParam(value ="page",defaultValue = "1")Integer pageNum,
                                                            @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize){
        try {
            PageResult pageResult = movementsService.queryPublishList(pageNum,pageSize,true);//false表示是推荐动态
            if (pageResult!=null){
                return ResponseEntity.ok(pageResult);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * GET
     * 动态点赞
     * /movements/:id/like
     * @return
     */
    @GetMapping("{publishId}/like")
    public ResponseEntity<Object> likeComment(@PathVariable("publishId") String publishId){
        try {
            Long likeCount = movementsService.likeComment(publishId);
            if (likeCount != null) {
                /*发送消息,使用线程的方式,不会影响返回值的执行*/
                quanziMQService.sendMessage(UserThreadLocal.get(),SendMessageTypeEnum.LIKE.getValue(),publishId);

                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * GET
     * 动态取消点赞
     * /movements/:id/dislike
     * @param publishId
     * @return
     */
    @GetMapping("{publishId}/dislike")
    public ResponseEntity<Object> dislike(@PathVariable("publishId") String publishId){

        try {
            Long likeCount = movementsService.dislike(publishId);
            /*发送消息,使用线程的方式,不会影响返回值的执行*/
            quanziMQService.sendMessage(UserThreadLocal.get(),SendMessageTypeEnum.CANCEL_LIKE.getValue(),publishId);
            return ResponseEntity.ok(likeCount);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    /**
     * GET
     * 动态喜欢
     * /movements/:id/love
     * @return
     */
    @GetMapping("{publishId}/love")
    public ResponseEntity<Object> loveComment(@PathVariable("publishId") String publishId){
        try {
            Long loveCount = movementsService.loveComment(publishId);
            if (loveCount != null) {
                /*发送消息,使用线程的方式,不会影响返回值的执行*/
                quanziMQService.sendMessage(UserThreadLocal.get(),SendMessageTypeEnum.LOVE.getValue(),publishId);
                return ResponseEntity.ok(loveCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * GET
     * 动态取消喜欢
     * /movements/:id/unlove
     * @param publishId
     * @return
     */
    @GetMapping("{publishId}/unlove")
    public ResponseEntity<Object> disLove(@PathVariable("publishId") String publishId){

        try {
            Long loveCount = movementsService.disLove(publishId);
            /*发送消息,使用线程的方式,不会影响返回值的执行*/
            quanziMQService.sendMessage(UserThreadLocal.get(),SendMessageTypeEnum.CANCEL_LOVE.getValue(),publishId);
            return ResponseEntity.ok(loveCount);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }



    /**
     * GET
     * 查询单条动态
     * /movements/:id
     * @param publishId
     * @return
     */
    @GetMapping("{publishId}")
    public ResponseEntity<Object> queryById(@PathVariable("publishId") String publishId){

        try {
            Movements movements = movementsService.queryById(publishId);
            if (movements!=null){
                /*发送消息,使用线程的方式,不会影响返回值的执行*/
                quanziMQService.sendMessage(UserThreadLocal.get(),SendMessageTypeEnum.WATCH_PUBLISH.getValue(),publishId);
                return ResponseEntity.ok(movements);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * GET
     * 谁看过我-访客列表查询
     * /movements/visitors
     * @return
     */
    @GetMapping("visitors")
    public ResponseEntity<Object> queryVisitorsList(){
        try {
            List<VisitorsVo> visitorsVoList = movementsService.queryVisitorsList();
            if (CollectionUtils.isNotEmpty(visitorsVoList)){
                return ResponseEntity.ok(visitorsVoList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * GET
     * 获取指定用户自己所有的动态,查询相册表
     * /movements/all
     * @param pageNum 页
     * @param pageSize 条
     * @param userId 用户id
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity<Object> queryAlbumList(@RequestParam(value = "page",defaultValue = "1")Integer pageNum,
                                                 @RequestParam(value = "pagesize",defaultValue = "10")Integer pageSize,
                                                 @RequestParam("userId")Long userId){
        try {
            PageResult pageResult = movementsService.queryAlbumList(userId,pageNum,pageSize);
            if (pageNum!=null){
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
