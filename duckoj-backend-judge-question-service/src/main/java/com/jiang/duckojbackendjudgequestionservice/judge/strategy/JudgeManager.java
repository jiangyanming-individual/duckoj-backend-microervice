package com.jiang.duckojbackendjudgequestionservice.judge.strategy;


import com.jiang.duckojbackendjudgequestionservice.judge.strategy.impl.DefaultJudgeStrategy;
import com.jiang.duckojbackendjudgequestionservice.judge.strategy.impl.JavaJudgeStrategy;
import com.jiang.duckojbackendmodel.model.entity.QuestionSubmit;
import com.jiang.duckojbackendmodel.model.judge.JudgeInfo;
import org.springframework.stereotype.Service;

/**
 * 策略模式管理类，根据不同的语言进行选择判题机
 * 将JudgeManager注入到Bean 中
 */
@Service
public class JudgeManager {
    //根据不同的语言进行判题的操作：
    public JudgeInfo doJudge(JudgeContext judgeContext){
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String submitLanguage = questionSubmit.getSubmitLanguage();
        if ("java".equals(submitLanguage)){
            judgeStrategy=new JavaJudgeStrategy();
        }
        //执行判题：
        return judgeStrategy.doJudge(judgeContext);
    }
}
