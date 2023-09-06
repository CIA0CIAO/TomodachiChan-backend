package com.tomodachi.common.role;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum Role {
    GUEST(-1),
    USER(0),
    ADMIN(1);

    private final Integer level;

    /**
     * 根据用户状态获取对应用户角色枚举
     *
     * @param userStatus 用户状态
     * @return 用户角色枚举
     */
    public static Role getRole(Integer userStatus) {
        for (Role r : Role.values())
            if (r.getLevel().equals(userStatus))
                return r;
        // 字符串无匹配时返回游客角色
        return GUEST;
    }

    /**
     * 判断用户是否拥有权限访问接口
     *
     * @param requiredRole 接口所需权限
     * @return 是否拥有权限
     */
    public boolean hasPermission(Role requiredRole) {
        return this.getLevel() >= requiredRole.getLevel();
    }
}
