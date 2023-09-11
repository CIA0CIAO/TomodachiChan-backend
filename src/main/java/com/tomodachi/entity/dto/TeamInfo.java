package com.tomodachi.entity.dto;

import com.tomodachi.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamInfo {
    private Long id;
    private String name;
    private String description;
    private User leader;
    private Integer type;
    private Boolean hasPassword;
    private Integer memberCount;
    private Integer memberLimit;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}