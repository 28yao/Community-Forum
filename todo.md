# 社区论坛系统 — 任务清单 (todo.md)

> **关联文档**: PRD v1.1 / plan.md (2026-05-23) / CLAUDE.md
> **拆分原则**: 以"功能模块"为一级结构，每模块按"目标 → 用户视角 → 前后端任务 → 测试 → 状态"12 个维度细分
> **任务粒度**: 原子化，每个任务只改/建一个主要文件，标注涉及文件/前置依赖/验收方式
> **推进规则**: 单任务推进，一次仅一个"进行中"，完成验收后方可继续下一任务

---

## 📌 一期归档说明（2026-05-23）

> **一期验收状态**: M0~M7 全部验收完成（含 71+10 项后端测试通过 + 前端构建成功 + 端到端验收）
>
> **本文档定位**: 一期任务执行档案，**只读归档**。后续修复 bug 或追溯一期工作时回写本文档，但不在本文档继续拆分新任务。
>
> **二期入口**: `.context/phase2/`
> - `.context/phase2/prd.md` — 二期需求文档（贴吧风 UI + 板块申请审核 + 板块关注 + 搜索增强 + 发帖弹窗）
> - `.context/phase2/plan.md` — 二期技术方案
> - `.context/phase2/todo.md` — 二期任务拆分（P2-M1 ~ P2-M8）
>
> **二期 commit 约定**: 所有二期提交以 `[phase2]` 为前缀，便于 `git log --grep="\[phase2\]"` 追溯。
>
> **二期对一期的覆盖关系**:
> - M2-T9 AppSidebar 版块导航 → 被 P2-M4 BoardSidebar 三段重构覆盖
> - M2-T11 Home.vue 首页布局 → 被 P2-M4 Home.vue 重写覆盖
> - M3-T20 PostCard.vue → 被 P2-M4 贴吧风卡片重构覆盖
> - M3-T28 Home.vue 显示最新帖 → 被 P2-M5 feed 接口 + P2-M4 PostFeed 覆盖
> - M5-T7 Search.vue → 被 P2-M7 Tab 化重构覆盖
> - 其余一期任务在二期保持不变

---

## 状态规则速查

| 任务状态 | 说明 |
|----------|------|
| 未开始 | 尚未启动 |
| 进行中 | 当前正在执行（同时仅 1 个） |
| 已完成 | 已实现并自测通过 |
| 阻塞 | 因依赖/问题暂停 |
| 待确认 | 信息缺失，需用户/产品确认 |

| 验收状态 | 说明 |
|----------|------|
| 待验收 | 任务完成，等待验收 |
| 自动验收通过 | 单测/接口测试通过 |
| 人工验收通过 | 人工体验通过 |
| 验收驳回 | 不通过，需修复 |
| 重新提交 | 修复后再次提交 |
| 无需验收 | 内部脚手架/无独立验收点 |

---

## 模块总览

| # | 模块 | 关联 PRD | 关联 Phase | 当前状态 |
|---|------|---------|-----------|---------|
| M0 | 基础设施模块（项目初始化） | — | Phase 1 | 已完成 |
| M1 | 用户系统模块 | §5.1 | Phase 2 | 已完成 |
| M2 | 版块系统模块 | §5.2 | Phase 3 | 已完成 |
| M3 | 帖子系统模块 | §5.3 | Phase 4 | 已完成 |
| M4 | 互动系统模块（点赞 / 评论 / 收藏） | §5.4 | Phase 5 | 已完成 |
| M5 | 搜索模块 | §5.5 | Phase 6 | 已完成 |
| M6 | 后台管理模块 | §6 | Phase 7 | 已完成 |
| M7 | 系统收尾模块（404 / 限流 / 错误处理） | §10 | Phase 8 | 已完成 |

---

## M0 基础设施模块（项目初始化）

> 对应 plan.md Phase 1，为后续所有模块提供基础脚手架。

### 1. 模块目标
- 搭建后端 Spring Boot 项目骨架和前端 Vue 3 项目骨架
- 建立数据库表结构
- 配置通用组件（统一响应、异常处理、分页、Token 等）

### 2. 用户可见内容
- 当前阶段用户**看不到具体业务功能**
- 该模块主要用于支撑后续功能开发
- 可通过启动项目、访问默认首页、或访问任意基础接口（如 `GET /api/boards` 返回空列表）验证

### 3. 用户操作流程
- 无业务操作。开发者可执行：
  - 启动 `mvn spring-boot:run` 看到 "Started ForumApplication"
  - 启动 `npm run dev` 看到 Vue 默认首页可访问

### 4. 前端页面任务
- App.vue + main.js 默认页面渲染
- 路由 / Pinia / Axios 全局初始化

### 5. Controller 任务
- 暂无业务 Controller，仅需保证 Spring Boot 启动正常

### 6. Service 任务
- 暂无业务 Service

### 7. Mapper 任务
- 暂无业务 Mapper

### 8. Repository / 数据保存任务
- 执行 schema.sql 完成 8 张表建表

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 启动前端 `npm run dev` | 控制台无报错，输出访问地址 |
| 2 | 浏览器访问 `http://localhost:5173` | 看到默认页面（可以是空白带顶栏的占位页） |

### 10. 接口测试方法

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 启动后端 `mvn spring-boot:run` | 控制台无报错堆栈 |
| 2 | curl/Postman 访问 `http://localhost:8080/actuator/health`（如启用）或任意 `/api/*` | 返回 200 或统一响应体 JSON |
| 3 | 触发一个不存在的接口 | 返回统一异常 JSON，不抛 500 |

### 11. 异常情况测试

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 故意配置错误的数据库密码启动后端 | 启动失败并给出清晰错误，不静默 |
| 2 | 访问不存在路由 `/api/no-such-api` | 返回统一 404 JSON |
| 3 | 后端抛出 BizException | GlobalExceptionHandler 捕获，返回统一错误码 |

### 12. 当前状态
- **任务状态**: 已完成
- **验收状态**: 自动验收通过
- **完成日期**: 2026-05-23
- **后端测试**: 31/31 通过
- **前端构建**: vite build 成功（85 modules，含 Element Plus 后 1.2MB / gzip 400KB）
- **手动验收路径**: `npm run dev` 起前端 → 浏览器访问 http://127.0.0.1:5173 → 点击"健康检查后端"按钮验证全链路

---

### 原子任务列表（M0）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|----|------|---------|---------|---------|------|------|
| M0-T1 | 创建后端 Maven 工程骨架 | `forum-server/pom.xml` | 无 | `mvn validate` 通过 | 已完成 | 自动验收通过 |
| M0-T2 | 创建启动类 ForumApplication | `forum-server/src/main/java/com/forum/ForumApplication.java` | M0-T1 | `mvn spring-boot:run` 启动成功 | 已完成 | 自动验收通过 |
| M0-T3 | 配置 application.yml（端口 8080、JWT 7 天、QQ SMTP、上传路径） | `forum-server/src/main/resources/application.yml` + `application-dev.yml` + `application-dev.example.yml` + `.gitignore` | M0-T2 | 启动加载配置无报错；QQ 邮箱授权码走 dev 配置；JWT secret 64 位随机串（C2/C7） | 已完成 | 自动验收通过 |
| M0-T4 | 编写数据库建表脚本 schema.sql（8 张表） | `forum-server/src/main/resources/db/schema.sql` | 无 | 执行脚本后 `SHOW TABLES` 列出 8 张表 | 已完成 | 自动验收通过 |
| M0-T4b | 编写种子脚本 seed.sql（默认管理员 + 3 个版块） | `forum-server/src/main/resources/db/seed.sql` | M0-T4 | 执行后 user 表有 1 条 role=admin 记录（admin@forum.com，bcrypt 密码 raw=123456），board 表有 3 条记录（C8）；幂等已验证 | 已完成 | 自动验收通过 |
| M0-T5 | 实现统一响应体 Result | `forum-server/.../common/Result.java` + `ErrorCode.java`（占位，T7 补全） | M0-T2 | 单测构造成功/失败响应 + JSON 序列化（7/7 通过） | 已完成 | 自动验收通过 |
| M0-T6 | 实现分页响应 PageResult | `forum-server/.../common/PageResult.java` | M0-T5 | 单测分页结构正确（5/5） | 已完成 | 自动验收通过 |
| M0-T7 | 实现错误码枚举 ErrorCode | `forum-server/.../common/ErrorCode.java` | M0-T5 | 枚举值与 plan.md §4.1 表一致；16 个码值（4/4 通过） | 已完成 | 自动验收通过 |
| M0-T8 | 实现业务异常 BizException | `forum-server/.../common/exception/BizException.java` | M0-T7 | 单测抛出/捕获正常（4/4） | 已完成 | 自动验收通过 |
| M0-T9 | 实现全局异常处理 GlobalExceptionHandler | `forum-server/.../common/exception/GlobalExceptionHandler.java` | M0-T8 | 故意抛异常接口返回统一 JSON（3/3 通过：BizException + 自定义 msg + 兜底 Throwable） | 已完成 | 自动验收通过 |
| M0-T10 | 配置 MyBatis-Plus（分页插件 + 自动填充） | `forum-server/.../config/MybatisPlusConfig.java` | M0-T2 | 启动加载无报错；分页插件 + MetaObjectHandler 注入成功（2/2） | 已完成 | 自动验收通过 |
| M0-T11 | 配置 Redis 连接 | `forum-server/.../config/RedisConfig.java` + `.redis/`（本地 Microsoft Redis 3.0.504） | M0-T3 | 启动连接 Redis 成功；set/get/TTL/delete 全链路验证（4/4） | 已完成 | 自动验收通过 |
| M0-T12 | 配置 WebMvc（跨域 + 静态资源） | `forum-server/.../config/WebMvcConfig.java` | M0-T2 | 前端可跨域调用后端；上传目录自动创建 + 静态资源映射（2/2） | 已完成 | 自动验收通过 |
| M0-T13 | 创建前端 Vite 工程骨架（Node 18 LTS） | `forum-web/package.json` + `vite.config.js` | 无 | `npm install` 成功；`package.json` 含 `"engines": { "node": ">=18" }`；vite 5.4 可用（C3） | 已完成 | 自动验收通过 |
| M0-T14 | 配置前端入口与 App.vue | `forum-web/src/main.js` + `App.vue` + `index.html` + `src/styles/global.css` | M0-T13 | `npm run dev` 启动成功；HTTP 200 + 标题与 #app 挂载点正确 | 已完成 | 自动验收通过 |
| M0-T15 | 配置 Vue Router 与基础路由 | `forum-web/src/router/index.js` | M0-T14 | `vite build` 成功；占位路由 / /login /register 与 404 兜底可达 | 已完成 | 自动验收通过 |
| M0-T16 | 配置 Pinia 与基础 store | `forum-web/src/stores/user.js` + `app.js` | M0-T14 | `vite build` 成功；User/App store 注入并可调用 getter/action | 已完成 | 自动验收通过 |
| M0-T17 | 封装 Axios 请求（拦截器/Token/错误处理） | `forum-web/src/api/request.js` | M0-T14 | 接口请求自动注入 Token，统一错误提示；BizError 封装；vite build 85 modules 通过 | 已完成 | 自动验收通过 |
| M0-T18 | 引入 Element Plus 全局组件 | `forum-web/src/main.js` + `App.vue` | M0-T14 | 页面可使用 `<el-button>` `<el-tag>` `<el-alert>` 等组件；中文 locale 已挂载；vite build 通过（注：全量注册体积较大，二期可改按需） | 已完成 | 自动验收通过 |

