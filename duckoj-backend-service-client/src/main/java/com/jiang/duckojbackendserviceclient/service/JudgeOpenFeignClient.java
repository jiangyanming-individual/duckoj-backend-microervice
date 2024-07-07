package com.jiang.duckojbackendserviceclient.service;

import com.jiang.duckojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 判题服务接口
 */
@FeignClient(name = "duckoj-backend-judge-question-service", path = "/api/judge/inner")
public interface JudgeOpenFeignClient {

    @PostMapping("/do")
    QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId);
}
