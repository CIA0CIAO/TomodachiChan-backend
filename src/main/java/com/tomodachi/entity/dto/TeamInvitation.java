package com.tomodachi.entity.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeamInvitation {
    /**
     * 队伍 ID
     */
    private Long teamId;
    /**
     * 邀请者 ID
     */
    private Long inviter;
    /**
     * 受邀者 ID
     */
    private Long invitee;
    /**
     * 邀请码
     */
    private String invitationCode;
}