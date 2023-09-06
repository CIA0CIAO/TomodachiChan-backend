package com.tomodachi.entity.dto;

import lombok.Data;

@Data
public class UserLoginForm {
//    private String phoneNumber;
    private String verificationCode;
    private String password;
    private String email;
}