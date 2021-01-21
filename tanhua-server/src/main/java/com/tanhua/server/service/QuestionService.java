package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.server.mapper.QuestionMapper;
import com.tanhua.server.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author Administrator
 * @create 2021/1/14 0:25
 */
@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 根据id,查询该用户的问题信息
     * @param userId
     * @return
     */
    public Question queryQuestion(Long userId){

        QueryWrapper queryWrapper =new QueryWrapper();
        queryWrapper.eq("user_id",userId);
        queryWrapper.last("limit 1");

        return questionMapper.selectOne(queryWrapper);
    }

    /**
     * 保存用户问题
     * @param question
     * @return
     */
    public Boolean saveQuestions(Question question){
        return questionMapper.insert(question)>0;
    }

    /**
     * 根据id更新用户问题
     * @param question
     * @return
     */
    public Boolean updateQuestion(Question question){
        return questionMapper.updateById(question)>0;
    }
}
