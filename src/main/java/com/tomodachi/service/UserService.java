package com.tomodachi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tomodachi.entity.User;
import com.tomodachi.entity.dto.TeamInfo;
import com.tomodachi.entity.dto.UserLogin;

import java.util.List;
import java.util.Set;

/**
 * @author CIA0CIA0
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2023-09-06 21:02:24
 */
public interface UserService extends IService<User> {

    void sendVerificationCode(String email);

    UserLogin loginByVerificationCode(String email, String verificationCode);

    UserLogin loginByPassword(String email, String password);

    User getAccountInfo();

    void updateEmail(String email, String verificationCode);

    void updatePassword(String password, String verificationCode);

    void updateBasicInfo(User user);

    void updateTags(List<String> tags);

    User queryByUserId(Long userId);

    List<User> queryByIdsWithCache(List<Long> userIds);

    Page<User> queryByTagsWithPagination(Set<String> tags, Integer currentPage);

    List<String> queryHotTags();

    Page<User> recommendUsers(Long userId, Integer currentPage);

    Page<User> queryByUsernameWithPagination(String username, Integer currentPage);
}
