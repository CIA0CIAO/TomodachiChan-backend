package com.tomodachi.entity.dto;

import com.tomodachi.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLogin {
    private User user;
    private String token;
}