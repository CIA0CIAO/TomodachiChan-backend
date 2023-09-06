package com.tomodachi.common;

import com.tomodachi.entity.User;

/**
 * 用户上下文, 用于存储解析 Token 获得的用户信息
 */
public class UserContext {
    private static final ThreadLocal<User> THREAD_LOCAL = new ThreadLocal<>();

    public static User getUser() {
        return THREAD_LOCAL.get();
    }

    public static void setUser(User user) {
        THREAD_LOCAL.set(user);
    }

    public static void removeUser() {
        THREAD_LOCAL.remove();
    }

    public static Long getId() {
        return getUser().getId();
    }

    public static String getPhone() {
        return getUser().getPhone();
    }
}