---

## M1 用户系统模块

> 对应 PRD §5.1、plan.md Phase 2。覆盖邮箱注册、登录/登出、个人资料。

### 1. 模块目标
- 用户可通过邮箱注册账号、收验证邮件、激活账号
- 已注册用户可登录/登出，登录态持久化（JWT + Redis）
- 用户可查看和编辑个人资料（头像、昵称、简介）
- 提供基础安全机制（密码加密、登录失败锁定、封禁拦截）

### 2. 用户可见内容
- **注册页**：邮箱、密码、昵称三个输入框 + 注册按钮
- **登录页**：邮箱、密码输入框 + 登录按钮 + 注册入口链接
- **邮箱验证页**：根据 URL 中 token 显示验证结果
- **编辑资料页**：头像（点击上传）、昵称（带 30 天冷却提示）、简介（200 字以内）
- **顶部导航栏**：未登录显示"登录/注册"，已登录显示头像 + 昵称 + 下拉菜单（设置/登出）

### 3. 用户操作流程
1. 访客访问 `/register` → 填表 → 提交 → 收到"验证邮件已发送"提示
2. 用户从邮箱点击验证链接 → 跳转 `/verify-email?token=xxx` → 显示"验证成功，自动登录"
3. 用户访问 `/login` → 输入凭据 → 跳转首页，右上角显示头像
4. 已登录用户点击"设置" → 进入 `/settings/profile` → 修改昵称/头像/简介 → 保存
5. 用户点击下拉菜单"登出" → 清除 Token → 返回首页

### 4. 前端页面任务
- `Register.vue`：注册表单 + 实时校验
- `Login.vue`：登录表单 + 锁定提示
- `VerifyEmail.vue`：根据 token 调接口显示结果
- `Settings.vue`：资料编辑 + 头像上传 + 昵称冷却提示
- `stores/user.js`：登录态、用户信息、Token 持久化
- `AppHeader.vue`：登录态切换显示（与 M2 协同）

### 5. Controller 任务
- `AuthController`：register / verify-email / resend-verification / login / logout
- `UserController`：GET /users/:id、PUT /users/profile、POST /users/avatar、GET /users/:id/posts（接口存在，列表二期返回空）

### 6. Service 任务
- `UserService`：注册、登录、密码加密、登录失败计数、锁定、封禁校验、资料更新（含昵称冷却）
- `MailService`：发送验证邮件
- `FileService`：头像上传（与 M3 共用）

### 7. Mapper 任务
- `UserMapper`：基础 CRUD + 按邮箱/昵称查询、更新登录失败次数、更新锁定时间
- `VerificationTokenMapper`：插入/查询/标记已使用/清理过期

### 8. Repository / 数据保存任务
- `user` 表：用户主信息（含 status / role / login_fail_count / locked_until / email_verified）
- `verification_token` 表：邮箱验证 token（24h 过期）
- Redis：JWT Token（7 天过期，便于踢人）+ 重发邮件冷却（60s）

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 访问 `/register` | 显示注册表单 |
| 2 | 输入合法邮箱/密码/昵称提交 | 提示"注册成功，验证邮件已发送" |
| 3 | 点击邮件中的验证链接 | 跳转 `/verify-email`，显示"验证成功" |
| 4 | 访问 `/login` 用刚注册账号登录 | 跳转首页，右上角显示头像 |
| 5 | 进入 `/settings/profile` 修改昵称 | 保存成功，顶栏昵称更新 |
| 6 | 上传头像（小于 2MB 的 JPG） | 头像更新，预览生效 |
| 7 | 点击"登出" | 返回首页，右上角恢复登录入口 |

### 10. 接口测试方法

| 步骤 | curl / Postman 请求 | 预期结果 |
|------|---------------------|---------|
| 1 | `POST /api/auth/register` 携带 email/password/nickname | code=0, 返回成功消息 |
| 2 | `POST /api/auth/verify-email` 携带 token | code=0, 用户 email_verified=1 |
| 3 | `POST /api/auth/login` 携带 email/password | code=0, 返回 token + user |
| 4 | `GET /api/users/{id}` 携带 Authorization 头 | code=0, 返回用户公开资料 |
| 5 | `PUT /api/users/profile` 修改昵称 | code=0, 数据库 nickname_updated_at 更新 |
| 6 | `POST /api/users/avatar` multipart 上传图片 | code=0, 返回 avatar URL |
| 7 | `POST /api/auth/logout` 携带 Token | code=0, Redis 中 Token 失效 |

### 11. 异常情况测试

| 场景 | 预期错误码/消息 |
|------|----------------|
| 邮箱已注册 | code=2002, "该邮箱已注册，请直接登录" |
| 昵称已存在 | code=2003, "昵称已被使用" |
| 密码强度不足（<6位/无字母数字） | code=2001, 实时校验提示 |
| 验证链接过期/已使用 | code=2001, "链接已过期" / "链接已使用" |
| 60s 内重发验证邮件 | code=2001, 提示冷却中 |
| 密码连续错误 5 次 | code=1004, 账号锁定 15 分钟 |
| 已封禁用户登录 | code=1003, "该账号已被封禁" |
| 锁定期内登录 | code=1004, "登录次数过多" |
| 未登录访问 `/api/users/profile` | code=1001, 跳登录页 |
| 昵称 30 天冷却内修改 | code=2004, 提示下次可修改时间 |
| 头像超过 2MB | code=4002, "图片大小不能超过 2MB" |
| 头像非图片格式 | code=4001, "请上传图片文件" |
| Token 过期再调接口 | code=1001, 前端跳登录页 |

### 12. 当前状态
- **任务状态**: 已完成
- **验收状态**: 自动验收通过
- **完成日期**: 2026-05-23
- **后端测试**: 66/66 通过
- **前端构建**: vite build 成功（1665 modules，页面代码拆分）

---

