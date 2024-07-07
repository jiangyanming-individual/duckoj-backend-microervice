package com.jiang.duckojbackendjudgequestionservice.judge.codesandbox.impl;


import com.jiang.duckojbackendjudgequestionservice.judge.codesandbox.CodeSandBox;
import com.jiang.duckojbackendmodel.model.judge.ExecuteCodeRequest;
import com.jiang.duckojbackendmodel.model.judge.ExecuteCodeResponse;

/**
 * 第三方代码沙箱
 */
public class ThirdPartyCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
