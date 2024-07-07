package com.jiang.duckojbackendserviceclient.service;

import com.jiang.duckojbackendcommon.common.ErrorCode;
import com.jiang.duckojbackendcommon.constant.UserConstant;
import com.jiang.duckojbackendcommon.exception.BusinessException;
import com.jiang.duckojbackendmodel.model.entity.User;
import com.jiang.duckojbackendmodel.model.enums.UserRoleEnum;
import com.jiang.duckojbackendmodel.model.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

/**
 * 公共服务接口，内部调用
 */

@FeignClient(name = "duckoj-backend-user-service", path = "/api/user/inner")
public interface UserOpenFeignClient {
    /**
     * 根据用户id获取用户：
     *
     * @param userId
     * @return
     */
    @GetMapping("/get/id")
    User getById(@RequestParam("userId") Long userId);


    /**
     * 获取用户列表：
     *
     * @param idList
     * @return
     */
    @GetMapping("/get/ids")
    List<User> listByIds(@RequestParam("idList") Collection<Long> idList);


    /**
     * 返回UserVO,默认实现
     *
     * @param user
     * @return
     */
    default UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }


    /**
     * 获取登录用户
     *
     * @param request
     * @return
     */
    default User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    default boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

}
