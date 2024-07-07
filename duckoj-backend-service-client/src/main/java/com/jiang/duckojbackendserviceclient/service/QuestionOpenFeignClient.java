package com.jiang.duckojbackendserviceclient.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiang.duckojbackendmodel.model.entity.Question;
import com.jiang.duckojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
* @author jiangyanming
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2024-06-21 17:43:56
*/

@FeignClient(name = "duckoj-backend-question-service", path = "/api/question/inner")
public interface QuestionOpenFeignClient{

//    Question question = questionService.getById(questionId);
//    boolean b = questionService.updateById(question);
//    QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
//    boolean update = questionSubmitService.updateById(questionSubmitUpdate);
//    questionSubmitService.list

    @GetMapping("/question/getById")
    Question getQuestionById(@RequestParam("questionId") Long questionId);


    @PostMapping("/question/updateById")
    boolean updateQuestionById(@RequestBody Question question);


    @GetMapping("/question_submit/getById")
    QuestionSubmit getQuestionSubmitById(Long questionSubmitId);


    @PostMapping("/question_submit/update")
    boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmitUpdate);

    @GetMapping("/question_submit/list")
    List<QuestionSubmit> getQuestionSubmitList(QueryWrapper queryWrapper);
}
