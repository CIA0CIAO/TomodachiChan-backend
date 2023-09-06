package com.tomodachi.controller;

import com.tomodachi.common.exception.BusinessException;
import com.tomodachi.controller.response.ErrorCode;
import com.tomodachi.controller.response.BaseResponse;
import com.tomodachi.entity.dto.UserLogin;
import com.tomodachi.entity.dto.UserLoginForm;
import com.tomodachi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        if (Strings.isBlank(email)|| Strings.isBlank(verificationCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入邮箱和验证码");
        return BaseResponse.success(userService.loginByVerificationCode(email, verificationCode));
    }

}
