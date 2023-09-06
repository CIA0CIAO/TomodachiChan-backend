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
}
