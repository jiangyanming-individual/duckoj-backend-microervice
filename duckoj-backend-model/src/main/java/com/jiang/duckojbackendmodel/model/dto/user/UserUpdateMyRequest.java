package com.jiang.duckojbackendmodel.model.dto.user;

import lombok.Data;

import java.io.Serializable;


/**
 * 用户更新个人信息：
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 性别
     */
    private String gender;

    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private String phone;


    private static final long serialVersionUID = 1L;
}