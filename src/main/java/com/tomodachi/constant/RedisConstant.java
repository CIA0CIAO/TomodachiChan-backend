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
}
