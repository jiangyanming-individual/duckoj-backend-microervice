package com.jiang.duckojbackenduserservice.controller.inner;

import com.jiang.duckojbackendmodel.model.entity.User;
import com.jiang.duckojbackendserviceclient.service.UserOpenFeignClient;
import com.jiang.duckojbackenduserservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 内部接口实现类：
 */
@RestController
@RequestMapping("/inner")
@Slf4j
public class UserInnerController implements UserOpenFeignClient {

    @Resource
    private UserService userService;

    /**
     * 根据用户id获取用户：
     *
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/get/id")
    public User getById(@RequestParam("userId") Long userId) {
        return userService.getById(userId);
    }


    /**
     * 获取用户列表：
     *
     * @param idList
     * @return
     */
    @Override
    @GetMapping("/get/ids")
    public List<User> listByIds(@RequestParam("idList") Collection<Long> idList) {
        return userService.listByIds(idList);
    }
}
