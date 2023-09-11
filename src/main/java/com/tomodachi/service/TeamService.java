package com.tomodachi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tomodachi.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tomodachi.entity.User;
import com.tomodachi.entity.dto.TeamInfo;
import com.tomodachi.entity.dto.TeamInvitation;
import com.tomodachi.entity.dto.TeamQuery;

import java.util.List;

/**
 * @author CIA0CIA0
 * @description 针对表【team(队伍表)】的数据库操作Service
 * @createDate 2023-09-09 14:29:57
 */
public interface TeamService extends IService<Team> {

    void createTeam(Team team);

    void joinPublicTeam(Long id, String password);

    TeamInvitation sendInvitation(TeamInvitation teamInvitation);

    void acceptInvitation(String invitationCode);

    String quitOrDisbandTeam(Long teamId);

    void updateTeamInfo(Team team);

    TeamInfo queryByTeamId(Long teamId);

    List<TeamInfo> listMyTeamInfo();

    List<TeamInfo> listTeamInfoByUserId(Long userId);

    Page<TeamInfo>  queryByConditionWithPagination(TeamQuery teamQuery);
    List<User> listTeamMember(Long teamId);

}
