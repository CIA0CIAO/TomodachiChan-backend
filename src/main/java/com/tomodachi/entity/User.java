package com.tomodachi.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@Accessors(chain = true)
public class User {
    /**
     * 主键
     */
    private Long id;
    /**
     * 账号
     */
    private String account;
    /**
     * 昵称
     */
    private String username;
    /**
     * 头像 URL
     */
    private String avatarUrl;
    /**
     * 性别 (0 - 未知, 1 - 男, 2 - 女)
     */
    private Integer gender;
    /**
     * 手机
     */
    private String phone;
    /**
     * 密码
     */
    private String password;
    /**
     * 用户状态 (0 - 正常, 1 - 管理员)
     */
    private Integer status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 逻辑删除
     */
    private Integer isDeleted;
}
