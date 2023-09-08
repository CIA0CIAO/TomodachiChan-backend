package com.tomodachi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomodachi.entity.User;
import com.tomodachi.entity.dto.UserLogin;

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
}
