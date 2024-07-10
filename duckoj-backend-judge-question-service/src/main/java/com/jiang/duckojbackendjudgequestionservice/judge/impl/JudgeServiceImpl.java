package com.jiang.duckojbackendjudgequestionservice.judge.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiang.duckojbackendcommon.common.ErrorCode;
import com.jiang.duckojbackendcommon.exception.BusinessException;
import com.jiang.duckojbackendjudgequestionservice.judge.JudgeService;
import com.jiang.duckojbackendjudgequestionservice.judge.codesandbox.CodeSandBox;
import com.jiang.duckojbackendjudgequestionservice.judge.codesandbox.CodeSandBoxFactory;
import com.jiang.duckojbackendjudgequestionservice.judge.codesandbox.CodeSandBoxProxy;
import com.jiang.duckojbackendjudgequestionservice.judge.strategy.JudgeContext;
import com.jiang.duckojbackendjudgequestionservice.judge.strategy.JudgeManager;
import com.jiang.duckojbackendmodel.model.dto.question.JudgeCase;
import com.jiang.duckojbackendmodel.model.entity.Question;
import com.jiang.duckojbackendmodel.model.entity.QuestionSubmit;
import com.jiang.duckojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.jiang.duckojbackendmodel.model.judge.ExecuteCodeRequest;
import com.jiang.duckojbackendmodel.model.judge.ExecuteCodeResponse;
import com.jiang.duckojbackendmodel.model.judge.JudgeInfo;
import com.jiang.duckojbackendserviceclient.service.QuestionOpenFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 判题实现服务
 */
@Service
public class JudgeServiceImpl implements JudgeService {

    private static final int number = 1;

    @Resource
    private QuestionOpenFeignClient questionOpenFeignClient;

    @Value("${codesandbox.type}")
    private String type;


    @Resource
    private JudgeManager judgeManager;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        //(1) 传入判题的id，获取到对应判题题目、判题语言、判题内容、提交信息
        QuestionSubmit questionSubmit = questionOpenFeignClient.getQuestionSubmitById(questionSubmitId);
        //参数校验：
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交题目为空");
        }
        long questionId = questionSubmit.getQuestionId();
        Question question = questionOpenFeignClient.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目不存在");
        }
        //(2) 如果题目的提交状态不为待判题，就不用重复提交判题 （只有待判题是真正需要判题的）
        if (!questionSubmit.getSubmitState().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目在判题中");
        }
        //(3) 如果判题状态不为判题中，更改判题状态为判题中。
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setSubmitState(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionOpenFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新题目提交状态失败");
        }
        //(4) 调用代码沙箱，得到判题结果。
        //工厂+ 代理模式：
        CodeSandBoxFactory codeSandBoxFactory = new CodeSandBoxFactory();
        CodeSandBox exampleCodeSandBox = codeSandBoxFactory.getInstance(type);
        CodeSandBoxProxy codeSandBoxProxy = new CodeSandBoxProxy(exampleCodeSandBox);
        String submitLanguage = questionSubmit.getSubmitLanguage();
        String submitCode = questionSubmit.getSubmitCode();
        //题目判题用例：
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        //判题输入用例：
        List<String> judgeCaseInput = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        //判题输出用例：
        List<String> judgeCaseOutput = judgeCaseList.stream().map(JudgeCase::getOutput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .submitCode(submitCode)
                .submitLanguage(submitLanguage)
                .inputList(judgeCaseInput).build();
        ExecuteCodeResponse executeCodeResponse = codeSandBoxProxy.doExecute(executeCodeRequest);

        //(5) 判题机根据代码沙箱的输出进行判题
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setJudgeCaseInput(judgeCaseInput);
        judgeContext.setJudgeCaseOutput(judgeCaseOutput);
        //获取代码沙箱输出结果
        judgeContext.setOutputList(executeCodeResponse.getOutputList());
        //获得代码沙箱输出的判题的信息：
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setQuestionSubmit(questionSubmit);
        judgeContext.setQuestion(question);
        //使用策略管理，进行判题操作：选择不同的判题策略
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        //(6) 更新提交题目状态以及判题的状态
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        //判题机的状态：
        questionSubmitUpdate.setSubmitState(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        //更新题目提交的状态:
        update = questionOpenFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新题目提交状态失败");
        }
        //(7) 更新总的题目提交数：
        QueryWrapper<QuestionSubmit> questionSubmitQueryWrapper = new QueryWrapper<>();
        questionSubmitQueryWrapper.eq("questionId", questionId);
        //统计总的提交数：
        int submitNum = (int) questionOpenFeignClient.getQuestionSubmitList(questionSubmitQueryWrapper).stream().count();
        List<QuestionSubmit> questionSubmitList = questionOpenFeignClient.getQuestionSubmitList(questionSubmitQueryWrapper).stream().collect(Collectors.toList());
        //按照状态进行分组：
        Map<Integer, List<QuestionSubmit>> submitQuestionMap = questionSubmitList.stream().collect(Collectors.groupingBy(QuestionSubmit::getSubmitState));
        int acceptedNum = submitQuestionMap.get(QuestionSubmitStatusEnum.SUCCEED.getValue()).size();
        //更新题目的状态：
        question.setSubmitNum(submitNum + number);
        question.setAcceptedNum(acceptedNum + number);
        boolean b = questionOpenFeignClient.updateQuestionById(question);
        if (!b) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新题目提交数失败");
        }
        //返回提交题目信息
        return questionOpenFeignClient.getQuestionSubmitById(questionSubmitId);
    }
}
