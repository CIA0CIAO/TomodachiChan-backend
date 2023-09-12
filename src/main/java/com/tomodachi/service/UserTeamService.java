package com.tomodachi.service;

import com.tomodachi.entity.UserTeam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Set;

/**
* @author CIA0CIA0
* @description 针对表【user_team(用户队伍关联表)】的数据库操作Service
* @createDate 2023-09-09 22:10:09
*/
public interface UserTeamService extends IService<UserTeam> {
    void sendMessages(String content, Set<Long> userIds);
}