### 原子任务列表（M1）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|----|------|---------|---------|---------|------|------|
| M1-T1 | 创建 User 实体 | `entity/User.java` | M0 完成 | MyBatis-Plus 注解齐全，映射 user 表；编译通过 | 已完成 | 自动验收通过 |
| M1-T2 | 创建 UserMapper | `mapper/UserMapper.java` + 启动类加 @MapperScan | M1-T1 | 单测 selectById / selectByEmail / 软删除 / incrLoginFailCount 正确（5/5） | 已完成 | 自动验收通过 |
| M1-T3 | 创建 VerificationToken 实体 + Mapper | `entity/VerificationToken.java` + `mapper/VerificationTokenMapper.java` | M0 完成 | 单测 CRUD 正确（2/2） | 已完成 | 自动验收通过 |
| M1-T4 | 实现 MailService（发送验证邮件，QQ SMTP） | `service/MailService.java` | M0-T3 | 通过 QQ 邮箱授权码方式实际发送邮件成功（一次性手工测试已实发到 3168968763@qq.com）；MockBean 单测验证邮件构造（C7） | 已完成 | 自动验收通过 + 人工验收通过 |
| M1-T5 | 实现 UserService.register | `service/UserService.java` + `service/dto/RegisterRequest.java` | M1-T1, T3, T4 | 单测覆盖正常/邮箱重复/昵称重复/密码强度（3/3） | 已完成 | 自动验收通过 |
| M1-T6 | 实现 AuthController.register 接口 | `controller/AuthController.java` | M1-T5 | curl 注册成功，邮箱收到验证邮件；4 个边界单测全过（4/4） | 已完成 | 自动验收通过 |
| M1-T7 | 实现 verify-email 接口 | `controller/AuthController.java` | M1-T6 | 点击邮件链接后 user.email_verified=1 | 已完成 | 自动验收通过 |
| M1-T8 | 实现 resend-verification 接口（含 60s 冷却） | `controller/AuthController.java` | M1-T6 | 重复调用第二次返回冷却提示 | 已完成 | 自动验收通过 |
| M1-T9 | 实现 UserService.login（含失败锁定） | `service/UserService.java` | M1-T2 | 单测覆盖：成功/密码错/封禁/锁定/5次锁定/未注册（6/6） | 已完成 | 自动验收通过 |
| M1-T10 | 实现 JWT 工具类 | `common/JwtUtil.java` | M0-T11 | 单测生成/解析/校验 Token（6/6） | 已完成 | 自动验收通过 |
| M1-T11 | 实现 AuthController.login 接口 | `controller/AuthController.java` | M1-T9, T10 | curl 登录返回 token | 已完成 | 自动验收通过 |
| M1-T12 | 实现 AuthController.logout 接口 | `controller/AuthController.java` | M1-T11 | 登出后 Token 在 Redis 失效 | 已完成 | 自动验收通过 |
| M1-T13 | 实现 AuthInterceptor（Token 校验 + 用户注入） | `interceptor/AuthInterceptor.java` | M1-T10 | 受保护接口未带 Token 返回 1001 | 已完成 | 自动验收通过 |
| M1-T14 | 注册 AuthInterceptor 到 SecurityConfig | `config/ForumWebMvcConfig.java` | M1-T13 | 启动后拦截规则生效（公开/受保护路径分离） | 已完成 | 自动验收通过 |
| M1-T15 | 实现 UserService.getUserById / updateProfile | `service/UserService.java` | M1-T2 | 含昵称 30 天冷却/昵称重复校验 | 已完成 | 自动验收通过 |
| M1-T16 | 实现 UserController.getUser / updateProfile | `controller/UserController.java` | M1-T15 | GET 公开 + PUT 需登录 | 已完成 | 自动验收通过 |
| M1-T17 | 实现 FileService（图片上传基础） | `service/FileService.java` | M0-T12 | 格式/大小校验 + 本地存储 | 已完成 | 自动验收通过 |
| M1-T18 | 实现 UserController.uploadAvatar | `controller/UserController.java` | M1-T17 | POST multipart 上传头像 | 已完成 | 自动验收通过 |
| M1-T19 | 实现前端 user store（登录态/Token 持久化） | `stores/user.js` | M0-T16 | localStorage 持久化 + login/logout/updateInfo actions | 已完成 | 自动验收通过 |
| M1-T20 | 实现前端 api/auth.js | `api/auth.js` | M0-T17 | register/login/logout/verifyEmail/resendVerification | 已完成 | 自动验收通过 |
| M1-T21 | 实现前端 api/user.js | `api/user.js` | M0-T17 | getUserById/updateProfile/uploadAvatar | 已完成 | 自动验收通过 |
| M1-T22 | 实现 Register.vue 页面 | `views/Register.vue` | M1-T20 | 表单校验 + 提交 + 跳转登录 | 已完成 | 自动验收通过 |
| M1-T23 | 实现 Login.vue 页面 | `views/Login.vue` | M1-T20 | 登录 + 跳转 + redirect 参数 | 已完成 | 自动验收通过 |
| M1-T24 | 实现 VerifyEmail.vue 页面 | `views/VerifyEmail.vue` | M1-T20 | token 验证 + 成功/失败结果展示 | 已完成 | 自动验收通过 |
| M1-T25 | 实现 Settings.vue 编辑资料页 | `views/Settings.vue` | M1-T21 | 昵称/简介/头像编辑 + 30天冷却提示 | 已完成 | 自动验收通过 |
| M1-T26 | 实现 AppHeader 登录态切换 | `components/layout/AppHeader.vue` | M1-T19 | 登录/登出 + 下拉菜单 + 头像显示 | 已完成 | 自动验收通过 |
| M1-T27 | 实现前端路由守卫（未登录跳转） | `router/index.js` | M1-T19 | requiresAuth 跳 /login + guest 已登录跳 / | 已完成 | 自动验收通过 |

---

## M2 版块系统模块

> 对应 PRD §5.2、plan.md Phase 3。一期前台仅需"版块列表展示"，CRUD 在 M6 后台模块。

### 1. 模块目标
- 用户可在首页和导航栏看到版块列表，按版块进入帖子页
- 提供版块查询接口给帖子模块使用

### 2. 用户可见内容
- **首页 Sidebar**：版块列表（名称 + 帖子数）
- **首页主区**：各版块最新 3 篇帖子（依赖 M3）
- **版块帖子页 `/board/:id`**：版块名 + 描述 + 帖子列表（依赖 M3）

### 3. 用户操作流程
1. 用户访问首页 `/` → 看到左侧版块列表
2. 点击某个版块 → 跳转 `/board/:id` 看到该版块所有帖子
3. 点击 Logo / "首页" → 返回 `/`

### 4. 前端页面任务
- `Home.vue`：首页布局 + 版块列表 + 各版块预览帖子区
- `AppSidebar.vue`：版块导航组件（左侧）
- `AppHeader.vue`：顶部导航（Logo + 搜索框占位 + 用户区）

### 5. Controller 任务
- `BoardController`：GET /api/boards（启用的版块）、GET /api/boards/:id

### 6. Service 任务
- `BoardService`：版块列表查询（仅 status=1 且 deleted=0）、版块详情查询、post_count 维护辅助方法

### 7. Mapper 任务
- `BoardMapper`：列表查询（按 sort_weight DESC 排序）、详情查询、post_count 原子增减

### 8. Repository / 数据保存任务
- `board` 表：版块主信息（name / description / sort_weight / status / post_count）
- 初始化 SQL：插入 3 个种子版块（如：技术交流、生活日常、资源分享）方便开发自测

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 启动前后端 + 数据库（含种子版块） | 首页 Sidebar 显示 3 个版块 |
| 2 | 点击版块"技术交流" | 跳转 `/board/1`，标题正确 |
| 3 | 后台禁用某版块后刷新前台 | 该版块从前台列表中消失 |

### 10. 接口测试方法

| 步骤 | curl/Postman | 预期 |
|------|--------------|------|
| 1 | `GET /api/boards` | code=0, data 为启用版块数组，含 postCount |
| 2 | `GET /api/boards/1` | code=0, 返回该版块详情 |
| 3 | `GET /api/boards/999`（不存在） | code=3003, "版块不存在" |
| 4 | 后台将某版块 status=0 后调用 `GET /api/boards` | 该版块不在返回列表 |

### 11. 异常情况测试

| 场景 | 预期 |
|------|------|
| 数据库无版块 | 返回空数组，前端显示"暂无版块" |
| 版块 ID 不存在 | code=3003 |
| 已禁用版块前台直接访问 `/board/:id` | 返回 3003 或友好提示 |
| 已删除版块前台访问 | 返回 3003 |

### 12. 当前状态
- **任务状态**: 已完成
- **验收状态**: 自动验收通过
- **完成日期**: 2026-05-23
- **后端测试**: 71/71 通过（含 BoardServiceTest 5/5）
- **前端构建**: vite build 成功（Home 单独 chunk）

---

