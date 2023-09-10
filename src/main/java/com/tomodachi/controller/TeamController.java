package com.tomodachi.controller;

import com.tomodachi.common.exception.BusinessException;
import com.tomodachi.common.role.RoleCheck;
import com.tomodachi.controller.response.BaseResponse;
import com.tomodachi.controller.response.ErrorCode;
import com.tomodachi.entity.Team;
import com.tomodachi.entity.dto.TeamInvitation;
import com.tomodachi.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
@Tag(name = "队伍接口")
public class TeamController {
    @Resource
    private TeamService teamService;

    @RoleCheck
    @Operation(summary = "用户创建队伍")
    @PostMapping
    public BaseResponse<String> createTeam(@RequestBody Team team) {
        if (team == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入队伍信息");
        teamService.createTeam(team);
        return BaseResponse.success("队伍创建成功");
    }

    @RoleCheck
    @Operation(summary = "用户加入公开队伍")
    @PostMapping("/join")
    public BaseResponse<String> joinPublicTeam(@RequestBody Team team) {
        if (team == null || team.getId() == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要加入的队伍");
        teamService.joinPublicTeam(team.getId(), team.getPassword());
        return BaseResponse.success("加入队伍成功");
    }

    @RoleCheck
    @Operation(summary = "用户发送入队邀请")
    @PostMapping("/invite")
    public BaseResponse<TeamInvitation> sendInvitation(@RequestBody TeamInvitation teamInvitation) {
        if (teamInvitation == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入邀请信息");
        return BaseResponse.success(teamService.sendInvitation(teamInvitation));
    }

    @RoleCheck
    @Operation(summary = "用户接受入队邀请")
    @GetMapping("/join/invite")
    public BaseResponse<String> acceptInvitation(@RequestParam String invitationCode) {
        if (Strings.isBlank(invitationCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入邀请码");
        teamService.acceptInvitation(invitationCode);
        return BaseResponse.success("接受邀请成功");
    }

    @RoleCheck
    @Operation(summary = "用户退出或解散队伍")
    @DeleteMapping("/{teamId}")
    public BaseResponse<String> quitOrDisbandTeam(@PathVariable Long teamId) {
        if (teamId == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要退出或解散的队伍");
        return BaseResponse.success(teamService.quitOrDisbandTeam(teamId));
    }

    @RoleCheck
    @Operation(summary = "用户修改队伍信息")
    @PutMapping
    public BaseResponse<String> updateTeamInfo(@RequestBody Team team) {
        if (team == null || team.getId() == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择要修改的队伍");
        teamService.updateTeamInfo(team);
        return BaseResponse.success("队伍信息修改成功");
    }
}
