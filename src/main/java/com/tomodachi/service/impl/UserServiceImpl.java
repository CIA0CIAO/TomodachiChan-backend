package com.tomodachi.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomodachi.common.UserContext;
import com.tomodachi.common.exception.BusinessException;
import com.tomodachi.common.role.Role;
import com.tomodachi.controller.response.ErrorCode;
import com.tomodachi.entity.User;
import com.tomodachi.entity.dto.UserLogin;
import com.tomodachi.mapper.UserMapper;
import com.tomodachi.service.UserService;
import com.tomodachi.util.MD5Util;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.tomodachi.constant.RedisConstant.*;
import static com.tomodachi.constant.SystemConstant.*;

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
    MD5Util md5Util;
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

    /**
     * 通过验证码登录
     */
    @Override
    public UserLogin loginByVerificationCode(String email, String verificationCode) {
        // 校验邮箱和验证码
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
     * 通过密码登录
     */
    @Override
    public UserLogin loginByPassword(String email, String password) {
        // 校验邮箱
        if (!email.matches(EMAIL_REGEX)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        // 校验密码长度
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱或密码错误");
        }
        //加密密码
        String encryptedPassword = md5Util.encrypt(password);
        // 获取已注册用户信息或创建新用户
        User user = this.lambdaQuery()
                .eq(User::getEmail, email)
                .eq(User::getPassword, encryptedPassword)
                .oneOpt()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱或密码错误"));
        log.info("邮箱为 {} 的用户使用密码登录成功", email);
        return new UserLogin(getMaskedUser(user), getUserToken(user));
    }

    /**
     * 获取账号信息
     */
    @Override
    public User getAccountInfo() {
        String email = UserContext.getEmail();
        User user = this.lambdaQuery()
                .eq(User::getEmail, email)
                .one();
        return getMaskedUser(user)
                //DesensitizedUtil.email邮箱脱敏
                .setEmail(DesensitizedUtil.email(email));
    }

    /**
     * 更换邮箱
     */
    @Override
    public void updateEmail(String email, String verificationCode) {
        // 校验邮箱和验证码
        if (!email.matches(EMAIL_REGEX)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        verifyAndDeleteCode(email, verificationCode);
        // 校验邮箱是否能换绑
        User user = this.lambdaQuery()
                .eq(User::getEmail, email)
                .one();
        if (user != null) {
            if (user.getId().equals(UserContext.getId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已在使用");
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱已被绑定,请更换邮箱");
            }
        }
        // 更换邮箱
        this.lambdaUpdate()
                .eq(User::getId, UserContext.getId())
                .set(User::getEmail, email)
                .update();
    }

    /**
     * 设置密码
     */
    @Override
    public void updatePassword(String password, String verificationCode) {
        // 校验密码长度
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于6位");
        }
        // 加密密码
        String encryptedPassword = md5Util.encrypt(password);
        // 校验验证码
        verifyAndDeleteCode(UserContext.getEmail(), verificationCode);
        // 更换密码
        this.lambdaUpdate()
                .eq(User::getId, UserContext.getId())
                .set(User::getPassword, encryptedPassword)
                .update();
    }

    /**
     * 更新基本信息
     */
    @Override
    public void updateBasicInfo(User user) {
        // 提取基本信息
        String username = user.getUsername();
        Integer gender = user.getGender();
        String profile = user.getProfile();
        if (username == null && gender == null && profile == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息为空");
        // 校验基本信息
        if (username != null && !username.matches(USERNAME_REGEX))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称必须由 2~10 位的中英文或数字组成");
        if (gender != null && (gender < 0 || gender > 2))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "性别参数错误");
        if (profile != null && profile.length() > 50)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "个人简介最长为 50 个字符");
        // 更新基本信息
        this.lambdaUpdate()
                .eq(User::getId, UserContext.getId())
                .set(username != null, User::getUsername, username)
                .set(gender != null, User::getGender, gender)
                .set(profile != null, User::getProfile, profile)
                .update();
    }

    /**
     * 根据 ID 查询用户信息
     */
    @Override
    public User queryByUserId(Long userId) {
        User user = this.getById(userId);
        User maskedUser = getMaskedUser(user);
        redissonClient.getBucket(USER_INFO_KEY + userId)
                .set(maskedUser, USER_INFO_TTL);
        return maskedUser;
    }

    /**
     * 根据 ID 批量查询用户信息
     */
    @Override
    public List<User> queryByIdsWithCache(List<Long> userIds) {
        Map<Long, User> userMap = new HashMap<>();
        //封装id
        String[] userIdListWithPrefix = userIds.stream()
                .map(userId -> USER_INFO_KEY + userId)
                .toArray(String[]::new);
        //获取缓存
        Map<String, User> cachedMap = redissonClient.getBuckets()
                .get(userIdListWithPrefix);
        for (Map.Entry<String, User> entry : cachedMap.entrySet()) {
            String userIdWithPrefix = entry.getKey();
            User user = entry.getValue();
            Long userId = Long.valueOf(userIdWithPrefix.substring(USER_INFO_KEY.length()));
            userMap.put(userId, user);
        }
        //查询未缓存的用户并缓存
        List<Long> uncachedIdsList = userIds.stream()
                .filter(userId -> !cachedMap.containsKey(USER_INFO_KEY + userId))
                .toList();
        if (!uncachedIdsList.isEmpty()) {
            this.lambdaQuery()
                    .in(User::getId, uncachedIdsList)
                    .list()
                    .forEach(user -> {
                        Long userId = user.getId();
                        user = getMaskedUser(user);
                        redissonClient.getBucket(USER_INFO_KEY + userId)
                                .set(user, USER_INFO_TTL);
                        userMap.put(userId, user);
                    });
        }

        return userIds.stream().map(userMap::get).toList();
    }

    /**
     * 根据标签分页查询用户
     */
    @Override
    public Page<User> queryByTagsWithPagination(Set<String> tags, Integer currentPage) {
        if (tags.size() > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多只能查询 10 个标签");
        }
        // 格式化当前日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String dateStr = LocalDate.now().format(formatter);

        // 更新标签搜索次数
        RScoredSortedSet<String> searchHotSortedSet = redissonClient
                .getScoredSortedSet(SEARCH_HOT_KEY + dateStr);
        searchHotSortedSet.expire(SEARCH_HOT_TTL);
        tags.forEach(tag -> searchHotSortedSet.addScore(tag, 1));
        // 分页相关参数
        long total;
        List<Long> idRecords;
        int start = (currentPage - 1) * DEFAULT_PAGE_SIZE;
        int end = currentPage * DEFAULT_PAGE_SIZE - 1;

        // 检查搜索结果是否已缓存
        String searchTagsKey = SEARCH_TAGS_KEY + String.join(",", tags);
        RList<Long> searchTagsList = redissonClient.getList(searchTagsKey);
        int cacheSize = searchTagsList.size();
        if (cacheSize > 0) {
            total = cacheSize;
            idRecords = searchTagsList.range(start, end);
        } else {
            // 统计用户的标签匹配次数
            Map<Long, Integer> matchCount = new HashMap<>();
            tags.forEach(tag -> {
                RSet<Long> userIdSet = redissonClient.getSet(TAGS_KEY + tag);
                userIdSet.forEach(userId -> matchCount.merge(userId, 1, Integer::sum));
            });
            // 按标签匹配次数降序排序
            List<Long> userIds = matchCount.keySet()
                    .stream()
                    .sorted((a, b) -> matchCount.get(b) - matchCount.get(a))
                    .toList();
            // 缓存搜索结果
            searchTagsList.addAll(userIds);
            searchTagsList.expire(SEARCH_TAGS_TTL);
            // 获得分页数据
            total = userIds.size();
            idRecords = userIds.subList(start, Math.min(end + 1, userIds.size()));
        }
        // 查询分页后的用户信息
        List<User> userRecords = queryByIdsWithCache(idRecords);
        return new Page<User>(currentPage, DEFAULT_PAGE_SIZE, total).setRecords(userRecords);
    }

    /**
     * 查询近期热门搜索标签
     */
    @Override
    public List queryHotTags() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        // 统计近三天的热门标签
        Map<String, Double> tagToHotMap = new TreeMap<>();
        for (int i = 0; i < 3; i++) {
            int daysBefore = i;
            String dateBeforeStr = now.minusDays(daysBefore).format(formatter);
            RScoredSortedSet<String> searchHotSortedSet = redissonClient
                    .getScoredSortedSet(SEARCH_HOT_KEY + dateBeforeStr);
            searchHotSortedSet.entryRangeReversed(0, 9)
                    .forEach(entry -> {
                        String tag = entry.getValue();
                        Double hot = entry.getScore() * (3 - daysBefore);
                        tagToHotMap.merge(tag, hot, Double::sum);
                    });
        }
        // 取最热门的 10 个标签
        return tagToHotMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10)
                .toList();
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
                .setEmail(email)
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
                .setEmail(user.getEmail())
                .setStatus(user.getStatus());
        RBucket<User> userBucket = redissonClient.getBucket(USER_TOKEN_KEY + token);
        userBucket.set(tokenUser, USER_TOKEN_TTL);
        return token;
    }
}






