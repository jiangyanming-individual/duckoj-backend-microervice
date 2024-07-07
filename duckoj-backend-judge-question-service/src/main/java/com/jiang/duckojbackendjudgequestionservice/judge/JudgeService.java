package com.jiang.duckojbackendjudgequestionservice.judge;
import com.jiang.duckojbackendmodel.model.entity.QuestionSubmit;

/**
 * 判题服务接口
 */
public interface JudgeService {
    QuestionSubmit doJudge(long questionSubmitId);
}
