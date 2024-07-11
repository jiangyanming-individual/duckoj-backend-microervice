package com.jiang.duckojbackenduserservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiang.duckojbackendcommon.annotation.AuthCheck;
import com.jiang.duckojbackendcommon.common.BaseResponse;
import com.jiang.duckojbackendcommon.common.DeleteRequest;
import com.jiang.duckojbackendcommon.common.ErrorCode;
import com.jiang.duckojbackendcommon.common.ResultUtils;
import com.jiang.duckojbackendcommon.constant.UserConstant;
import com.jiang.duckojbackendcommon.exception.BusinessException;
import com.jiang.duckojbackendcommon.exception.ThrowUtils;
import com.jiang.duckojbackendcommon.utils.ValidEmailUtils;
import com.jiang.duckojbackendcommon.utils.ValidPhoneUtils;
import com.jiang.duckojbackendmodel.model.dto.user.*;
import com.jiang.duckojbackendmodel.model.entity.User;
import com.jiang.duckojbackendmodel.model.vo.LoginUserVO;
import com.jiang.duckojbackendmodel.model.vo.UserVO;
import com.jiang.duckojbackenduserservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.jiang.duckojbackendcommon.constant.SALTConstant.SALT;


/**
 * 用户接口
 */
@RestController
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //参数校验
        String userName = userAddRequest.getUserName();
        String userAccount = userAddRequest.getUserAccount();
        String userPassword = userAddRequest.getUserPassword();
        String gender = userAddRequest.getGender();
        String email = userAddRequest.getEmail();
        String phone = userAddRequest.getPhone();
        String userState = userAddRequest.getUserState();
        String userAvatar = userAddRequest.getUserAvatar();
        String userRole = userAddRequest.getUserRole();


        if (StringUtils.isAnyBlank(userAccount, userName, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度不能小于4");
        }


        //校验邮箱和密码：
        if (userPassword.length() < 8 || userPassword.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不符合要求");
        }
        if (userAvatar.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像不符合要求");
        }
        if (StringUtils.isNotBlank(email)) {

            boolean validate = ValidEmailUtils.validate(email);
            if (!validate) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不合法");
            }
        }
        if (StringUtils.isNotBlank(phone)) {
            boolean validate = ValidPhoneUtils.validate(phone);
            if (!validate) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不合法");
            }
        }

        // 2. 密码进行加密操作
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        //加密密码：
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userUpdateRequest.getId();
        String userName = userUpdateRequest.getUserName();
        String userAvatar = userUpdateRequest.getUserAvatar();
        String userProfile = userUpdateRequest.getUserProfile();
        String gender = userUpdateRequest.getGender();
        String email = userUpdateRequest.getEmail();
        String phone = userUpdateRequest.getPhone();
        String userState = userUpdateRequest.getUserState();
        String userRole = userUpdateRequest.getUserRole();


        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id不合法");
        }
        if (userAvatar.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像不合法");
        }
        if (userName.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名过长");
        }

        if (StringUtils.isNotBlank(email)) {

            boolean validate = ValidEmailUtils.validate(email);
            if (!validate) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不合法");
            }
        }
        if (StringUtils.isNotBlank(phone)) {
            boolean validate = ValidPhoneUtils.validate(phone);
            if (!validate) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不合法");
            }
        }

        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userName = userUpdateMyRequest.getUserName();
        String userAvatar = userUpdateMyRequest.getUserAvatar();
        String userProfile = userUpdateMyRequest.getUserProfile();
        String gender = userUpdateMyRequest.getGender();
        String email = userUpdateMyRequest.getEmail();
        String phone = userUpdateMyRequest.getPhone();
        if (userName.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名不合法");
        }
        if (userProfile.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户个人简介不合法");
        }
        //校验邮箱和手机号
        if (StringUtils.isNotBlank(email)) {
            boolean validate = ValidEmailUtils.validate(email);
            if (!validate) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不合法");
            }
        }
        if (StringUtils.isNotBlank(phone)) {
            boolean validate = ValidPhoneUtils.validate(phone);
            if (!validate) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不合法");
            }
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
