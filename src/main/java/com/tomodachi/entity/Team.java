package com.tomodachi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 队伍表
 * @TableName team
 */
@TableName(value ="team")
@Data
@Accessors(chain = true)
public class Team implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 队长 ID
     */
    private Long leaderId;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 队伍类型 (0 - 公开, 1 - 私密)
     */
    private Integer type;

    /**
     * 加入密码
     */
    private String password;

    /**
     * 人数限制
     */
    private Integer memberLimit;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}