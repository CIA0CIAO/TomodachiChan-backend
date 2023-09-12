package com.tomodachi.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomodachi.common.UserContext;
import com.tomodachi.entity.UserTeam;
import com.tomodachi.entity.dto.Message;
import com.tomodachi.service.UserTeamService;
import com.tomodachi.mapper.UserTeamMapper;
import jakarta.annotation.Resource;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

import static com.tomodachi.constant.RedisConstant.USER_MESSAGE_KEY;

/**
 * @author CIA0CIA0
 * @description 针对表【user_team(用户队伍关联表)】的数据库操作Service实现
 * @createDate 2023-09-09 22:10:09
 */
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam> implements UserTeamService {
    @Resource
    RedissonClient redissonClient;

    /**
     * 向列表中的用户发送消息
     */
    @Override
    public void sendMessages(String content, Set<Long> userIds) {
        long scrollId = IdUtil.getSnowflakeNextId();
        Message message = new Message().setContent(content)
                .setIsUnread(true)
                .setSenderId(UserContext.getId())
                .setSendTime(LocalDateTime.now())
                .setScrollId(scrollId);

        for (Long userId : userIds) {
            RScoredSortedSet<Message> messageSortedSet = redissonClient.getScoredSortedSet(
                    USER_MESSAGE_KEY + userId);
            messageSortedSet.add(scrollId, message);
        }
    }
}



