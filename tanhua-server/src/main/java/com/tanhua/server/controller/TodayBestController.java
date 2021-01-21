package com.tanhua.server.controller;

import com.tanhua.server.service.TodayBestService;
import com.tanhua.server.utils.Cache;
import com.tanhua.server.vo.NearUserVo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.TodayBest;
import com.tanhua.server.vo.params.RecommendUserQueryParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author Administrator
 * @create 2021/1/2 21:36
 */
@RestController
@RequestMapping("tanhua")
public class TodayBestController {

    @Autowired
    private TodayBestService todayBestService;


    /**
     * GET
     * 今日佳人
     * /tanhua/todayBest
     * @return
     */
    @GetMapping("todayBest")
    public ResponseEntity<Object> todayBest(){

        try {

            TodayBest todayBest = todayBestService.todayBest();
            if (todayBest!=null){
                return ResponseEntity.ok(todayBest);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 推荐朋友
     * /tanhua/recommendation
     * @return
     */
    @GetMapping("recommendation")
    @Cache(time = 80)
    public ResponseEntity<Object> recommendation(RecommendUserQueryParam queryParam){
        try {

            PageResult pageResult = todayBestService.recommendation(queryParam);
            return ResponseEntity.ok(pageResult);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    /**
     * GET
     * 点击后,今日佳人详细信息查询
     * /tanhua/:id/personalInfo
     * @param userId
     * @return
     */
    @GetMapping("/{userId}/personalInfo")
    public ResponseEntity<Object> queryTodayBest(@PathVariable("userId")Long userId){
        try {
            TodayBest todayBest= todayBestService.queryTodayBest(userId);
            if (todayBest!=null){
                return ResponseEntity.ok(todayBest);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * GET
     * 查询陌生人问题
     * /tanhua/strangerQuestions
     * @param userId
     * @return
     */
    @GetMapping("/strangerQuestions")
    public ResponseEntity<Object> queryQuestion(@RequestParam("userId")Long userId){
        try {
            String question =  todayBestService.queryQuestion(userId);
            if (StringUtils.isNotEmpty(question)){
                return ResponseEntity.ok(question);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * POST
     * 回复陌生人问题
     * /tanhua/strangerQuestions
     * @param params
     * @return
     */
    @PostMapping("/strangerQuestions")
    public ResponseEntity<Object> replyQuestion(@RequestBody Map<String,Object> params){
        try {
            Long userId = Long.parseLong(params.get("userId").toString());
            String reply = params.get("reply").toString();

            Boolean replyQuestion = todayBestService.replyQuestion(userId,reply);
            if (replyQuestion){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 搜附近
     * /tanhua/search
     * @param gender 性别
     * @param distance 距离
     * @return
     */
    @GetMapping("search")
    public ResponseEntity<Object> queryNearUser(@RequestParam(value = "gender", required = false) String gender,
                                                          @RequestParam(value = "distance", defaultValue = "2000") Integer distance) {
        try {
            List<NearUserVo> list = todayBestService.queryNearUser(gender, distance);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * GET
     * 探花 -卡片展示列表
     * /tanhua/cards
     * @return
     */
    @GetMapping("cards")
    public ResponseEntity<List<TodayBest>> queryCardsList() {
        try {
            List<TodayBest> list =todayBestService.queryCardsList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 探花-右划喜欢
     * /tanhua/:id/love
     * @param likeUserId
     * @return
     */
    @GetMapping("{id}/love")
    public ResponseEntity<Void> likeUser(@PathVariable("id") Long likeUserId) {
        try {
            todayBestService.likeUser(likeUserId);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * GET
     * 探花-左划不喜欢
     * /tanhua/:id/unlove
     * @param likeUserId
     * @return
     */
    @GetMapping("{id}/unlove")
    public ResponseEntity<Void> disLikeUser(@PathVariable("id") Long likeUserId) {
        try {
            todayBestService.disLikeUser(likeUserId);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


}
