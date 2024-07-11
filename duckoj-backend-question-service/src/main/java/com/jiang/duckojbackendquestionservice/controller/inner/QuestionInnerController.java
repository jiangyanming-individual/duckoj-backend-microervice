package com.jiang.duckojbackendquestionservice.controller.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiang.duckojbackendmodel.model.entity.Question;
import com.jiang.duckojbackendmodel.model.entity.QuestionSubmit;
import com.jiang.duckojbackendquestionservice.service.QuestionService;
import com.jiang.duckojbackendquestionservice.service.QuestionSubmitService;
import com.jiang.duckojbackendserviceclient.service.QuestionOpenFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 内部接口
 */
@RestController
@RequestMapping("/inner")
@Slf4j
public class QuestionInnerController implements QuestionOpenFeignClient {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Override
    @GetMapping("/question/getById")
    public Question getQuestionById(@RequestParam("questionId") Long questionId) {
        return questionService.getById(questionId);
    }


    @Override
    @PostMapping("/question/updateById")
    public boolean updateQuestionById(@RequestBody Question question) {
        return questionService.updateById(question);
    }


    @Override
    @GetMapping("/question_submit/getById")
    public QuestionSubmit getQuestionSubmitById(long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);
    }


    @Override
    @PostMapping("/question_submit/update")
    public boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmitUpdate) {
        return questionSubmitService.updateById(questionSubmitUpdate);
    }

    @Override
    @GetMapping("/question_submit/list")
    public List<QuestionSubmit> getQuestionSubmitList(QueryWrapper queryWrapper) {
        return questionSubmitService.list(queryWrapper);
    }
}