### 原子任务列表（M2）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|----|------|---------|---------|---------|------|------|
| M2-T1 | 创建 Board 实体 | `entity/Board.java` | M0 完成 | MyBatis-Plus 注解齐全，映射 board 表 | 已完成 | 自动验收通过 |
| M2-T2 | 创建 BoardMapper | `mapper/BoardMapper.java` | M2-T1 | selectEnabledList + incrPostCount/decrPostCount | 已完成 | 自动验收通过 |
| M2-T3 | 实现 BoardService（列表/详情） | `service/BoardService.java` | M2-T2 | 5/5 测试通过（启用过滤/排序/3003/禁用过滤/incr） | 已完成 | 自动验收通过 |
| M2-T4 | 实现 BoardController.list 接口 | `controller/BoardController.java` | M2-T3 | GET /api/boards 公开返回启用版块 | 已完成 | 自动验收通过 |
| M2-T5 | 实现 BoardController.detail 接口 | `controller/BoardController.java` | M2-T3 | GET /api/boards/{id} 公开；不存在/禁用 3003 | 已完成 | 自动验收通过 |
| M2-T6 | 种子数据已合并到 M0-T4b（默认管理员 + 3 个版块） | `db/seed.sql` | M0-T4b | 与 M0-T4b 一并验收，本行仅作模块内引用 | 已完成 | 无需验收 |
| M2-T7 | 前端 api/board.js | `api/board.js` | M0-T17 | listBoards / getBoardById | 已完成 | 自动验收通过 |
| M2-T8 | 实现 AppHeader.vue | `components/layout/AppHeader.vue` | M1-T26 | 已在 M1-T26 实现 | 已完成 | 自动验收通过 |
| M2-T9 | 实现 AppSidebar.vue（版块导航） | `components/layout/AppSidebar.vue` | M2-T7 | 列出版块 + 帖子数 Tag + 跳转 /board/:id | 已完成 | 自动验收通过 |
| M2-T10 | 实现 AppFooter.vue | `components/layout/AppFooter.vue` | M0-T14 | 页脚显示版权 + 技术栈 | 已完成 | 自动验收通过 |
| M2-T11 | 实现 Home.vue 首页布局 | `views/Home.vue` | M2-T8, T9, T10 | 左侧 Sidebar + 右侧主区版块预览卡片 | 已完成 | 自动验收通过 |
| M2-T12 | 在 app store 中缓存版块列表 | `stores/app.js` | M2-T7 | boardsLoaded 标记 + loadBoards(force) action | 已完成 | 自动验收通过 |

---

## M3 帖子系统模块

> 对应 PRD §5.3、plan.md Phase 4。覆盖发帖、列表、详情、编辑、删除、图片上传。

### 1. 模块目标
- 登录用户可在指定版块发布图文帖子（标题 + 富文本正文 + 最多 9 张图）
- 任何人可浏览帖子列表（按版块筛选 / 分页）和帖子详情
- 帖子作者可编辑/删除自己的帖子（软删除）
- 支持图片上传

### 2. 用户可见内容
- **版块帖子列表 `/board/:id`**：帖子卡片（标题 / 作者头像昵称 / 时间 / 点赞数 / 评论数 / 摘要 / 首图缩略）
- **帖子详情 `/post/:id`**：完整内容、所有图片、作者信息、点赞收藏按钮、评论区（依赖 M4）
- **发帖页 `/post/create`**：版块下拉、标题、富文本编辑器、图片上传区
- **编辑帖子页 `/post/:id/edit`**：与发帖页相同，预填充原数据
- **置顶帖子**：始终在列表最前

### 3. 用户操作流程
1. 登录用户点击"发帖" → 进入 `/post/create` → 选择版块 → 填写标题/内容/图片 → 提交 → 跳转新帖详情
2. 浏览首页/版块页 → 看到帖子卡片 → 点击进入 `/post/:id` 查看完整内容
3. 作者点击"编辑" → 进入 `/post/:id/edit` 修改 → 保存（显示"已编辑"标记）
4. 作者点击"删除" → 二次确认 → 帖子从前台消失（软删除）

### 4. 前端页面任务
- `PostCard.vue`：帖子卡片
- `PostList.vue`：列表 + 分页/加载更多
- `Board.vue`：版块帖子页（用 PostList）
- `PostDetail.vue`：详情页（含图片展示、点赞收藏入口、评论区挂载点）
- `PostCreate.vue`：发帖页
- `PostEdit.vue`：编辑页
- `PostEditor.vue`：WangEditor 封装
- `ImageUpload.vue`：图片上传组件（最多 9 张）

### 5. Controller 任务
- `PostController`：list / detail / create / update / delete
- `FileController`：upload/image

### 6. Service 任务
- `PostService`：发帖（含版块校验、图片关联）、列表（分页 + 排序 + 置顶处理）、详情（含浏览数+1）、编辑（仅作者）、软删除（含级联评论标记）
- `FileService`：图片保存、格式/大小校验、返回访问 URL

### 7. Mapper 任务
- `PostMapper`：列表查询（含版块筛选/置顶排序/分页）、详情、浏览数原子递增、软删除
- `PostImageMapper`：批量插入、按 post_id 查询

### 8. Repository / 数据保存任务
- `post` 表：帖子主信息（含 is_pinned / is_edited / 各计数字段 / status / deleted）
- `post_image` 表：每帖最多 9 张图
- 图片文件保存到 `forum.upload.path` 配置目录（**待确认**具体路径策略）

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 登录后点击"发帖" | 进入 `/post/create` |
| 2 | 选版块、填标题/正文、上传 2 张图 → 提交 | 跳转新帖详情，标题/正文/图片正确 |
| 3 | 返回版块页 | 列表第一条是刚发的帖（按时间倒序） |
| 4 | 点击编辑 → 修改标题 → 保存 | 详情页标题更新，显示"已编辑" |
| 5 | 点击删除 → 确认 | 列表中该帖消失 |
| 6 | 未登录访问 `/post/create` | 跳转登录页 |
| 7 | 后台将某帖置顶 | 列表中该帖永远第一 |

### 10. 接口测试方法

| 步骤 | curl/Postman | 预期 |
|------|--------------|------|
| 1 | `POST /api/upload/image` 上传 JPG | code=0, 返回 URL |
| 2 | `POST /api/posts` 携带 boardId/title/content/imageUrls | code=0, 返回新帖 id |
| 3 | `GET /api/posts?boardId=1&page=1&size=20` | code=0, 分页结构正确，置顶在前 |
| 4 | `GET /api/posts/{id}` | code=0, 返回完整正文/图片/作者；浏览数+1 |
| 5 | `PUT /api/posts/{id}` 修改标题 | code=0, is_edited=1, last_edited_at 更新 |
| 6 | `DELETE /api/posts/{id}` | code=0, deleted=1 |
| 7 | 非作者调用 `PUT/DELETE` | code=1002, 无权限 |

### 11. 异常情况测试

| 场景 | 预期 |
|------|------|
| 标题为空 | code=2001 |
| 正文为空 | code=2001 |
| 选择已禁用版块 | 列表不展示；强行传则 code=3003 |
| 图片数量超过 9 张 | code=4003 |
| 图片格式非 JPG/PNG/GIF | code=4001 |
| 单图超过 5MB | code=4002 |
| 访问已删除帖子 | code=3001 |
| 编辑/删除他人帖子 | code=1002 |
| 同一用户 10s 内连续发帖 | 触发限流（M7 实现） |
| 图片上传中网络中断 | 前端保留已编辑内容，提示"网络异常" |
| 帖子作者被封禁 | 帖子可看，作者信息显示"已注销" |

### 12. 当前状态
- **任务状态**: 已完成
- **验收状态**: 自动验收通过
- **完成日期**: 2026-05-23
- **后端测试**: 81/81 通过（含 PostServiceTest 10/10）
- **前端构建**: vite build 成功（PostList/PostDetail/PostCreate/PostEdit/Board 等代码拆分）

---

