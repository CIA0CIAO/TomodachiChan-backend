package com.tomodachi.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomodachi.common.UserContext;
import com.tomodachi.common.exception.BusinessException;
import com.tomodachi.common.role.RoleCheck;
import com.tomodachi.controller.response.ErrorCode;
import com.tomodachi.controller.response.BaseResponse;
import com.tomodachi.entity.User;
import com.tomodachi.entity.dto.UserLogin;
import com.tomodachi.entity.dto.UserLoginForm;
import com.tomodachi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户接口")
public class UserController {
    @Resource
    private UserService userService;

    @Operation(summary = "发送验证码")
    @PostMapping("/code")
    public BaseResponse<String> sendVerificationCode(@RequestBody UserLoginForm userLoginForm) {
        String email = userLoginForm.getEmail();
        if (Strings.isBlank(email))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入邮箱");
        userService.sendVerificationCode(email);
        return BaseResponse.success("验证码发送成功");
    }

    @Operation(summary = "用户通过验证码登录")
    @PostMapping("/login/byCode")
    public BaseResponse<UserLogin> loginByVerificationCode(@RequestBody UserLoginForm userLoginForm) {
        String email = userLoginForm.getEmail();
        String verificationCode = userLoginForm.getVerificationCode();
        if (Strings.isBlank(email) || Strings.isBlank(verificationCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入邮箱和验证码");
        return BaseResponse.success(userService.loginByVerificationCode(email, verificationCode));
    }

    @Operation(summary = "用户通过密码登录")
    @PostMapping("/login/byPassword")
    public BaseResponse<UserLogin> loginByPassword(@RequestBody UserLoginForm userLoginForm) {
        String email = userLoginForm.getEmail();
        String password = userLoginForm.getPassword();
        if (Strings.isBlank(email) || Strings.isBlank(password))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入邮箱和密码");
        return BaseResponse.success(userService.loginByPassword(email, password));
    }

    @RoleCheck
    @Operation(summary = "用户获取账号信息")
    @GetMapping("/account")
    public BaseResponse<User> getAccountInfo() {
        return BaseResponse.success(userService.getAccountInfo());
    }

    @RoleCheck
    @Operation(summary = "用户发送验证码 (已登录)")
    @PostMapping("/account/code")
    public BaseResponse<String> sendVerificationCode() {
        userService.sendVerificationCode(UserContext.getEmail());
        return BaseResponse.success("验证码发送成功");
    }

    @RoleCheck
    @Operation(summary = "用户更换邮箱")
    @PutMapping("/account/email")
    public BaseResponse<String> updateEmail(@RequestBody UserLoginForm userLoginForm) {
        String email = userLoginForm.getEmail();
        String verificationCode = userLoginForm.getVerificationCode();
        if (Strings.isBlank(email) || Strings.isBlank(verificationCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入邮箱和验证码");
        userService.updateEmail(email, verificationCode);
        return BaseResponse.success("邮箱更换成功");
    }

    @RoleCheck
    @Operation(summary = "用户设置密码")
    @PutMapping("/account/password")
    public BaseResponse<String> updatePassword(@RequestBody UserLoginForm userLoginForm) {
        String password = userLoginForm.getPassword();
        String verificationCode = userLoginForm.getVerificationCode();
        if (Strings.isBlank(password) || Strings.isBlank(verificationCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入邮箱和密码");
        userService.updatePassword(password, verificationCode);
        return BaseResponse.success("密码更换成功");
    }

    @RoleCheck
    @Operation(summary = "用户更新基本信息")
    @PutMapping("/account/basic")
    public BaseResponse<String> updateBasicInfo(@RequestBody User user) {
        if (user == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息为空");
        userService.updateBasicInfo(user);
        return BaseResponse.success("用户信息更新成功");
    }

    @Operation(summary = "查询指定用户的信息")
    @GetMapping("/{userId}")
    public BaseResponse<User> queryByUserId(@PathVariable Long userId) {
        if (userId == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要查询的用户");
        return BaseResponse.success(userService.queryByUserId(userId));
    }

    @Operation(summary = "根据标签分页查询用户")
    @GetMapping("/tags")
    public BaseResponse<Page<User>> queryByIdsWithCache(@RequestParam Set<String> tags, Integer currentPage) {
        if(tags == null || tags.isEmpty())
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要查询的标签");
        if(currentPage == null || currentPage < 1)
            currentPage = 1;
        return BaseResponse.success(userService.queryByTagsWithPagination(tags, currentPage));
    }
}
