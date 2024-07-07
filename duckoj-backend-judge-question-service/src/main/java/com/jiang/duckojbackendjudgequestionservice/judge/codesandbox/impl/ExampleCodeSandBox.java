package com.jiang.duckojbackendjudgequestionservice.judge.codesandbox.impl;


import com.jiang.duckojbackendjudgequestionservice.judge.codesandbox.CodeSandBox;
import com.jiang.duckojbackendmodel.model.enums.JudgeInfoMessageEnum;
import com.jiang.duckojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.jiang.duckojbackendmodel.model.judge.ExecuteCodeRequest;
import com.jiang.duckojbackendmodel.model.judge.ExecuteCodeResponse;
import com.jiang.duckojbackendmodel.model.judge.JudgeInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 远程沙箱实现，调用远程开发的代码沙箱服务接口：
 */
@Slf4j
public class ExampleCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest) {
        log.info("执行示例代码沙箱");
        //获取请求信息：
        List<String> inputList = executeCodeRequest.getInputList();
        String submitLanguage = executeCodeRequest.getSubmitLanguage();
        String submitCode = executeCodeRequest.getSubmitCode();
        //返回判题信息的结果：
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("判题成功");
        //提交成功：
        executeCodeResponse.setSubmitState(QuestionSubmitStatusEnum.SUCCEED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setTime(100L);
        judgeInfo.setMemory(100L);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        return executeCodeResponse;
    }
}
