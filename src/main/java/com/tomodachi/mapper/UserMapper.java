package com.tomodachi.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomodachi.entity.User;
import org.apache.ibatis.annotations.Mapper;
/**
 * @author CIA0CIA0
 * @description 针对表【user(用户表)】的数据库操作Mapper
 * @createDate 2023-09-06 21:02:24
 * @Entity generator.domain.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {}