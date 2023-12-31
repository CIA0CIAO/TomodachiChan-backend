package com.tomodachi.mapper;

import com.tomodachi.entity.Team;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomodachi.entity.dto.TeamInfo;

import java.util.List;

/**
* @author CIA0CIA0
* @description 针对表【team(队伍表)】的数据库操作Mapper
* @createDate 2023-09-09 14:29:57
* @Entity com.tomodachi.entity.Team
*/
public interface TeamMapper extends BaseMapper<Team> {
    TeamInfo getTeamInfoById(Long teamId);

    List<TeamInfo> listAllTeamInfoByUserId(Long userId);

    List<TeamInfo> listTeamInfoByUserId(Long userId);

    List<TeamInfo> listTeamInfoByCondition(long offset, long limit, String searchText, boolean onlyNoPassword);

    long countTeamByCondition(String searchText, boolean onlyNoPassword);
}