### 原子任务列表（M3）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|----|------|---------|---------|---------|------|------|
| M3-T1 | 创建 Post 实体 | `entity/Post.java` | M0 完成 | MyBatis-Plus 注解齐全 | 已完成 | 自动验收通过 |
| M3-T2 | 创建 PostImage 实体 | `entity/PostImage.java` | M0 完成 | 映射 post_image | 已完成 | 自动验收通过 |
| M3-T3 | 创建 PostMapper | `mapper/PostMapper.java` | M3-T1 | incrViewCount + like/comment 计数原子操作 | 已完成 | 自动验收通过 |
| M3-T4 | 创建 PostImageMapper | `mapper/PostImageMapper.java` | M3-T2 | selectByPostId / deleteByPostId | 已完成 | 自动验收通过 |
| M3-T5a | 定义 FileStorage 抽象接口 | `service/storage/FileStorage.java` | M0-T12 | save/delete 两个方法（C1） | 已完成 | 自动验收通过 |
| M3-T5 | 实现 LocalFileStorage（本地实现 + 校验） | `service/storage/LocalFileStorage.java` + 重构 FileService | M3-T5a | FileService 校验 + LocalFileStorage 落盘；FileService 现有测试继承通过 | 已完成 | 自动验收通过 |
| M3-T6 | 实现 FileController.uploadImage | `controller/FileController.java` | M3-T5, M1-T14 | POST /api/upload/image multipart 接口 | 已完成 | 自动验收通过 |
| M3-T7 | 实现 PostService.createPost | `service/PostService.java` | M3-T3, T4, M2-T3 | 3/3 测试通过（正常/版块3003/图片4003）+ board.post_count++ | 已完成 | 自动验收通过 |
| M3-T8 | 实现 PostController.create | `controller/PostController.java` | M3-T7 | POST /api/posts 需登录 | 已完成 | 自动验收通过 |
| M3-T9 | 实现 PostService.listPosts | `service/PostService.java` | M3-T3 | 分页 + 置顶排序 + 摘要 + 首图 + 作者；1/1 测试通过 | 已完成 | 自动验收通过 |
| M3-T10 | 实现 PostController.list | `controller/PostController.java` | M3-T9 | GET /api/posts 公开 | 已完成 | 自动验收通过 |
| M3-T11 | 实现 PostService.getPostDetail | `service/PostService.java` | M3-T3, T4 | 含图片/作者/版块；viewCount+1（2/2 通过） | 已完成 | 自动验收通过 |
| M3-T12 | 实现 PostController.detail | `controller/PostController.java` | M3-T11 | GET /api/posts/{id} 公开 | 已完成 | 自动验收通过 |
| M3-T13 | 实现 PostService.updatePost（仅作者） | `service/PostService.java` | M3-T3 | 作者可编辑（is_edited=1）；他人 1002（2/2） | 已完成 | 自动验收通过 |
| M3-T14 | 实现 PostController.update | `controller/PostController.java` | M3-T13 | PUT /api/posts/{id} 需登录 | 已完成 | 自动验收通过 |
| M3-T15 | 实现 PostService.deletePost（软删除） | `service/PostService.java` | M3-T3 | 软删 + post_count--（2/2 通过；评论级联留 M4） | 已完成 | 自动验收通过 |
| M3-T16 | 实现 PostController.delete | `controller/PostController.java` | M3-T15 | DELETE /api/posts/{id} 需登录 + 管理员可越权 | 已完成 | 自动验收通过 |
| M3-T17 | 前端 api/post.js | `api/post.js` | M0-T17 | 6 个接口（list/get/create/update/delete/uploadImage） | 已完成 | 自动验收通过 |
| M3-T18 | 实现 ImageUpload.vue | `components/common/ImageUpload.vue` | M3-T6 | 9 张上限 + 5MB 校验 + 缩略图/删除 | 已完成 | 自动验收通过 |
| M3-T19 | 实现 PostEditor.vue（WangEditor 封装） | `components/post/PostEditor.vue` | M0-T18 | WangEditor 5 + customUpload 集成 | 已完成 | 自动验收通过 |
| M3-T20 | 实现 PostCard.vue | `components/post/PostCard.vue` | M3-T17 | 标题/摘要/作者/计数/首图缩略 + 置顶/已编辑 Tag | 已完成 | 自动验收通过 |
| M3-T21 | 实现 PostList.vue（分页） | `components/post/PostList.vue` | M3-T20 | boardId 切换 + 分页 + EmptyState | 已完成 | 自动验收通过 |
| M3-T22 | 实现 Pagination.vue 通用组件 | `components/common/Pagination.vue` | M0-T14 | el-pagination 封装 | 已完成 | 自动验收通过 |
| M3-T23 | 实现 EmptyState.vue 空状态组件 | `components/common/EmptyState.vue` | M0-T14 | el-empty 封装 | 已完成 | 自动验收通过 |
| M3-T24 | 实现 Board.vue 版块帖子页 | `views/Board.vue` | M3-T21 | 版块标题/描述 + 帖子列表 + 发帖入口 | 已完成 | 自动验收通过 |
| M3-T25 | 实现 PostCreate.vue 发帖页 | `views/PostCreate.vue` | M3-T18, T19 | 版块选择/标题/正文/图片，提交跳详情 | 已完成 | 自动验收通过 |
| M3-T26 | 实现 PostEdit.vue 编辑页 | `views/PostEdit.vue` | M3-T25 | 预填充 + 保存 | 已完成 | 自动验收通过 |
| M3-T27 | 实现 PostDetail.vue（不含评论交互） | `views/PostDetail.vue` | M3-T17 | 标题/正文 HTML 渲染/图片 preview/作者/编辑删除按钮 | 已完成 | 自动验收通过 |
| M3-T28 | 更新 Home.vue 显示最新帖 | `views/Home.vue` | M3-T20, M2-T11 | 首页直接展示全站最新帖列表（简化版） | 已完成 | 自动验收通过 |

---

## M4 互动系统模块（点赞 / 评论 / 收藏）

> 对应 PRD §5.4、plan.md Phase 5。包含点赞（幂等）、树形评论（最多 2 层）、收藏（幂等）。

### 1. 模块目标
- 用户可对帖子点赞/取消点赞（同帖同人幂等，前端防抖）
- 用户可对帖子评论，可回复他人评论（最多 2 层嵌套）
- 用户可收藏/取消收藏帖子（同帖同人幂等）
- 帖子的点赞/评论计数实时更新

### 2. 用户可见内容
- **帖子详情页**：
  - 点赞按钮（数字 + 高亮状态）
  - 收藏按钮（图标 + 高亮状态）
  - 评论区（树形结构展示，2 层嵌套）
  - 评论输入框（顶部）
  - 每条评论的"回复"按钮 + "删除"按钮（仅作者可见）

### 3. 用户操作流程
1. 用户在帖子详情点击"点赞" → 按钮高亮，数字 +1；再次点击取消
2. 用户在评论输入框写评论 → 提交 → 出现在评论列表
3. 点击某条评论"回复" → 展开输入框 → 提交 → 作为子级评论
4. 点击自己的评论"删除" → 软删除，前台不展示
5. 点击"收藏" → 按钮高亮；再点取消

### 4. 前端页面任务
- 点赞按钮（集成在 PostDetail，含防抖）
- 收藏按钮（集成在 PostDetail，含防抖）
- `CommentTree.vue`：树形组装与渲染
- `CommentItem.vue`：单条评论（含回复/删除按钮）
- `CommentForm.vue`：评论输入框（顶级 + 嵌入子级）

### 5. Controller 任务
- `LikeController`：POST/DELETE /api/posts/:id/like
- `CommentController`：GET /api/posts/:postId/comments、POST /api/posts/:postId/comments、DELETE /api/comments/:id
- `FavoriteController`：POST/DELETE /api/posts/:id/favorite、GET /api/users/:id/favorites（一期接口存在，列表在二期）

### 6. Service 任务
- `LikeService`：幂等点赞/取消（UNIQUE 兜底）、post.like_count 原子增减
- `CommentService`：发表评论（含层级校验）、查询评论（应用层组装树）、软删除评论
- `FavoriteService`：幂等收藏/取消

### 7. Mapper 任务
- `PostLikeMapper`：按 post_id+user_id 查询、插入、删除
- `CommentMapper`：按 post_id 查询（含按时间排序、过滤已删除）、插入、按 id 软删除
- `PostFavoriteMapper`：按 post_id+user_id 查询、插入、删除

### 8. Repository / 数据保存任务
- `post_like` 表：UNIQUE (post_id, user_id) 保证幂等
- `comment` 表：邻接表（parent_id），depth 字段限制层级
- `post_favorite` 表：UNIQUE (post_id, user_id)
- `post.like_count` / `post.comment_count` 冗余计数原子更新

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 登录后进入某帖详情，点击点赞 | 数字 +1，按钮高亮 |
| 2 | 再次点击点赞 | 数字 -1，按钮恢复 |
| 3 | 快速连续点击 5 次 | 防抖，最终只生效一次 |
| 4 | 在评论框写"沙发" → 提交 | 评论区出现一条评论 |
| 5 | 点击该评论"回复" → 写"+1" → 提交 | 出现在子级 |
| 6 | 对子级评论点"回复" | 按钮不可用（已达层级上限） |
| 7 | 点击自己评论"删除" | 评论消失（软删除） |
| 8 | 点击"收藏" | 图标高亮；再点取消 |

### 10. 接口测试方法

| 步骤 | curl/Postman | 预期 |
|------|--------------|------|
| 1 | `POST /api/posts/1/like` | code=0, liked=true, likeCount=N+1 |
| 2 | 再次 `POST /api/posts/1/like` | code=0, liked=true, likeCount 不变（幂等） |
| 3 | `DELETE /api/posts/1/like` | code=0, liked=false, likeCount=N |
| 4 | `POST /api/posts/1/comments` body=`{"content":"x"}` | code=0, 返回评论 id |
| 5 | `POST /api/posts/1/comments` body=`{"content":"y","parentId":<上一条 id>}` | code=0, depth=2 |
| 6 | `POST` parentId 指向 depth=2 的评论 | code=2001, "回复层级已达上限" |
| 7 | `GET /api/posts/1/comments` | code=0, 树形结构正确 |
| 8 | `DELETE /api/comments/{id}` 由非作者调用 | code=1002 |
| 9 | `POST /api/posts/1/favorite` | code=0 |
| 10 | 再次 `POST /api/posts/1/favorite` | code=0（幂等） |

