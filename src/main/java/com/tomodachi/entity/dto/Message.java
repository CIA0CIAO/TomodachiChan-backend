package com.tomodachi.entity.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Message {
    private String content;
    private Boolean isUnread;
    private Long senderId;
    private LocalDateTime sendTime;
    private Long scrollId;
}
