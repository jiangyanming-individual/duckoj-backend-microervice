package com.jiang.duckojbackendjudgequestionservice.judge.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.jiang.duckojbackendjudgequestionservice.judge.strategy.JudgeContext;
import com.jiang.duckojbackendjudgequestionservice.judge.strategy.JudgeStrategy;
import com.jiang.duckojbackendmodel.model.dto.question.JudgeCase;
import com.jiang.duckojbackendmodel.model.dto.question.JudgeConfig;
import com.jiang.duckojbackendmodel.model.entity.Question;
import com.jiang.duckojbackendmodel.model.entity.QuestionSubmit;
import com.jiang.duckojbackendmodel.model.enums.JudgeInfoMessageEnum;
import com.jiang.duckojbackendmodel.model.judge.JudgeInfo;

import java.util.List;
import java.util.Optional;

/**
 * java的判题策略
 */
public class JavaJudgeStrategy implements JudgeStrategy {

    /**
     * 判题
     * @param judgeContext
     * @return
     */
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        // 1. 判断输入用例和代码沙箱的输出个数是否相等。
        //设置判题的状态：
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        List<String> judgeCaseInput = judgeContext.getJudgeCaseInput();
        List<String> judgeCaseOutput = judgeContext.getJudgeCaseOutput();
        List<String> outputList = judgeContext.getOutputList();
        //判题输出信息：
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        //运行时间
        Long runTime = Optional.ofNullable(judgeInfo.getTime()).orElse(0L);
        //运行内存
        Long runMemory = Optional.ofNullable(judgeInfo.getMemory()).orElse(0L);
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        Question question = judgeContext.getQuestion();
        //设置返回对象：
        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setTime(runTime);
        judgeInfoResponse.setMemory(runMemory);

        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        if (judgeCaseInput.size() != outputList.size()) {
            //设置判题结果信息
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        // 2. 判断每个输出用例和代码沙箱的输出是否相等，如果不相等直接返回。
        for (int i = 0; i < judgeCaseOutput.size(); i++) {
            if (!judgeCaseOutput.get(i).equals(outputList.get(i))) {
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResponse;
            }
        }
        // 3. 判断题目的限制是否符合要求。
        // 3.1题目设置的判题限制：
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig expectJudgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long needTimeLimit = expectJudgeConfig.getTimeLimit();
        Long needeMoryLimit = expectJudgeConfig.getMemoryLimit();

        //3.2 java 策略额外需要花费10s:
        final long JAVA_COST_TIME = 10000L;
        if ((runTime - JAVA_COST_TIME) > needTimeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        if (runMemory > needeMoryLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        //3.3 成功后返回判题成功的信息：
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        return judgeInfoResponse;
    }
}