### 11. 异常情况测试

| 场景 | 预期 |
|------|------|
| 对已删除帖子点赞 | code=3001 |
| 评论内容为空 | code=2001 |
| 评论超过 1000 字 | code=2001 |
| 回复不存在的父评论 | code=3002 |
| 回复属于其他帖子的评论 | code=2001 |
| 评论已删除帖子 | code=3001 |
| 删除他人评论 | code=1002 |
| 同用户 5s 内连续评论 | 触发限流（M7 实现） |
| 收藏已删除帖子 | code=3001 |
| 并发点赞（同时两次请求） | UNIQUE 约束保证只入一条 |

### 12. 当前状态
- **任务状态**: 未开始
- **验收状态**: 待验收

---

### 原子任务列表（M4）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|----|------|---------|---------|---------|------|------|
| M4-T1 | 创建 PostLike 实体 + Mapper | `entity/PostLike.java` + `mapper/PostLikeMapper.java` | M3 完成 | UNIQUE 约束 + 幂等单测 | 已完成 | 自动验收通过 |
| M4-T2 | 实现 LikeService（幂等） | `service/LikeService.java` | M4-T1 | 单测覆盖：未点→点；已点→取消；并发兜底 | 已完成 | 自动验收通过 |
| M4-T3 | 实现 LikeController.like / unlike | `controller/LikeController.java` | M4-T2 | POST/DELETE /api/posts/{id}/like | 已完成 | 自动验收通过 |
| M4-T4 | 前端 api/like.js + 帖子详情集成点赞按钮（含 loading） | `api/like.js` + `views/PostDetail.vue` | M4-T3 | 切换/loading 防抖 | 已完成 | 自动验收通过 |
| M4-T5 | 创建 Comment 实体 + Mapper | `entity/Comment.java` + `mapper/CommentMapper.java` | M3 完成 | selectByPostId + 软删过滤 | 已完成 | 自动验收通过 |
| M4-T6 | 实现 CommentService.createComment（含 depth 校验） | `service/CommentService.java` | M4-T5 | 顶级/2 层/跨帖父拒绝/depth=2 时父级自动提升 | 已完成 | 自动验收通过 |
| M4-T7 | 实现 CommentService.listComments（树形组装） | `service/CommentService.java` | M4-T5 | 应用层组装 + 批量查用户 + 已注销兜底 | 已完成 | 自动验收通过 |
| M4-T8 | 实现 CommentService.deleteComment（仅作者/admin） | `service/CommentService.java` | M4-T5 | 鉴权 1002 + decr | 已完成 | 自动验收通过 |
| M4-T9 | 实现 CommentController（list/create/delete） | `controller/CommentController.java` | M4-T6, T7, T8 | 3 个接口 | 已完成 | 自动验收通过 |
| M4-T10 | 前端 api/comment.js | `api/comment.js` | M0-T17 | list/create/delete | 已完成 | 自动验收通过 |
| M4-T11 | 实现 CommentItem.vue | `components/comment/CommentItem.vue` | M4-T10 | 头像/作者/回复目标/时间/回复/删除 | 已完成 | 自动验收通过 |
| M4-T12 | 实现 CommentTree.vue | `components/comment/CommentTree.vue` | M4-T11 | 顶级 + children 2 层布局 | 已完成 | 自动验收通过 |
| M4-T13 | 实现 CommentForm.vue | `components/comment/CommentForm.vue` | M4-T10 | 顶级/子级共用 + cancellable | 已完成 | 自动验收通过 |
| M4-T14 | 在 PostDetail.vue 集成评论区 | `views/PostDetail.vue` | M4-T12, T13 | 未登录提示 + 提交 + 删除 + 即时刷新 | 已完成 | 自动验收通过 |
| M4-T15 | 创建 PostFavorite 实体 + Mapper | `entity/PostFavorite.java` + `mapper/PostFavoriteMapper.java` | M3 完成 | UNIQUE 约束 | 已完成 | 自动验收通过 |
| M4-T16 | 实现 FavoriteService（幂等） | `service/FavoriteService.java` | M4-T15 | 单测覆盖收藏/取消 | 已完成 | 自动验收通过 |
| M4-T17 | 实现 FavoriteController | `controller/FavoriteController.java` | M4-T16 | POST/DELETE | 已完成 | 自动验收通过 |
| M4-T18 | 前端 api/favorite.js + 收藏按钮集成 | `api/favorite.js` + `views/PostDetail.vue` | M4-T17 | 切换 + loading | 已完成 | 自动验收通过 |

---

## M5 搜索模块

> 对应 PRD §5.5、plan.md Phase 6。基于 MySQL FULLTEXT 的关键词搜索。

### 1. 模块目标
- 用户可通过关键词搜索帖子（标题 + 正文）
- 搜索结果按相关度或时间排序，支持分页

### 2. 用户可见内容
- **顶部导航搜索框**：输入关键词回车
- **搜索结果页 `/search?q=xxx`**：
  - 标题"搜索：xxx"
  - 结果列表（与帖子列表相同卡片样式，关键词高亮）
  - 无结果时友好提示

### 3. 用户操作流程
1. 用户在顶栏搜索框输入"Spring" → 回车 → 跳转 `/search?q=Spring`
2. 查看结果列表 → 点击某帖进入详情

### 4. 前端页面任务
- `AppHeader.vue`：搜索框输入逻辑
- `Search.vue`：搜索结果页
- 关键词高亮工具函数

### 5. Controller 任务
- `SearchController`：GET /api/search?q=xxx&page=1&size=20

### 6. Service 任务
- `SearchService`：基于 MySQL FULLTEXT 查询帖子（特殊字符转义）

### 7. Mapper 任务
- `PostMapper.searchByKeyword`：使用 MATCH...AGAINST 查询（idx_search 索引）

### 8. Repository / 数据保存任务
- 复用 `post` 表上的 `FULLTEXT idx_search (title, content)` 索引
- 无新增表

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 在顶栏搜索框输入"测试"回车 | 跳转 `/search?q=测试`，显示结果列表 |
| 2 | 输入不存在的关键词 | 显示"未找到相关帖子" |
| 3 | 输入仅 1 字 | 提示"关键词至少 2 个字符" |
| 4 | 关键词在标题中匹配 | 标题中关键词高亮 |

### 10. 接口测试方法

| 步骤 | curl | 预期 |
|------|------|------|
| 1 | `GET /api/search?q=Spring&page=1&size=20` | code=0, 分页结构正确 |
| 2 | `GET /api/search?q=不存在的词` | code=0, total=0, list=[] |
| 3 | `GET /api/search?q=a`（<2 字符） | code=2001 |
| 4 | `GET /api/search?q=%27;DROP TABLE`（注入尝试） | 正常返回空，不报 500 |

### 11. 异常情况测试

| 场景 | 预期 |
|------|------|
| 关键词为空 | code=2001 |
| 关键词 <2 字符 | code=2001 |
| 关键词含 SQL 特殊字符 | 自动转义，安全返回 |
| 短时间内高频搜索 | 触发限流（M7 实现） |
| 已删除帖子命中 | 不返回（WHERE deleted=0） |

### 12. 当前状态
- **任务状态**: 未开始
- **验收状态**: 待验收

---

### 原子任务列表（M5）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|----|------|---------|---------|---------|------|------|
| M5-T1 | 在 schema.sql 中确认 idx_search 索引存在 | `db/schema.sql` | M0-T4 | FULLTEXT idx_search(title,content) WITH PARSER ngram 已建（M0 时建） | 已完成 | 自动验收通过 |
| M5-T2 | 实现 PostMapper.searchByKeyword | `mapper/PostMapper.java` | M5-T1 | MATCH AGAINST IN BOOLEAN MODE + 分页 | 已完成 | 自动验收通过 |
| M5-T3 | 实现 SearchService.search | `service/SearchService.java` | M5-T2 | 5/5 测试通过（空/短/命中/无结果/注入） | 已完成 | 自动验收通过 |
| M5-T4 | 实现 SearchController | `controller/SearchController.java` | M5-T3 | GET /api/search 公开 | 已完成 | 自动验收通过 |
| M5-T5 | 前端 api/search.js | `api/search.js` | M0-T17 | searchPosts(q, params) | 已完成 | 自动验收通过 |
| M5-T6 | 在 AppHeader 实现搜索框逻辑 | `components/layout/AppHeader.vue` | M5-T5 | 输入回车跳 /search?q= + 路由同步 | 已完成 | 自动验收通过 |
| M5-T7 | 实现 Search.vue 页面 | `views/Search.vue` | M5-T5, M3-T21 | 标题/总数/列表+关键词高亮/分页/空状态 | 已完成 | 自动验收通过 |

---

## M6 后台管理模块

> 对应 PRD §6、plan.md Phase 7。覆盖管理员登录、用户管理、版块 CRUD、帖子/评论管理。

