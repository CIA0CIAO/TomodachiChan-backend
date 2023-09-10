package com.tomodachi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomodachi.entity.UserTeam;
import com.tomodachi.service.UserTeamService;
import com.tomodachi.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author CIA0CIA0
* @description 针对表【user_team(用户队伍关联表)】的数据库操作Service实现
* @createDate 2023-09-09 22:10:09
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




