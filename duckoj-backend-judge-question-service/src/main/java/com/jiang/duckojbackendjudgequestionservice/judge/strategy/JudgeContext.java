package com.jiang.duckojbackendjudgequestionservice.judge.strategy;


import com.jiang.duckojbackendmodel.model.dto.question.JudgeCase;
import com.jiang.duckojbackendmodel.model.entity.Question;
import com.jiang.duckojbackendmodel.model.entity.QuestionSubmit;
import com.jiang.duckojbackendmodel.model.judge.JudgeInfo;
import lombok.Data;

import java.util.List;

/**
 * 判题上下文，类似于DTO
 */

@Data
public class JudgeContext {

    private List<JudgeCase> judgeCaseList;

    private List<String> judgeCaseInput;

    private List<String> judgeCaseOutput;

    private List<String> outputList;

    private JudgeInfo judgeInfo;

    private QuestionSubmit questionSubmit;

    private Question question;
}
