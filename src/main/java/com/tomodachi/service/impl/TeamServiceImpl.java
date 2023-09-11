package com.tomodachi.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomodachi.common.UserContext;
import com.tomodachi.common.exception.BusinessException;
import com.tomodachi.controller.response.ErrorCode;
import com.tomodachi.entity.Team;
import com.tomodachi.entity.User;
import com.tomodachi.entity.UserTeam;
import com.tomodachi.entity.dto.TeamInfo;
import com.tomodachi.entity.dto.TeamInvitation;
import com.tomodachi.entity.dto.TeamQuery;
import com.tomodachi.mapper.TeamMapper;
import com.tomodachi.service.TeamService;
import com.tomodachi.service.UserService;
import com.tomodachi.service.UserTeamService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.tomodachi.constant.RedisConstant.*;
import static com.tomodachi.constant.SystemConstant.*;

/**
 * @author CIA0CIA0
 * @description 针对表【team(队伍表)】的数据库操作Service实现
 * @createDate 2023-09-09 14:29:57
 */
@Service
@RequiredArgsConstructor
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
    @Resource
    RedissonClient redissonClient;
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private TeamMapper teamMapper;
    @Resource
    private UserService userService;

    /**
     * 创建队伍
     */
    @Override
    public void createTeam(Team team) {
        // 校验队伍信息
        validateTeamInfo(team);

        // 校验用户已拥有的队伍数量
        Long userId = UserContext.getId();
        if (this.lambdaQuery()
                .eq(Team::getLeaderId, userId)
                .count() >= 10)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您拥有的队伍数量已达上限");
        // 创建并加入队伍
        team.setLeaderId(userId);
        this.save(team);
        doJoinTeam(userId, team.getId());
    }

    /**
     * 加入公开队伍
     */
    @Override
    public void joinPublicTeam(Long teamId, String password) {
        Team team = isTeamExist(teamId, true);
        // 校验队伍是否能加入
        validateTeamJoin(team);
        // 校验密码是否正确
        String teamPassword = team.getPassword();
        if (Strings.isNotBlank(teamPassword) && !teamPassword.equals(password))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        doJoinTeam(UserContext.getId(), teamId);
    }

    /**
     * 发送入队邀请
     */
    @Override
    public TeamInvitation sendInvitation(TeamInvitation teamInvitation) {
        // 校验邀请信息
        Long teamId = teamInvitation.getTeamId();
        Long invitee = teamInvitation.getInvitee();
        if (teamId == null || invitee == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邀请信息不完整");
        // 校验用户是否为队长
        Team team = isTeamExist(teamId, false);
        Long inviter = UserContext.getId();
        if (!inviter.equals(team.getLeaderId()))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅队长可使用邀请功能");
        // 校验对方是否已在队伍中
        if (userTeamService.lambdaQuery()
                .eq(UserTeam::getUserId, invitee)
                .eq(UserTeam::getTeamId, teamId)
                .one() != null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "对方已在该队伍中");
        // 校验队伍是否能加入
        validateTeamJoin(team);
        // 生成邀请码并保存邀请信息
        String invitationCode = RandomUtil.randomNumbers(6);
        RBucket<TeamInvitation> teamInvitationBucket = redissonClient.getBucket(
                TEAM_INVITATION_KEY + invitationCode);
        //如果存在则重新生成
        while (teamInvitationBucket.isExists()) {
            invitationCode = RandomUtil.randomNumbers(6);
            teamInvitationBucket = redissonClient.getBucket(TEAM_INVITATION_KEY + invitationCode);
        }
        teamInvitationBucket.set(teamInvitation, TEAM_INVITATION_TTL);

        return new TeamInvitation().setInvitationCode(invitationCode);
    }

    /**
     * 接受入队邀请
     */
    @Override
    public void acceptInvitation(String invitationCode) {
        // 校验邀请码
        RBucket<TeamInvitation> teamInvitationBucket = redissonClient.getBucket(TEAM_INVITATION_KEY + invitationCode);
        if (!teamInvitationBucket.isExists())
            throw new BusinessException(ErrorCode.NULL_ERROR, "邀请码不存在或已过期");
        TeamInvitation teamInvitation = teamInvitationBucket.get();
        Long teamId = teamInvitation.getTeamId();
        Long invitee = teamInvitation.getInvitee();
        // 校验用户是否为受邀者
        if (!invitee.equals(UserContext.getId()))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您不是该邀请码的受邀者");
        // 校验队伍是否能加入
        Team team = isTeamExist(teamId, false);
        validateTeamJoin(team);
        // 加入队伍
        doJoinTeam(UserContext.getId(), teamId);
        // 删除邀请信息
        teamInvitationBucket.delete();
    }

    /**
     * 退出或解散队伍
     */
    @Override
    @Transactional
    public String quitOrDisbandTeam(Long teamId) {
        Team team = isTeamExist(teamId, false);
        // 校验用户是否在队伍中
        Long userId = UserContext.getId();
        List<UserTeam> userTeam = userTeamService.lambdaQuery()
                .eq(UserTeam::getUserId, userId)
                .list();
        if (userTeam.stream()
                .map(UserTeam::getUserId)
                .noneMatch(userId::equals))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您不在该队伍中");

        if (team.getLeaderId().equals(userId)) {
            // 解散队伍
            this.removeById(teamId);
            userTeamService.lambdaUpdate()
                    .eq(UserTeam::getTeamId, teamId)
                    .remove();
            return "已解散队伍";
        } else {
            // 退出队伍
            userTeamService.lambdaUpdate()
                    .eq(UserTeam::getUserId, userId)
                    .eq(UserTeam::getTeamId, teamId)
                    .remove();
            return "已退出队伍";
        }
    }

    /**
     * 更新队伍信息
     */
    @Override
    public void updateTeamInfo(Team team) {
        Team oldTeam = isTeamExist(team.getId(), false);
        // 校验用户是否为队长
        Long userId = UserContext.getId();
        if (!userId.equals(oldTeam.getLeaderId()))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅队长可修改队伍信息");
        // 校验队伍信息
        validateTeamInfo(team);
        // 校验当前队伍人数是否超过人数限制
        int teamMembers = listTeamMember(team.getId()).size();
        if (teamMembers > team.getMemberLimit())
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前人数已超过该人数限制");
        this.lambdaUpdate()
                .eq(Team::getId, team.getId())
                .set(Team::getName, team.getName())
                .set(Team::getDescription, team.getDescription())
                .set(Team::getType, team.getType())
                .set(team.getType() == 0 && Strings.isNotBlank(team.getPassword())
                        , Team::getPassword, team.getPassword())
                .set(team.getType() != 0, Team::getPassword, null)
                .set(Team::getMemberLimit, team.getMemberLimit())
                .set(Team::getExpireTime, team.getExpireTime())
                .update();
    }

    /**
     * 根据 ID 查询队伍信息
     */
    @Override
    public TeamInfo queryByTeamId(Long teamId) {
        TeamInfo teamInfo = teamMapper.getTeamInfoById(teamId);
        if (teamInfo == null)
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");

        // 私密队伍校验身份
        if (teamInfo.getType() == 1 &&
                listTeamMember(teamId)
                        .stream()
                        .noneMatch(user -> user.getId().equals(UserContext.getId())))
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        return teamInfo;
    }

    /**
     * 查询我加入的队伍列表
     */
    @Override
    public List<TeamInfo> listMyTeamInfo() {
        return teamMapper.listAllTeamInfoByUserId(UserContext.getId());
    }

    /**
     * 查询用户加入的队伍列表
     */
    @Override
    public List<TeamInfo> listTeamInfoByUserId(Long userId) {
        return teamMapper.listTeamInfoByUserId(userId);
    }

    /**
     * 按条件分页查询队伍信息
     */
    @Override
    public Page<TeamInfo> queryByConditionWithPagination(TeamQuery teamQuery) {
        // 处理查询条件
        int currentPage = teamQuery.getCurrentPage();
        int offset = (currentPage - 1) * DEFAULT_PAGE_SIZE;
        int limit = DEFAULT_PAGE_SIZE;
        String searchText = teamQuery.getSearchText();
        boolean onlyNoPassword = Boolean.TRUE.equals(teamQuery.getOnlyNoPassword());

        // 查询队伍信息
        List<TeamInfo> teamInfoList = teamMapper.listTeamInfoByCondition(offset, limit, searchText, onlyNoPassword);
        long total = teamMapper.countTeamByCondition(searchText, onlyNoPassword);

        Page<TeamInfo> teamInfoPage = new Page<>(currentPage, DEFAULT_PAGE_SIZE, total);
        return teamInfoPage.setRecords(teamInfoList);
    }

    /**
     * 查询队伍成员列表
     */
    @Override
    public List<User> listTeamMember(Long teamId) {
        List<Long> memberIds = userTeamService.lambdaQuery()
                .select(UserTeam::getUserId)
                .eq(UserTeam::getTeamId, teamId)
                .orderByAsc(UserTeam::getCreateTime)
                .list()
                .stream()
                .map(UserTeam::getUserId)
                .toList();
        return userService.queryByIdsWithCache(memberIds);
    }

    /**
     * 分页推荐随机队伍
     */
    @Override
    public Page<TeamInfo> recommendTeams(Long userId, Integer currentPage) {
        if (userId == null)
            userId = 0L;
        // 从缓存中获取推荐队伍列表
        RList<TeamInfo> teamRecommendList = redissonClient.getList(TEAM_RECOMMEND_KEY + userId);
        // 游客缓存的队伍推荐列表过期或用户刷新列表时重新生成缓存
        if (teamRecommendList.isEmpty() || (userId != 0 && currentPage == 1))
            teamRecommendList = generateTeamRecommendCache(userId);
        int offset = (currentPage - 1) * DEFAULT_PAGE_SIZE;
        int total = teamRecommendList.size();
        List<TeamInfo> teamRecords = new ArrayList<>(teamRecommendList.subList(offset, Math.min(offset + DEFAULT_PAGE_SIZE, total)));
        return new Page<TeamInfo>(currentPage, DEFAULT_PAGE_SIZE, total).setRecords(teamRecords);
    }


    /**
     * 校验队伍信息是否合法
     */
    private void validateTeamInfo(Team team) {
        // 校验队伍名称
        String name = team.getName();
        if (Strings.isBlank(name) || !name.matches(TEAM_NAME_REGEX))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队名必须由 2~20 位的中英文或数字组成");

        // 校验队伍描述
        String description = team.getDescription();
        if (Strings.isNotBlank(description) && description.length() > 50)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述限制 50 字以内");

        // 校验队伍类型
        int type = Optional.ofNullable(team.getType()).orElse(0);
        if (type != 0 && type != 1)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍类型不符合要求");

        // 校验入队密码
        String password = team.getPassword();
        if (type != 0 && Strings.isNotBlank(password))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅公开队伍可设置入队密码");

        // 校验人数限制
        int memberLimit = Optional.ofNullable(team.getMemberLimit()).orElse(10);
        if (memberLimit < 2 || memberLimit > 10)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数限制在 2~10 人之间");

        // 校验过期时间
        LocalDateTime expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍过期时间必须晚于当前");
    }

    /**
     * 执行加入队伍操作
     */
    private void doJoinTeam(Long userId, Long teamId) {
        // 获取分布式锁实例
        RLock lock = redissonClient.getLock(LOCK_TEAM_JOIN_KEY + userId + ":" + teamId);
        try {
            if (lock.tryLock()) {
                // 查询用户已加入的队伍
                List<UserTeam> joinedTeam = userTeamService.lambdaQuery()
                        .eq(UserTeam::getUserId, userId)
                        .list();
                // 校验用户是否已在队伍中
                if (joinedTeam.stream()
                        .map(UserTeam::getTeamId)
                        .anyMatch(id -> id.equals(teamId)))
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "您已在该队伍中");
                // 校验用户已加入的队伍数量
                if (joinedTeam.size() > JOINED_TEAM_LIMIT)
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "您已加入的队伍数量已达上限");
                // 添加用户队伍关联
                UserTeam userTeam = new UserTeam();
                userTeam.setUserId(userId);
                userTeam.setTeamId(teamId);
                userTeamService.save(userTeam);
            }
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    /**
     * 校验队伍是否存在
     */
    private Team isTeamExist(Long teamId, boolean isPublic) {
        Team team = this.getById(teamId);
        if (team == null)
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        if (isPublic && team.getType() != 0)
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        return team;
    }

    /**
     * 校验队伍是否可加入
     */
    private void validateTeamJoin(Team team) {
        // 校验队伍是否过期
        LocalDateTime expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        // 校验队伍是否已满
        if (userTeamService.lambdaQuery()
                .eq(UserTeam::getTeamId, team.getId())
                .count() >= team.getMemberLimit())
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数已满");
    }

    /**
     * 生成队伍推荐列表缓存
     */
    private RList<TeamInfo> generateTeamRecommendCache(Long userId) {
        // 在随机位置查询队伍信息并打乱
        int offset = RandomUtil.randomInt(0, 100);
        List<TeamInfo> teamInfoList = teamMapper.listTeamInfoByCondition(
                offset, 100, null, true);
        Collections.shuffle(teamInfoList);

        // 缓存打乱后的队伍推荐列表
        RList<TeamInfo> recommendTeamList = redissonClient.getList(TEAM_RECOMMEND_KEY + userId);
        recommendTeamList.clear();
        recommendTeamList.addAll(teamInfoList);
        recommendTeamList.expire(TEAM_RECOMMEND_TTL);
        return recommendTeamList;
    }

}




