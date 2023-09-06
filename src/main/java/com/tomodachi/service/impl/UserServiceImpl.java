package com.tomodachi.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomodachi.common.exception.BusinessException;
import com.tomodachi.common.role.Role;
import com.tomodachi.controller.response.ErrorCode;
import com.tomodachi.entity.User;
import com.tomodachi.entity.dto.UserLogin;
import com.tomodachi.mapper.UserMapper;
import com.tomodachi.service.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import static com.tomodachi.constant.RedisConstant.*;
import static com.tomodachi.constant.SystemConstant.EMAIL_REGEX;
import static com.tomodachi.constant.SystemConstant.USER_ACCOUNT_PREFIX;

/**
 * @author CIA0CIA0
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2023-09-06 21:02:24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Value("${spring.mail.username}")
    String provider;
    @Resource
    MailSender mailSender;
    @Resource
    RedissonClient redissonClient;

    /**
     * 发送验证码
     */
    @Override
    public void sendVerificationCode(String email) {
        //校验邮箱
        if (!email.matches(EMAIL_REGEX)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        //生成验证码
        String code = RandomUtil.randomNumbers(6);
        //发送验证码
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(provider);
        message.setTo(email);
        message.setSubject("ニコニコ验证码");
        message.setText("您的验证码为：" + code + "。三分钟内有效，请及时完成注册!如果不是本人操作,请忽略");
        log.info("邮箱为 {} 的用户发送验证码成功: {}", email, code);
        mailSender.send(message);
        // 缓存验证码
        redissonClient.getBucket(USER_CODE_KEY + email)
                .set(code, USER_CODE_TTL);
    }

    @Override
    public UserLogin loginByVerificationCode(String email, String verificationCode) {
        // 校验手机号和验证码
        if (!email.matches(EMAIL_REGEX)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        // 校验验证码
        verifyAndDeleteCode(email, verificationCode);
        // 获取已注册用户信息或创建新用户
        User user = this.lambdaQuery()
                .eq(User::getEmail, email)
                .oneOpt()
                .orElseGet(() -> registerByEmail(email));
        log.info("邮箱为 {} 的用户使用验证码登录成功", email);
        return new UserLogin(getMaskedUser(user), getUserToken(user));
    }

    /**
     * 校验验证码并删除缓存
     */
    private void verifyAndDeleteCode(String email, String verificationCode) {
        // 校验验证码
        RBucket<String> codeBucket = redissonClient.getBucket(USER_CODE_KEY + email);
        String code = codeBucket.get();
        if (Strings.isBlank(code) || !code.equals(verificationCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        // 删除缓存验证码
        codeBucket.delete();
    }

    /**
     * 通过邮箱注册
     */
    private User registerByEmail(String email) {
        User user = new User();
        user.setAccount(USER_ACCOUNT_PREFIX + RandomUtil.randomString(10))
                .setPhone(email)
                .setStatus(Role.USER.getLevel());
        this.save(user);
        log.info("邮箱为 {} 的用户注册成功: {}", email, user.getAccount());
        return user;
    }
    /**
     * 获得脱敏后的用户信息
     */
    private User getMaskedUser(User user) {
        return new User().setId(user.getId())
                .setAccount(user.getAccount())
                .setUsername(user.getUsername())
                .setAvatarUrl(user.getAvatarUrl())
                .setGender(user.getGender())
                .setTags(user.getTags())
                .setProfile(user.getProfile())
                .setCreateTime(user.getCreateTime());
    }
    /**
     * 用户登录后生成 Token
     */
    private String getUserToken(User user) {
        // 生成 Token
        String token = UUID.randomUUID().toString(true);
        // 保存登录状态
        User tokenUser = new User().setId(user.getId())
                .setPhone(user.getPhone())
                .setStatus(user.getStatus());
        RBucket<User> userBucket = redissonClient.getBucket(USER_TOKEN_KEY + token);
        userBucket.set(tokenUser, USER_TOKEN_TTL);
        return token;
    }
}






