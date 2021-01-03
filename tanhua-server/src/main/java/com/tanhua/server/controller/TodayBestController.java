package com.tanhua.server.controller;

import com.tanhua.server.service.TodayBestService;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.TodayBest;
import com.tanhua.server.vo.params.RecommendUserQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Object> todayBest(@RequestHeader("Authorization")String token){

        try {

            TodayBest todayBest = todayBestService.todayBest(token);
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
     * @param token
     * @return
     */
    @GetMapping("recommendation")
    public ResponseEntity<Object> recommendation(@RequestHeader("Authorization")String token,
                                                 RecommendUserQueryParam queryParam){
        try {

            PageResult pageResult = todayBestService.recommendation(token, queryParam);
            return ResponseEntity.ok(pageResult);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

}
