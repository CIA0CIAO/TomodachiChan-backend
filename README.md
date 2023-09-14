# 公诸同好 (后端) - TomodachiChan-backend

> 于离别之朝束起约定之花

## 📝 项目介绍

公诸同好是一个以标签和队伍为核心的社交平台, 旨在帮助用户快速找到志同道合的伙伴。

此为该项目的后端部分, 前端部分请移步 [TomodachiChan-frontend](https://github.com/CIA0CIAO/TomodachiChan-frontend)

## 🔧 项目技术栈

- **Redisson**
- Spring
- SpringMVC
- SpringBoot
- SpringBoot mail
- MySQL
- MyBatis-Plus
- Redis
- Knife4j
- Hutool
- Aliyun OSS

## 🔍 主要功能

### 👤 用户相关

- 📱  邮箱验证码快速注册 / 登录
- 🔑 密码登录
- ✏️ 编辑个人信息
- 📸 上传 / 修改头像
- 🔗 换绑邮箱 / 更改密码
- 🔥 热门标签推荐
- 🔍 搜索与标签相关联的伙伴
- ℹ️ 伙伴信息展示
- ✉️ 滚动查询用户消息列表
- 🔴 未读消息标记 / 数量统计
- 🎁 用户推荐

### 👥 队伍相关

- ⚙️ 创建 / 加入 / 退出 / 解散队伍
- 🔒 设置队伍可见性为公开 / 私密
- ⬆️ 设置队伍人数上限
- 📆 设置队伍过期时间
- 🎫 邀请用户入队 / 接受入队邀请
- ✏️ 更改队伍信息
- ℹ️ 队伍信息展示
- 🎁 队伍推荐

## ✨ 功能亮点

1. 通过 Redisson 访问 Redis
    - 全部使用 `RedissonClient` 访问 Redis
    - 与 JDK 中常用的数据结构操作相同, 相比 `StringRedisTemplate` 更加方便
    - 提供了分布式锁, 方便实现分布式的互斥操作
2. 接口权限校验
    - 使用自定义注解 `@RoleCheck` 对接口所需的权限进行标注
    - 配合 `TokenInterceptor` 拦截器对用户进行权限校验
    - 通过配置 Knife4j 实现对不同权限接口的分组展示
3. 预缓存任务
    - 使用 `@Scheduled` 注解实现每日定时任务
    - 使用 Redisson 分布式锁实现预缓存任务的互斥
4. 用户消息
    - 使用 Redis 存储用户消息
    - 通过传入 `scrollId` 实现消息的滚动查询
    - 通过消息的 `isUnread` 字段实现对未读的标记
5. 队伍查询
    - 通过编写 Mapper XML 实现复杂的队伍查询
    - 通过 `association` 标签实现对复杂查询结果的映射
    - 通过 `sql` 标签抽取公共 SQL 语句

## 🚀 项目运行

1. 执行 `src/main/resources/momo_companion.sql` 中的 SQL 语句创建数据库
2. 在 `src/main/resources` 目录下创建 `application-dev.yml` 文件, 并填写相关配置, 样例如下

```yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql:///companion
    username: root
    password: root
  data:
    redis:
      host: localhost
      password: root

momo-companion:
  md5-salt: 'MomoCompanion' # 密码 MD5 加密盐值
  aliyun:
    oss: # 阿里云 OSS 服务配置
      access-key-id: 'XXXXXXXXXX'
      access-key-secret: 'XXXXXXXXXX'
      endpoint: 'https://oss-cn-beijing.aliyuncs.com'
      bucket-name: 'your-bucket-name'
      url-prefix: 'https://your.url.prefix/'
```
