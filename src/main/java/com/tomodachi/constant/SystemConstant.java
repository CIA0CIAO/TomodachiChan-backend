package com.tomodachi.constant;

/**
 * 系统常量
 */
public class SystemConstant {
    /**
     * 默认分页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 20;
    /**
     * 最大分页大小
     */
    public static final Integer MAX_PAGE_SIZE = 1000;

    /**
     * 登录状态验证请求头
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";
    /**
     * 文件公共前缀
     */
    public static final String FILE_COMMON_PREFIX = "Tomodachi";

    /**
     * 用户账号前缀
     */
    public static final String USER_ACCOUNT_PREFIX = "user_";

    /**
     * 验证手机号正则表达式
     */
    public static final String PHONE_NUMBER_REGEX = "^1[3-9]\\d{9}$";
    /**
     * 验证邮箱正则表达式
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$";
    /**
     * 验证用户昵称正则表达式
     */
    public static final String USERNAME_REGEX = "^[\\u4e00-\\u9fa5_a-zA-Z0-9]{2,10}$";
    /**
     * 验证队伍名称正则表达式
     */
    public static final String TEAM_NAME_REGEX = "^[\\u4e00-\\u9fa5_a-zA-Z0-9]{2,20}$";

    /**
     * 用户加入的队伍数量上限
     */
    public static final Integer JOINED_TEAM_LIMIT = 10;
    /**
     * 用户拥有的队伍数量上限
     */
    public static final Integer OWNED_TEAM_LIMIT = 5;
}

