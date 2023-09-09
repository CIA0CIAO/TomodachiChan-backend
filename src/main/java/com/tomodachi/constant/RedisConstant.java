package com.tomodachi.constant;

import java.time.Duration;

/**
 * Redis 常量
 */
public class RedisConstant {
    /**
     * 用户登录状态 Token
     */
    public static final String USER_TOKEN_KEY = "user:token:";
    public static final Duration USER_TOKEN_TTL = Duration.ofDays(7);

    /**
     * 缓存用户发送的验证码
     */
    public static final String USER_CODE_KEY = "user:code:";
    public static final Duration USER_CODE_TTL = Duration.ofMinutes(5);

    /**
     * 缓存用户信息
     */
    public static final String USER_INFO_KEY = "user:info:";
    public static final Duration USER_INFO_TTL = Duration.ofDays(1);

    /**
     * 缓存标签关联的用户集合
     */
    public static final String TAGS_KEY = "tags:";
    public static final Duration TAGS_TTL = Duration.ofDays(3);

    /**
     * 缓存用户查询的标签结果
     */
    public static final String SEARCH_TAGS_KEY = "search:tags:";
    public static final Duration SEARCH_TAGS_TTL = Duration.ofHours(1);
}
