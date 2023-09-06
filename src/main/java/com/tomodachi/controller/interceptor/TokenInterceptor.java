package com.tomodachi.controller.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomodachi.common.UserContext;
import com.tomodachi.common.role.Role;
import com.tomodachi.common.role.RoleCheck;
import com.tomodachi.controller.response.ErrorCode;
import com.tomodachi.controller.response.BaseResponse;
import com.tomodachi.entity.User;
import com.tomodachi.constant.RedisConstant;
import com.tomodachi.constant.SystemConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Token 拦截器
 */
@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final RedissonClient redissonClient;
    /**
     * 拦截请求进行登录校验和权限校验
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        // 放行跨域预检请求
        if ("OPTIONS".equals(request.getMethod()))
            return true;
        // 放行静态资源请求
        if (handler instanceof HttpRequestHandler)
            return true;
        // 获取接口权限
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RoleCheck roleCheck = handlerMethod.getMethod().getAnnotation(RoleCheck.class);
        Role requiredRole = roleCheck == null ? Role.GUEST : roleCheck.value();
        // 放行无权限设置的接口
        if (requiredRole == Role.GUEST)
            return true;
        // 验证 Token
        String token = request.getHeader(SystemConstant.AUTHORIZATION_HEADER);
        if (Strings.isBlank(token)) {
            BaseResponse<Object> baseResponse = BaseResponse.error(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
            return setResponse(response, baseResponse, HttpServletResponse.SC_UNAUTHORIZED);
        }
        // 验证登录状态
        RBucket<User> userBucket = redissonClient.getBucket(RedisConstant.USER_TOKEN_KEY + token);
        User user = userBucket.get();
        if (user == null) {
            BaseResponse<Object> baseResponse = BaseResponse.error(ErrorCode.NOT_LOGIN_ERROR, "登录已过期");
            return setResponse(response, baseResponse, HttpServletResponse.SC_UNAUTHORIZED);
        }
        // 验证用户权限
        Role userRole = Role.getRole(user.getStatus());
        if (!userRole.hasPermission(requiredRole)) {
            BaseResponse<Object> baseResponse = BaseResponse.error(ErrorCode.AUTH_ERROR, "用户权限不足");
            return setResponse(response, baseResponse, HttpServletResponse.SC_FORBIDDEN);
        }
        // 刷新 Token 有效期
        userBucket.expire(RedisConstant.USER_TOKEN_TTL);
        // 存储用户信息并放行
        UserContext.setUser(user);
        return true;
    }

    /**
     * 请求处理完成后清除用户信息
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        UserContext.removeUser();
    }

    /**
     * 为响应设置响应体和状态码
     */
    private boolean setResponse(HttpServletResponse response, BaseResponse<Object> baseResponse, int status) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(baseResponse));
        response.setStatus(status);
        return false;
    }
}
