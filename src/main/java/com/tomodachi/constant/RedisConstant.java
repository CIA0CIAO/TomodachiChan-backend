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
     * 缓存使用的分布式锁标识
     */
    public static final String LOCK_CACHE_TAGS_KEY = "lock:cache:tags";

    /**
     * 缓存标签关联的用户集合
     */
    public static final String TAGS_KEY = "tags:";
    public static final Duration TAGS_TTL = Duration.ofDays(3);

    /**
     * 缓存热门搜索标签
     */
    public static final String SEARCH_HOT_KEY = "search:hot:";
    public static final Duration SEARCH_HOT_TTL = Duration.ofDays(7);

    /**
     * 缓存用户查询的标签结果
     */
    public static final String SEARCH_TAGS_KEY = "search:tags:";
    public static final Duration SEARCH_TAGS_TTL = Duration.ofHours(1);


    /**
     * 加入队伍使用的分布式锁标识
     */
    public static final String LOCK_TEAM_JOIN_KEY = "lock:team:join:";

    /**
     * 入队邀请信息
     */
    public static final String TEAM_INVITATION_KEY = "team:invitation:";
    public static final Duration TEAM_INVITATION_TTL = Duration.ofDays(7);

    /**
     * 队伍推荐缓存信息
     */
    public static final String TEAM_RECOMMEND_KEY = "team:recommend:";
    public static final Duration TEAM_RECOMMEND_TTL = Duration.ofHours(1);
    /**
     * 用户消息列表
     */
    public static final String USER_MESSAGE_KEY = "user:message:";
}