### 1. 模块目标
- 提供独立的后台登录入口（与前台 Token 隔离）
- 管理员可管理用户（封禁/解封/重置密码）
- 管理员可对版块进行 CRUD（含启用/禁用）
- 管理员可管理帖子（删除/恢复/置顶）和评论（删除/恢复）

### 2. 用户可见内容
- **后台登录页 `/admin/login`**：邮箱 + 密码 + 登录按钮
- **后台布局**：顶栏 + 左侧菜单（用户/帖子/评论/版块管理） + 主区
- **用户管理页**：列表（搜索/分页） + 详情 + 封禁/解封/重置密码按钮
- **版块管理页**：列表 + 创建/编辑/删除/启用禁用按钮
- **帖子管理页**：列表（含已删除筛选） + 删除/恢复/置顶按钮
- **评论管理页**：按帖子查看评论 + 删除/恢复按钮

### 3. 用户操作流程
1. 管理员访问 `/admin/login` → 登录 → 进入后台
2. 进入"用户管理" → 搜索某用户 → 点"封禁"填原因 → 该用户无法登录前台
3. 进入"版块管理" → 创建新版块 → 设置排序/启用 → 前台可见
4. 进入"帖子管理" → 删除违规帖子 → 前台不展示
5. 进入"帖子管理" → 切换到"已删除"筛选 → 恢复某帖
6. 进入"评论管理" → 选择某帖 → 删除违规评论 → 可恢复

### 4. 前端页面任务
- `AdminLogin.vue`：后台登录
- `AdminLayout.vue`：后台布局（顶栏 + 侧边菜单 + router-view）
- `AdminUsers.vue`：用户管理
- `AdminBoards.vue`：版块管理
- `AdminPosts.vue`：帖子管理（含恢复/置顶）
- `AdminComments.vue`：评论管理

### 5. Controller 任务
- `AdminAuthController`：login
- `AdminUserController`：list / detail / ban / unban / resetPassword
- `AdminBoardController`：CRUD + status
- `AdminPostController`：list（含已删除） / delete / restore / pin
- `AdminCommentController`：list / delete / restore

### 6. Service 任务
- 复用 UserService / BoardService / PostService / CommentService 的内部方法，提供 admin 专用方法（如 `adminListPosts(includeDeleted)`、`restorePost`、`pinPost`）

### 7. Mapper 任务
- 在现有 Mapper 上增加：管理员列表查询（含 deleted）、按 status 筛选、ban/unban 更新方法、is_pinned 更新

### 8. Repository / 数据保存任务
- 复用现有 user / board / post / comment 表
- 后台操作建议记录到日志（一期可使用 Spring 日志即可，**待确认**是否需要 audit_log 表）

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 访问 `/admin/login` 用管理员账号登录 | 进入后台首页 |
| 2 | 进入"版块管理" → 新建"测试版块" | 列表新增；前台可见 |
| 3 | 进入"用户管理" → 封禁某测试账号 | 该账号前台立即无法登录 |
| 4 | 解封该账号 | 重新可登录 |
| 5 | 进入"帖子管理" → 删除某帖 | 前台该帖消失 |
| 6 | 切换到"已删除"筛选 → 恢复该帖 | 前台又可见 |
| 7 | 置顶某帖 | 前台列表中该帖永远第一 |
| 8 | 进入"评论管理" → 删除某评论 | 前台不展示；可恢复 |

### 10. 接口测试方法

| 步骤 | curl | 预期 |
|------|------|------|
| 1 | `POST /api/admin/auth/login` | code=0, 返回管理员 token |
| 2 | `GET /api/admin/users?keyword=xxx&page=1` | code=0, 分页结构正确 |
| 3 | `PUT /api/admin/users/{id}/ban` body=`{"reason":"违规"}` | code=0, user.status=0 |
| 4 | `PUT /api/admin/users/{id}/unban` | code=0, status=1 |
| 5 | `POST /api/admin/boards` 创建版块 | code=0, 新版块写入 |
| 6 | `PUT /api/admin/boards/{id}/status` | code=0, 启用/禁用切换 |
| 7 | `DELETE /api/admin/boards/{id}`（有帖子） | 返回需确认信息 |
| 8 | `GET /api/admin/posts?includeDeleted=true` | 含已删除帖子 |
| 9 | `DELETE /api/admin/posts/{id}` | post.deleted=1 |
| 10 | `PUT /api/admin/posts/{id}/restore` | post.deleted=0 |
| 11 | `PUT /api/admin/posts/{id}/pin` | post.is_pinned 切换 |
| 12 | `DELETE /api/admin/comments/{id}` | comment.deleted=1 |
| 13 | `PUT /api/admin/comments/{id}/restore` | comment.deleted=0 |

### 11. 异常情况测试

| 场景 | 预期 |
|------|------|
| 普通用户尝试访问 `/api/admin/*` | code=1002, 无权限 |
| 管理员封禁自己 | code=1002, "不能封禁自己的账号" |
| 管理员封禁其他管理员 | 需二次确认机制（前端弹窗） |
| 后台登录失败 5 次 | 锁定 30 分钟 |
| 删除有帖子的版块未二次确认 | 提示需确认（携带 confirm=true 才执行） |
| 重置密码后老 Token 仍可用？ | 应使其失效（**待确认**实现策略） |
| 后台 Token 与前台 Token 混用 | 校验 role=admin，否则 1002 |

### 12. 当前状态
- **任务状态**: 未开始
- **验收状态**: 待验收

---

### 原子任务列表（M6）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|----|------|---------|---------|---------|------|------|
| M6-T1 | 实现 AdminInterceptor（校验 role=admin） | `interceptor/AdminInterceptor.java` | M1-T13 | 普通用户访问后台接口返回 1002 | 已完成 | 自动验收通过 |
| M6-T2 | 在 ForumWebMvcConfig 注册 AdminInterceptor | `config/ForumWebMvcConfig.java` | M6-T1 | order=2 拦截 `/api/admin/**` | 已完成 | 自动验收通过 |
| M6-T3 | 实现 AdminAuthController.login（含 role 校验） | `controller/admin/AdminAuthController.java` | M1-T11 | 复用 UserService.login + role=admin 二次校验；非 admin 即踢 token | 已完成 | 自动验收通过 |
| M6-T4 | 实现 AdminUserController.list / detail | `controller/admin/AdminUserController.java` | M1-T15 | 分页 + 关键词模糊搜索（email/nickname） | 已完成 | 自动验收通过 |
| M6-T5 | 实现 UserService.banUser / unbanUser（封禁时踢出 Token） | `service/UserService.java` | M1-T15 | 单测覆盖：禁止封禁自己/禁止封禁管理员/封禁同时清 Redis Token | 已完成 | 自动验收通过 |
| M6-T6 | 实现 AdminUserController.ban / unban | `controller/admin/AdminUserController.java` | M6-T5 | POST /api/admin/users/{id}/ban + /unban | 已完成 | 自动验收通过 |
| M6-T7 | 实现 AdminUserController.resetPassword（重置后老 Token 立即失效） | `controller/admin/AdminUserController.java` | M1-T15 | 单测覆盖：新密码可登录 + Redis Token 立即清除（C5） | 已完成 | 自动验收通过 |
| M6-T7b | 后台写操作统一审计日志（仅 Spring 日志） | `controller/admin/*` 全部写接口 | M6-T3 | `[ADMIN] op=XXX by=opId target=tgtId` 日志行（C4） | 已完成 | 自动验收通过 |
| M6-T8 | 实现 AdminBoardController（CRUD + status） | `controller/admin/AdminBoardController.java` + `service/BoardService.java` | M2-T3 | 含名称唯一性 + 启用/禁用切换 | 已完成 | 自动验收通过 |
| M6-T9 | 实现 PostService.adminListPosts / restore / pin | `service/PostService.java` + `mapper/PostMapper.java` | M3-T11 | 单测覆盖 includeDeleted / 恢复 / 置顶切换 | 已完成 | 自动验收通过 |
| M6-T10 | 实现 AdminPostController | `controller/admin/AdminPostController.java` | M6-T9 | list/delete/restore/pin | 已完成 | 自动验收通过 |
| M6-T11 | 实现 CommentService.adminList / restore | `service/CommentService.java` + `mapper/CommentMapper.java` | M4-T7 | 含已删评论 + 恢复 | 已完成 | 自动验收通过 |
| M6-T12 | 实现 AdminCommentController | `controller/admin/AdminCommentController.java` | M6-T11 | list/delete/restore | 已完成 | 自动验收通过 |
| M6-T13 | 前端 api/admin.js（统一后台接口） | `api/admin.js` | M0-T17 | 16 个接口封装 | 已完成 | 自动验收通过 |
| M6-T14 | 实现 AdminLogin.vue | `views/admin/AdminLogin.vue` | M6-T13 | 全屏渐变背景 + 表单 + 跳转 /admin | 已完成 | 自动验收通过 |
| M6-T15 | 实现 AdminLayout.vue（侧边菜单 + 顶栏） | `views/admin/AdminLayout.vue` | M6-T14 | 5 项菜单 + 返回前台 + 退出登录 | 已完成 | 自动验收通过 |
| M6-T16 | 实现 AdminUsers.vue | `views/admin/AdminUsers.vue` | M6-T15 | 列表/封禁(原因)/解封/重置密码(校验) | 已完成 | 自动验收通过 |
| M6-T17 | 实现 AdminBoards.vue | `views/admin/AdminBoards.vue` | M6-T15 | 列表 + 弹窗创建/编辑 + 启停 | 已完成 | 自动验收通过 |
| M6-T18 | 实现 AdminPosts.vue | `views/admin/AdminPosts.vue` | M6-T15 | 版块/标题过滤 + 删除/恢复/置顶 | 已完成 | 自动验收通过 |
| M6-T19 | 实现 AdminComments.vue | `views/admin/AdminComments.vue` | M6-T15 | 按帖子过滤 + 删除/恢复 | 已完成 | 自动验收通过 |
| M6-T20 | 前端后台路由守卫（管理员校验） | `router/index.js` + `App.vue` | M6-T13 | adminOnly 守卫 + adminLayout 切布局 + Dashboard 总览页 | 已完成 | 自动验收通过 |

