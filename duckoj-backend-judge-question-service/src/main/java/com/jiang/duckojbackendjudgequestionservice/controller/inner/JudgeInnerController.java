package com.jiang.duckojbackendjudgequestionservice.controller.inner;


import com.jiang.duckojbackendjudgequestionservice.judge.JudgeService;
import com.jiang.duckojbackendmodel.model.entity.QuestionSubmit;
import com.jiang.duckojbackendserviceclient.service.JudgeOpenFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 使用rabbitmq 替换这一步：
 */
@RestController
@RequestMapping("/inner")
@Slf4j
public class JudgeInnerController implements JudgeOpenFeignClient {

    @Resource
    private JudgeService judgeService;

    @Override
    @PostMapping("/do")
    public QuestionSubmit doJudge(long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }
}
