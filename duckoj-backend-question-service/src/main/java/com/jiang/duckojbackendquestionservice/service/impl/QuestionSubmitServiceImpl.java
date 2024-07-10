package com.jiang.duckojbackendquestionservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiang.duckojbackendcommon.common.ErrorCode;
import com.jiang.duckojbackendcommon.constant.CommonConstant;
import com.jiang.duckojbackendcommon.exception.BusinessException;
import com.jiang.duckojbackendcommon.utils.SqlUtils;
import com.jiang.duckojbackendmodel.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.jiang.duckojbackendmodel.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.jiang.duckojbackendmodel.model.entity.Question;
import com.jiang.duckojbackendmodel.model.entity.QuestionSubmit;
import com.jiang.duckojbackendmodel.model.entity.User;
import com.jiang.duckojbackendmodel.model.enums.QuestionSubmitLanguageEnum;
import com.jiang.duckojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.jiang.duckojbackendmodel.model.vo.QuestionSubmitVO;
import com.jiang.duckojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.jiang.duckojbackendquestionservice.mq.MyProducer;
import com.jiang.duckojbackendquestionservice.service.QuestionService;
import com.jiang.duckojbackendquestionservice.service.QuestionSubmitService;
import com.jiang.duckojbackendserviceclient.service.JudgeOpenFeignClient;
import com.jiang.duckojbackendserviceclient.service.UserOpenFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.jiang.duckojbackendcommon.constant.RabbitMQConstant.DIRECT_EXCHANGE;
import static com.jiang.duckojbackendcommon.constant.RabbitMQConstant.ROUTING_KEY;

/**
 * @author jiangyanming
 * @description 针对表【question_submit(提交题目表)】的数据库操作Service实现
 * @createDate 2024-06-21 17:43:59
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private UserOpenFeignClient userOpenFeignClient;
    @Resource
    private QuestionService questionService;

    @Resource
    @Lazy
    private JudgeOpenFeignClient judgeOpenFeignClient;

    @Resource
    private MyProducer myProducer;

    /***
     * 提交题目
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        long userId = loginUser.getId();
        //数据库中查询user
        User oldUser = userOpenFeignClient.getById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户不存在");
        }
        //参数校验：
        QuestionSubmit questionSubmit = new QuestionSubmit();
        long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        String submitLanguage = questionSubmitAddRequest.getSubmitLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(submitLanguage);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        String submitCode = questionSubmitAddRequest.getSubmitCode();
        //保存到对象中
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setSubmitCode(submitCode);
        questionSubmit.setSubmitLanguage(submitLanguage);
        questionSubmit.setUserId(userId);
        //提交题目的状态为判题：
        questionSubmit.setSubmitState(QuestionSubmitStatusEnum.WAITING.getValue());//设置提交状态
        questionSubmit.setJudgeInfo("{}");
        //插入数据：
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交题目失败");
        }
        //返回提交题目后的id：
        //进行判题操作
        Long questionSubmitId = questionSubmit.getId();

        //改用消息队列,发送消息
        myProducer.sendMessage(DIRECT_EXCHANGE,ROUTING_KEY,String.valueOf(questionSubmitId));
        //异步操作，不用管返回值的事：
//        CompletableFuture.runAsync(()->{
//            judgeOpenFeignClient.doJudge(questionSubmitId);
//        });
        return questionSubmitId;
    }

    /**
     * 拼接查询参数
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        //为空返回空的查询
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String submitLanguage = questionSubmitQueryRequest.getSubmitLanguage();
        Integer submitState = questionSubmitQueryRequest.getSubmitState();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();
        //拼接查询字段：
        queryWrapper.eq(StringUtils.isNotBlank(submitLanguage), "submitLanguage", submitLanguage) ;
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(submitState) != null, "submitState", submitState);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId),"userId",userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId),"questionId",questionId);
        //排序字段：
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),sortOrder.equals(CommonConstant.SORT_ORDER_ASC),sortField);
        return queryWrapper;
    }
    /**
     * 获取单个题目提交信息：
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        if (questionSubmit  == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"题目提交参数为空");
        }
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        long userId = loginUser.getId();
        //不是管理员而且不是提交者，不能查看代码：
        if (userId!=questionSubmitVO.getUserId() && !userOpenFeignClient.isAdmin(loginUser)){
            //代码进行脱敏
            questionSubmitVO.setSubmitCode(null);
        }
        return questionSubmitVO;

    }
    /**
     * 分页脱敏信息：
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage,User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        //如果为空返回空的分页数据：
        if (CollUtil.isEmpty(questionSubmitList)){
            return questionSubmitVOPage;
        }
        //关联查询数据；
//        Set<Long> userIdSet = questionSubmitList.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());
//        List<User> userList = userOpenFeignClient.listByIds(userIdSet);
//        Map<Long, List<User>> userIdUserListMap = userList.stream().collect(Collectors.groupingBy(User::getId));
//
//        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> {
//            QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
//            long userId = questionSubmit.getUserId();
//            User user = null;
//            //查询hashmap:
//            if (userIdUserListMap.containsKey(userId)) {
//                user = userIdUserListMap.get(userId).get(0);
//            }
//            UserVO userVO = userOpenFeignClient.getUserVO(user);
//            questionSubmitVO.setUserVO(userVO);
//            return questionSubmitVO;
//        }).collect(Collectors.toList());

        //调用上面的单个提交信息脱敏的api进行脱敏：
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> {
            return getQuestionSubmitVO(questionSubmit, loginUser);
        }).collect(Collectors.toList());
        //设置分页的数据：
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }
}




