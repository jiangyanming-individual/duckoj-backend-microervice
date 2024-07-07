package com.jiang.duckojbackendjudgequestionservice.judge.codesandbox;


import com.jiang.duckojbackendmodel.model.judge.ExecuteCodeRequest;
import com.jiang.duckojbackendmodel.model.judge.ExecuteCodeResponse;

public interface CodeSandBox {

    /**
     * 执行代码：
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest);
}
