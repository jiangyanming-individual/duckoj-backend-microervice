package com.jiang.duckojbackendjudgequestionservice.judge.strategy;


import com.jiang.duckojbackendmodel.model.judge.JudgeInfo;

/**
 * 判题策略：
 */
public interface JudgeStrategy {

    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