---

## M7 系统收尾模块

> 对应 PRD §10、plan.md Phase 8。404、错误处理、限流、防抖。

### 1. 模块目标
- 提供友好的 404 / 5xx 页面
- 后端关键接口加 Redis 限流（发帖 10s / 评论 5s / 搜索）
- 前端关键按钮加防抖
- 全局错误提示与网络异常处理

### 2. 用户可见内容
- **404 页**：友好提示 + 返回首页按钮
- **500 错误页**：友好提示 + 重试按钮
- **限流提示**：当触发限流，前端 toast 提示"操作过于频繁，请稍后再试"
- **网络异常提示**：断网时 toast 提示

### 3. 用户操作流程
- 访问不存在路径 → 跳 404 页
- 接口超时 / 500 → 友好提示
- 触发限流（10s 内连续发帖） → 提示稍后再试
- 网络中断时提交表单 → 提示网络异常 + 保留输入

### 4. 前端页面任务
- `NotFound.vue`：404 页
- 全局错误 toast / 网络异常拦截（在 request.js 中）
- 提交按钮 loading + 防抖

### 5. Controller 任务
- 无新接口

### 6. Service 任务
- 限流服务 `RateLimitService`（基于 Redis）

### 7. Mapper 任务
- 无

### 8. Repository / 数据保存任务
- Redis：以 `rate:post:{userId}` / `rate:comment:{userId}` / `rate:search:{userId|ip}` 为 key 记录最近一次操作时间

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 访问 `/no-such-page` | 跳 404 友好页 |
| 2 | 故意停掉后端再访问任意页面 | 提示"网络异常"或 5xx 友好页 |
| 3 | 10s 内连续两次发帖 | 第二次按钮 loading + 提示限流 |
| 4 | 5s 内连续两次评论 | 第二次拒绝并提示 |

### 10. 接口测试方法

| 步骤 | curl | 预期 |
|------|------|------|
| 1 | 同一用户 10s 内 `POST /api/posts` 两次 | 第二次返回限流错误 |
| 2 | 同一用户 5s 内 `POST /api/posts/.../comments` 两次 | 第二次返回限流错误 |
| 3 | 同一 IP 高频 `GET /api/search` | 返回限流错误 |

### 11. 异常情况测试

| 场景 | 预期 |
|------|------|
| 限流 key 异常（Redis 不可用） | 降级放行，不影响正常使用（**待确认**降级策略） |
| 防抖期间快速多点 | 仅触发一次请求 |
| 网络中断后恢复 | 自动重试 / 用户手动重试 |

### 12. 当前状态
- **任务状态**: 未开始
- **验收状态**: 待验收

---

### 原子任务列表（M7）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|----|------|---------|---------|---------|------|------|
| M7-T1 | 实现 RateLimitService（Redis 滑窗 + 降级放行） | `service/RateLimitService.java` | M0-T11 | 4/4 测试通过；Redis 异常 log.warn 后按 fallback=allow 放行（C6） | 已完成 | 自动验收通过 |
| M7-T2 | 在 PostController.create 接入限流 | `controller/PostController.java` | M7-T1 | 10s 内第二次提交返回限流 | 已完成 | 自动验收通过 |
| M7-T3 | 在 CommentController.create 接入限流 | `controller/CommentController.java` | M7-T1 | 5s 内第二次返回限流 | 已完成 | 自动验收通过 |
| M7-T4 | 在 SearchController 接入限流 | `controller/SearchController.java` | M7-T1 | 登录用户按 uid、匿名按 IP，30 次/分钟 | 已完成 | 自动验收通过 |
| M7-T5 | 实现 NotFound.vue | `views/NotFound.vue` | M0-T15 | el-result 404 + 返回首页/返回上一页 | 已完成 | 自动验收通过 |
| M7-T6 | 在 router 增加 404 兜底路由 | `router/index.js` | M7-T5 | 替换占位为真实 NotFound | 已完成 | 自动验收通过 |
| M7-T7 | request.js 增加全局错误提示与网络异常处理 | `api/request.js` | M0-T17 | 1001 自动跳登录页（前/后台分流）+ 网络/超时/5xx 全局 toast | 已完成 | 自动验收通过 |
| M7-T8 | 提交按钮统一 loading + 防抖封装 | `composables/useSubmit.js` | M0-T14 | hook 在执行期 ignore 重复触发 | 已完成 | 自动验收通过 |
| M7-T9 | 在点赞/收藏/发帖/发评论按钮统一接入防抖 | `components/post/*` + `components/comment/*` | M7-T8 | 现有按钮已用 :loading 实现天然防抖（点击期间按钮禁用） | 已完成 | 自动验收通过 |

---

## 附录 A：跨模块依赖关系

```
M0 (基础) ─┬─> M1 (用户) ─┬─> M2 (版块) ─> M3 (帖子) ─┬─> M4 (互动) ─> M7 (收尾)
           │              │                          │
           │              └─> M6 (后台) <───────────┘
           │                                          │
           └─────────────────────────> M5 (搜索) ────┘
```

- **M0 必须最先完成**，是其他所有模块的前置
- **M1 必须先于 M3、M6**（接口鉴权依赖 Auth）
- **M2 先于 M3**（发帖需选版块）
- **M3 先于 M4、M5**（互动和搜索都基于帖子）
- **M6 依赖 M1~M4 所有服务**（后台复用业务逻辑）
- **M7 在所有功能模块完成后进行**

---

## 附录 B：已确认决策记录（2026-05-23）

> 以下事项已与产品/技术负责人确认，开发时按此决策执行。如需变更须先更新本表。

| ID | 主题 | 已确认决策 | 落地要点 | 影响模块 |
|----|------|----------|---------|---------|
| C1 | 图片存储方案 | **本地文件系统 + 预留 OSS 接口** | 1) 定义 `FileStorage` 接口；2) 一期实现 `LocalFileStorage`；3) 二期可加 `OssFileStorage` 不改业务代码 | M0, M3 |
| C2 | application.yml 默认值 | **端口 8080 + JWT 7 天** | 上传路径 dev=`./uploads`，prod=`/data/forum-uploads`；JWT secret 首次部署随机生成 64 位串固化 | M0 |
| C3 | Node.js 版本 | **Node 18 LTS** | `package.json` 增加 `"engines": { "node": ">=18 <19" }` 防止误用 | M0 |
| C4 | 后台审计日志 | **不加表，仅用 Spring 日志** | 在 `controller/admin/*` 所有写操作入口统一 `log.info("[ADMIN] op={} by={} target={}", ...)` | M6 |
| C5 | 管理员重置密码后老 Token | **立即失效** | 在 `UserService.resetPassword` / `banUser` 中删除 Redis 中该用户所有 Token（按 `token:user:{userId}:*` 通配） | M6 |
| C6 | Redis 不可用限流降级 | **降级放行 + warn 日志** | `RateLimitService` 捕获 Redis 异常时 `log.warn` 并返回 true（放行），不抛业务异常 | M7 |
| C7 | 邮件服务器 | **QQ 邮箱 SMTP** | `smtp.qq.com:465`，使用授权码登录；配置写入 `application-dev.yml`，授权码不入仓（用环境变量或本地未提交副本） | M1 |
| C8 | 管理员账号初始化 | **种子 SQL 插入默认管理员** | `db/seed.sql` 中 INSERT `admin@forum.com` + 预生成 bcrypt 密码串；README 提示首次登录后必须改密 | M0, M6 |
| C9 | JDK 版本 | **JDK 17**（原定 1.8，2026-05-23 放宽） | pom.xml `<java.version>17</java.version>`；CLAUDE.md 知识库"项目信息"段技术栈应同步改为 Java 17；本地 JAVA_HOME 指向 JDK 17 | M0 全部 |

---

*文档结束 — 按 M0 → M7 顺序推进，单任务推进规则严格执行*
