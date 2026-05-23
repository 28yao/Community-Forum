# 社区论坛系统 — 二期任务清单 (todo.md)

> **版本**：v2.0
> **基线**：一期 v1.0 已完成（todo.md 中 M0~M7）
> **文档依赖**：`prd.md`、`plan.md` 必读
> **本文档定位**：二期 8 个模块（P2-M1 ~ P2-M8）的原子任务拆分。沿用一期"模块目标→用户可见→操作流程→前端→Controller→Service→Mapper→Repository→测试→状态"模板。

---

## 状态规则速查

**任务状态**：未开始 / 进行中 / 已完成 / 阻塞 / 待确认
**验收状态**：待验收 / 自动验收通过 / 人工验收通过 / 验收驳回 / 重新提交 / 无需验收

---

## 模块总览

| 模块 | 主题 | 状态 | 依赖 |
|---|---|---|---|
| P2-M1 | 板块字段扩展 + 数据迁移 | 未开始 | 一期已完成 |
| P2-M2 | 板块申请审核 | 未开始 | P2-M1 |
| P2-M3 | 板块关注 | 未开始 | P2-M1 |
| P2-M4 | 贴吧风 UI 重构 | 未开始 | P2-M1 |
| P2-M5 | 混合信息流 feed 接口 | 未开始 | P2-M3, P2-M4 |
| P2-M6 | 发帖弹窗与表情面板 | 未开始 | P2-M4 |
| P2-M7 | 搜索增强 | 未开始 | P2-M4 |
| P2-M8 | 板块管理增强（吧主权限） | 未开始 | P2-M1 |

---

## P2-M1 板块字段扩展 + 数据迁移

> 对应 prd.md §5.1、plan.md §2。为一期 board 表追加 icon / slogan / tags / owner_user_id / follower_count 字段。

### 1. 模块目标
- 在不动一期数据的前提下，扩展 board 表以支持二期"吧主、关注数、UI 元素"需求
- 一期 3 个种子版块自动归属系统管理员（user_id=1）作为吧主

### 2. 用户可见内容
- 本模块用户无直接可见内容（基础设施层）
- 完成后，一期版块详情接口返回字段会多出 icon / slogan / tags / owner_user_id / follower_count（默认值/null）

### 3. 用户操作流程
- 无用户流程

### 4. 前端页面任务
- 修改 `entity Board` 对应的 TypeScript / JS 类型定义（如有）
- `api/board.js` 中 board 数据结构注释更新

### 5. Controller 任务
- 无（不新增接口，扩展字段透传）

### 6. Service 任务
- `BoardService` 修改：getById 返回 DTO 包含新字段
- 新增 `BoardService.update(boardId, dto)` 方法（P2-M8 会用，本模块只创建签名 + 测试桩）

### 7. Mapper 任务
- `BoardMapper.xml` / `Board.java` 实体类增加新字段映射

### 8. Repository / 数据保存任务
- `schema.v2.sql` 完整 DDL：`ALTER TABLE board ADD COLUMN ...`（5 列 + 2 索引）

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|---|---|---|
| 1 | 部署 schema.v2.sql 后访问一期版块详情页 | 页面正常打开，无报错 |
| 2 | F12 看接口返回 | board 对象多出 5 个新字段 |
| 3 | 一期版块列表页 | 数据正常显示，UI 不变 |

### 10. 接口测试方法

| 步骤 | curl | 预期 |
|---|---|---|
| 1 | `GET /api/boards/1` | code=0，data 含 icon=null, slogan=null, tags=null, owner_user_id=1, follower_count=0 |
| 2 | `GET /api/boards` | 数组中每个 board 都含新字段 |

### 11. 异常情况测试

| 场景 | 预期 |
|---|---|
| schema.v2.sql 执行失败（如已存在字段） | 用 `IF NOT EXISTS` 或重复执行报错时退出，不破坏现有数据 |
| 一期 user_id=1 不是管理员 | 部署前手动 UPDATE board SET owner_user_id={admin_id} |

### 12. 当前状态
- **任务状态**：已完成
- **验收状态**：自动验收通过
- **完成日期**：2026-05-23
- **数据库**：board 表已加 5 字段 + 2 索引；board_application / user_board_follow 已建；post.idx_title (ngram) 已建
- **回归测试**：109/109 一期测试全部通过（含 BoardServiceTest 5/5）

---

### 原子任务列表（P2-M1）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|---|---|---|---|---|---|---|
| P2-M1-T1 | 编写 schema.v2.sql 的 ALTER board 段 | `forum-server/src/main/resources/db/schema.v2.sql` | 一期完成 | 文件可执行，5 列 + 2 索引落上 | 已完成 | 自动验收通过 |
| P2-M1-T2 | 修改 Board.java 实体类加新字段 | `entity/Board.java` | T1 | MyBatis-Plus 注解齐全，所有字段映射 | 已完成 | 自动验收通过 |
| P2-M1-T3 | BoardMapper 加 incrFollowerCount/decrFollowerCount | `mapper/BoardMapper.java` | T2 | 原子 UPDATE，decr 加 GREATEST 防御 | 已完成 | 自动验收通过 |
| P2-M1-T4 | BoardService.getById 字段透传（无需改） | `service/BoardService.java` | T3 | MyBatis-Plus selectById 自动透传新字段 | 已完成 | 无需验收 |
| P2-M1-T5 | 部署后跑上线检查 SQL + 一期回归测试 | 数据库 + mvn test | T1~T4 | 109/109 通过；SHOW CREATE TABLE 验证 | 已完成 | 自动验收通过 |

---

## P2-M2 板块申请审核

> 对应 prd.md §5.2、plan.md §3.2.1~3.2.3。覆盖用户申请 + 管理员审核完整链路。

### 1. 模块目标
- 满足资格的用户可提交板块申请
- 管理员可在后台审核（通过/驳回）
- 通过后自动创建板块、申请人成为吧主、自动关注

### 2. 用户可见内容
- **个人中心 "我的板块申请" Tab**：申请记录列表（含状态、驳回原因）+ "申请新板块"按钮
- **申请弹窗 / 页面**：
  - 资格不足时显示提示（差几天 / 差几篇帖）
  - 资格通过显示表单（名称 / 描述 / 头像 / 口号 / 标签）
  - 提交按钮 + 取消按钮
- **后台 "板块申请" 菜单**：
  - 待审核列表（按申请时间正序）
  - 审核详情面板（申请人信息 + 申请字段 + 通过/驳回按钮，驳回需填原因）

### 3. 用户操作流程
1. 用户进入个人中心 → 点 "我的板块申请" Tab → 看到历史申请
2. 点 "申请新板块" → 接口检查资格
3. 资格不足 → 弹窗提示 → 关闭
4. 资格通过 → 显示表单 → 填写并提交 → toast "申请已提交，等待审核" → 列表新增一条 "待审核"
5. 管理员登录后台 → 点 "板块申请" 菜单 → 看到待审核列表 → 点某条 → 看详情 → 点通过 → 板块创建成功 / 点驳回 → 填原因 → 用户可在 "我的板块申请" 看到 "已驳回 (原因)"
6. 用户改名后重新提交 → 生成新记录（旧驳回记录保留）

### 4. 前端页面任务
- `views/user/MyApplications.vue`：我的申请列表（个人中心 Tab）
- `components/board-application/ApplicationForm.vue`：申请表单
- `components/board-application/ApplicationList.vue`：申请记录卡片列表
- `components/board-application/EligibilityHint.vue`：资格不足提示
- `views/admin/BoardApplicationReview.vue`：管理员审核台
- `api/boardApplication.js`：5 个接口封装

### 5. Controller 任务
- `BoardApplicationController` (用户端)：
  - GET /api/board-applications/eligibility
  - POST /api/board-applications
  - GET /api/board-applications/mine
- `admin/AdminBoardApplicationController` (管理端)：
  - GET /api/admin/board-applications?status=&page=
  - POST /api/admin/board-applications/{id}/approve
  - POST /api/admin/board-applications/{id}/reject

### 6. Service 任务
- `BoardApplicationService`：
  - checkEligibility(userId) → EligibilityVO
  - submit(userId, dto) → applicationId
  - listMine(userId, page) → 分页
  - listForReview(status, page) → 分页（管理端）
  - approve(adminId, applicationId) → boardId（含事务）
  - reject(adminId, applicationId, reason)

### 7. Mapper 任务
- `BoardApplicationMapper`：
  - 插入、按用户查询、按状态查询、SELECT FOR UPDATE 单条
- `UserBoardFollowMapper`：approve 时插入吧主关注关系（与 P2-M3 共用）

### 8. Repository / 数据保存任务
- `board_application` 表（见 schema.v2.sql）
- 同步使用 `board` 表（approve 时 INSERT）

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|---|---|---|
| 1 | 注册新用户（< 3 天，0 帖）→ 点申请 | 提示"注册不足 3 天，已发帖 0/3" |
| 2 | 满足资格的用户 → 点申请 → 填表 → 提交 | toast 成功 + 列表新增"待审核" |
| 3 | 同一用户立即再点申请 | 提示"已有待审核申请" |
| 4 | 管理员登录后台 → 板块申请菜单 | 看到该申请 |
| 5 | 管理员通过 | 列表中状态变"已通过" + 一期板块列表中出现新板块 |
| 6 | 申请人查看 "我的板块申请" | 状态显示 "已通过" + 含跳转到新板块的链接 |
| 7 | 管理员驳回 + 填原因 | 申请人看到 "已驳回 (原因)" |
| 8 | 申请人改名重提 | 生成新记录，原驳回记录保留 |

### 10. 接口测试方法

| 步骤 | curl | 预期 |
|---|---|---|
| 1 | `GET /api/board-applications/eligibility` (新用户) | code=0, data={ eligible: false, registeredDays: 0, postCount: 0, hasPending: false } |
| 2 | `POST /api/board-applications` 不满足资格 | code=403 |
| 3 | `POST /api/board-applications` 名称冲突 | code=409 |
| 4 | `POST /api/board-applications` 正常 | code=0, 返回 application.id |
| 5 | `GET /api/board-applications/mine` | code=0, 分页返回 |
| 6 | `GET /api/admin/board-applications?status=1` (非管理员) | code=1002 |
| 7 | `POST /api/admin/board-applications/{id}/approve` | code=0, 数据库 board 表新增一行 + board_application.status=2 |
| 8 | `POST /api/admin/board-applications/{id}/reject` 无 reason | code=2001 |
| 9 | `POST /api/admin/board-applications/{id}/reject` 正常 | code=0 |

### 11. 异常情况测试

| 场景 | 预期 |
|---|---|
| 名称含特殊字符 / 长度越界 | code=2001 校验失败 |
| 同时两个待审核同名（A 已通过后 B 再通过） | B 的 approve 二次校验名称冲突，抛 409 提示管理员驳回 |
| 60s 内同一用户重复提交申请 | 限流（plan.md §3.4） |
| 非管理员调用 /api/admin/** | AdminInterceptor 401 |
| 审核一个已审核过的申请 | code=409 "申请状态已变化" |

### 12. 当前状态
- **任务状态**：未开始
- **验收状态**：待验收

---

### 原子任务列表（P2-M2）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|---|---|---|---|---|---|---|
| P2-M2-T1 | schema.v2.sql 创建 board_application 表 | `db/schema.v2.sql` | P2-M1-T1 | 表结构与字段类型符合 plan §2.1 | 未开始 | 待验收 |
| P2-M2-T2 | BoardApplication 实体 + Mapper | `entity/BoardApplication.java`, `mapper/BoardApplicationMapper.java` | T1 | 基本 CRUD 注解 + 自定义查询签名 | 未开始 | 待验收 |
| P2-M2-T3 | DTO：SubmitRequest / ReviewRequest / EligibilityVO | `dto/*.java` | T2 | @Valid 注解齐全（5-20/10-100/30/60 长度限制） | 未开始 | 待验收 |
| P2-M2-T4 | BoardApplicationService.checkEligibility | `service/BoardApplicationService.java` | T2 | 单测 4/4（足/差天/差帖/有 pending） | 未开始 | 待验收 |
| P2-M2-T5 | BoardApplicationService.submit | `service/BoardApplicationService.java` | T4 | 单测 4/4（成功/资格不足/名称冲突/重复 pending） | 未开始 | 待验收 |
| P2-M2-T6 | BoardApplicationService.listMine | `service/BoardApplicationService.java` | T2 | 分页查询单测 | 未开始 | 待验收 |
| P2-M2-T7 | BoardApplicationController (用户端 3 接口) | `controller/BoardApplicationController.java` | T4~T6 | 接口测试 §10 第 1~5 项通过 | 未开始 | 待验收 |
| P2-M2-T8 | BoardApplicationService.approve（事务） | `service/BoardApplicationService.java` | T2, P2-M1-T4 | 单测：成功路径 + 二次名称冲突 + 已审核重复 | 未开始 | 待验收 |
| P2-M2-T9 | BoardApplicationService.reject | `service/BoardApplicationService.java` | T2 | 单测：成功 + 已审核重复 + 空 reason | 未开始 | 待验收 |
| P2-M2-T10 | BoardApplicationService.listForReview | `service/BoardApplicationService.java` | T2 | 分页 + status 过滤单测 | 未开始 | 待验收 |
| P2-M2-T11 | AdminBoardApplicationController (3 接口) | `controller/admin/AdminBoardApplicationController.java` | T8~T10 | 接口测试 §10 第 6~9 项通过 + AdminInterceptor 拦截 | 未开始 | 待验收 |
| P2-M2-T12 | RateLimit：board-applications 60s/用户 | 限流配置 / Controller 注解 | T7, 一期 RateLimitService | 60s 内第二次提交返回限流 | 未开始 | 待验收 |
| P2-M2-T13 | 前端 api/boardApplication.js | `api/boardApplication.js` | T7, T11 | 5 个函数：eligibility/submit/listMine/listForReview/approve/reject | 未开始 | 待验收 |
| P2-M2-T14 | ApplicationForm.vue 表单组件 | `components/board-application/ApplicationForm.vue` | T13 | 字段校验 + 提交后 toast | 未开始 | 待验收 |
| P2-M2-T15 | EligibilityHint.vue 资格提示 | `components/board-application/EligibilityHint.vue` | T13 | 显示注册天数/发帖数/差额 | 未开始 | 待验收 |
| P2-M2-T16 | ApplicationList.vue 我的申请列表 | `components/board-application/ApplicationList.vue` | T13 | 卡片含状态 + 驳回原因 + 跳板块链接（通过时） | 未开始 | 待验收 |
| P2-M2-T17 | MyApplications.vue 个人中心 Tab | `views/user/MyApplications.vue` | T14~T16 | Tab 集成 + 申请按钮联动 EligibilityHint 弹窗 | 未开始 | 待验收 |
| P2-M2-T18 | 个人中心路由 + 菜单加 "我的板块申请" | `views/user/UserCenter.vue` 或一期对应文件, `router/index.js` | T17 | 一期个人中心新增 Tab | 未开始 | 待验收 |
| P2-M2-T19 | 管理后台 BoardApplicationReview.vue | `views/admin/BoardApplicationReview.vue` | T13 | 列表 + 详情面板 + 通过/驳回操作 + 驳回弹窗填原因 | 未开始 | 待验收 |
| P2-M2-T20 | 后台菜单加 "板块申请" | 一期 admin 布局/菜单文件 | T19 | 管理员登录后看到新菜单 | 未开始 | 待验收 |
| P2-M2-T21 | 端到端联调（用户提交 → 审核 → 板块创建） | 全链路 | T1~T20 | 见 §9 页面测试全部跑通 | 未开始 | 待验收 |

---

## P2-M3 板块关注

> 对应 prd.md §5.3、plan.md §3.2.4。用户可关注/取关板块，关注列表用于左栏渲染和 feed 信息流。

### 1. 模块目标
- 用户可关注/取关板块（幂等）
- 单用户上限 50 个关注
- 维护 board.follower_count 冗余字段
- 提供"我关注的"、"推荐板块" 两个查询接口供左栏使用

### 2. 用户可见内容
- 板块页右上角"关注 / 已关注"切换按钮（含计数显示）
- 左栏（P2-M4 实现）三段：关注 / 推荐 / 全部
- 个人中心新增 "我关注的板块" Tab（可选，二期不强制）

### 3. 用户操作流程
1. 未登录用户看到 "关注" 按钮 → 点击 → 跳登录页
2. 登录用户点击 "关注" → 按钮立即切换 "已关注"（乐观更新）+ 计数 +1 → 接口成功则保留 / 失败则回滚
3. 再次点击 → 取关 → 计数 -1
4. 关注满 50 个后再点关注 → toast "关注数量已达上限"

### 4. 前端页面任务
- `components/board/BoardFollowButton.vue`：按钮组件（含乐观更新逻辑）
- `api/boardFollow.js`：4 个接口封装
- `stores/boardFollow.js`：缓存当前用户关注的 boardId 集合 + 增量更新

### 5. Controller 任务
- `BoardFollowController` 或挂在 `BoardController` 下：
  - POST /api/boards/{id}/follow
  - DELETE /api/boards/{id}/follow
  - GET /api/boards/followed
  - GET /api/boards/recommended?limit=10

### 6. Service 任务
- `BoardFollowService`：
  - follow(userId, boardId)（事务 + 上限校验）
  - unfollow(userId, boardId)（事务）
  - listFollowed(userId, page) → 分页
  - listRecommended(limit) → 按 follower_count + post_count 排序
  - isFollowed(userId, boardId) → 用于 BoardController.getById 时回填

### 7. Mapper 任务
- `UserBoardFollowMapper`：
  - INSERT IGNORE
  - DELETE
  - SELECT board_id WHERE user_id=?
  - COUNT WHERE user_id=?
- `BoardMapper.incrFollowerCount / decrFollowerCount`：原子计数

### 8. Repository / 数据保存任务
- `user_board_follow` 表（见 schema.v2.sql）
- 更新 `board.follower_count` 冗余字段

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|---|---|---|
| 1 | 未登录访问板块页 → 点关注 | 跳转登录页 |
| 2 | 登录后点关注 | 按钮变 "已关注"，计数 +1 |
| 3 | 刷新页面 | 状态保持 "已关注" |
| 4 | 点 "已关注" → 确认取关 | 按钮变 "关注"，计数 -1 |
| 5 | 关注满 50 个 → 再点关注 | toast "已达上限" |
| 6 | 查 "我关注的"（API） | 返回关注列表 |
| 7 | 查推荐板块 | 按热度返回 Top 10 |

### 10. 接口测试方法

| 步骤 | curl | 预期 |
|---|---|---|
| 1 | `POST /api/boards/1/follow` | code=0；DB 中 user_board_follow 新增一行；board.follower_count +1 |
| 2 | 重复 `POST /api/boards/1/follow` | code=0（幂等）；DB 无重复行；follower_count 不变 |
| 3 | `DELETE /api/boards/1/follow` | code=0；DB 中删除；follower_count -1 |
| 4 | 重复 `DELETE` | code=0（幂等）；follower_count 不变 |
| 5 | 关注 50 板块后 `POST /api/boards/51/follow` | code=4xx "关注数量已达上限" |
| 6 | `GET /api/boards/followed`（未登录） | code=1001 |
| 7 | `GET /api/boards/recommended?limit=5` | code=0，返回 ≤5 个，按 follower_count DESC + post_count DESC |
| 8 | `GET /api/boards/{id}` 含登录态 | 返回字段加 `isFollowed: true/false` |

### 11. 异常情况测试

| 场景 | 预期 |
|---|---|
| 关注不存在的 board_id | code=3003 |
| 关注已禁用的 board（status=0） | 可关注（业务允许，UI 标灰），或 code=403（待确认） |
| 高并发同时点关注 | 数据库 UNIQUE 主键 (user_id, board_id) 保证幂等 |
| follower_count 出现负数 | UPDATE 加 `WHERE follower_count > 0` 防御 |
| 关注 / 取关时事务失败 | 整体回滚，follower_count 不被错改 |

### 12. 当前状态
- **任务状态**：已完成
- **验收状态**：自动验收通过
- **完成日期**：2026-05-23
- **后端测试**：BoardFollowServiceTest 5/5 通过；总回归 114/114 通过
- **前端构建**：vite build 成功
- **拦截器调整**：移除 `/api/boards/**` 整段 exclude，改 AuthInterceptor 内部用 PUBLIC_BOARD_GET 精细控制
- **GET /api/boards/{id} 返回结构**：兼容一期所有字段平铺 + 新增 isFollowed 字段

---

### 原子任务列表（P2-M3）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|---|---|---|---|---|---|---|
| P2-M3-T1 | schema.v2.sql 创建 user_board_follow 表 | `db/schema.v2.sql` | P2-M1-T1 | 复合主键 + idx_board 索引（已随 P2-M1 一并执行） | 已完成 | 自动验收通过 |
| P2-M3-T2 | UserBoardFollow 实体 + Mapper | `entity/UserBoardFollow.java`, `mapper/UserBoardFollowMapper.java` | T1 | INSERT IGNORE + DELETE + SELECT board_ids + COUNT | 已完成 | 自动验收通过 |
| P2-M3-T3 | BoardMapper 加 incrFollowerCount / decrFollowerCount | `mapper/BoardMapper.java` | P2-M1-T3 | 已随 P2-M1 完成 | 已完成 | 自动验收通过 |
| P2-M3-T4 | BoardFollowService.follow（事务 + 上限） | `service/BoardFollowService.java` | T2, T3 | 单测通过 | 已完成 | 自动验收通过 |
| P2-M3-T5 | BoardFollowService.unfollow | `service/BoardFollowService.java` | T2, T3 | 单测通过 | 已完成 | 自动验收通过 |
| P2-M3-T6 | BoardFollowService.listFollowed | `service/BoardFollowService.java` | T2 | 关联 board 表返回完整 board 信息 | 已完成 | 自动验收通过 |
| P2-M3-T7 | BoardFollowService.listRecommended | `service/BoardFollowService.java` | T3 | 按 follower_count + post_count 排序 + LIMIT | 已完成 | 自动验收通过 |
| P2-M3-T8 | BoardController.detail 加 isFollowed 字段 | `controller/BoardController.java`, `service/BoardService.java` | T2 | 返回结构兼容一期（字段平铺） | 已完成 | 自动验收通过 |
| P2-M3-T9 | 实现 4 个 follow 相关接口 | `controller/BoardController.java` | T4~T7 | follow / unfollow / followed / recommended | 已完成 | 自动验收通过 |
| P2-M3-T9b | **拦截器调整**：移除 /api/boards/** exclude，改精细 GET 公开 | `config/ForumWebMvcConfig.java`, `interceptor/AuthInterceptor.java` | — | 一期 114 测试回归通过 | 已完成 | 自动验收通过 |
| P2-M3-T10 | 前端 api/boardFollow.js | `api/boardFollow.js` | T9 | follow/unfollow/listFollowed/listRecommended | 已完成 | 自动验收通过 |
| P2-M3-T11 | stores/boardFollow.js Pinia store | `stores/boardFollow.js` | T10 | followedIds Set + 乐观更新 + 失败回滚 | 已完成 | 自动验收通过 |
| P2-M3-T12 | BoardFollowButton.vue 组件 | `components/board/BoardFollowButton.vue` | T11 | 乐观更新 + 失败回滚 + 未登录跳转 | 已完成 | 自动验收通过 |
| P2-M3-T13 | 在 Board.vue 板块页集成 BoardFollowButton | `views/Board.vue` | T12 | 板块页顶部显示关注按钮和关注/帖子数 | 已完成 | 自动验收通过 |
| P2-M3-T14 | 端到端联调 | 全链路 | T1~T13 | 后端单测 + 前端构建通过 | 已完成 | 自动验收通过 |

---

## P2-M4 贴吧风 UI 重构

> 对应 prd.md §5.4、plan.md §4.1~4.2。重构首页布局、左栏导航、列表卡片、详情页样式。

### 1. 模块目标
- 重写 Home.vue 为贴吧风：左栏（三段板块） + 中间信息流（暂用一期数据，feed 接口在 P2-M5 实现）
- 重构 PostCard.vue：板块名标记 + 标题加粗 + 摘要 + 图片网格（1/2/3）+ 底部统计
- 新增图片网格组件 PostImageGrid.vue
- PostDetail.vue 加面包屑 + 图片大显
- AppHeader.vue 发帖按钮改为打开 Modal（Modal 在 P2-M6 实现，本模块只接桩）

### 2. 用户可见内容
- **首页**：
  - 左栏：三段板块导航（关注/推荐/全部）
  - 中间：贴吧风信息流卡片
  - 右栏：占位空白（暂不实现）
- **板块页**：贴吧风样式
- **帖子详情页**：
  - 顶部面包屑（首页 > 板块名 > 帖子）
  - 图片在主体区域大显（不再右侧小图）

### 3. 用户操作流程
1. 用户访问 `/` → 看到贴吧风首页
2. 左栏点某个板块 → 跳板块页
3. 中间卡片点标题 → 跳帖子详情
4. 卡片点板块名 → 跳板块页
5. 卡片点图片 → 大图预览（Element Plus 已有）
6. 详情页点面包屑板块名 → 跳板块页

### 4. 前端页面任务
- `layouts/TiebaLayout.vue`：贴吧风布局壳
- `components/layout/BoardSidebar.vue`：左栏三段导航
- `components/board/BoardCard.vue`：板块卡片（左栏 + 推荐区使用）
- `components/post/PostCard.vue`（**覆盖一期 M3-T20**）：贴吧风卡片
- `components/post/PostImageGrid.vue`：1/2/3 图自适应网格
- `components/post/PostFeed.vue`：信息流容器（loading / empty / 分页）
- `views/Home.vue`（**覆盖一期 M3-T28**）：重写首页
- `views/Board.vue`（轻改）：套贴吧风样式
- `views/PostDetail.vue`（轻改）：加面包屑 + 图片大显
- `components/layout/AppHeader.vue`（修改一期）：发帖按钮触发 store.openPostEditor()

### 5. Controller 任务
- 无（数据来自一期已有接口 + P2-M3 关注接口）

### 6. Service 任务
- 无

### 7. Mapper 任务
- 无

### 8. Repository / 数据保存任务
- 无

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|---|---|---|
| 1 | 访问首页 | 看到贴吧风布局（左栏 + 中间） |
| 2 | 未登录态：左栏"关注"段 | 显示"登录后可关注感兴趣的板块" |
| 3 | 已登录无关注：左栏"关注"段 | 显示"你还没关注任何板块" |
| 4 | 已关注 ≥1 板块 | 左栏关注段显示板块卡片 |
| 5 | 左栏"推荐板块" | 显示 Top 10 推荐 |
| 6 | 左栏"全部板块" | 滚动可查所有板块 |
| 7 | 中间信息流：1 图帖 | 单张大图展示 |
| 8 | 中间信息流：2 图帖 | 两列 1:1 |
| 9 | 中间信息流：3 图帖 | 三列 1:1:1 |
| 10 | 中间信息流：4+ 图帖 | 显示前 3 张 + 右下角"+N"角标 |
| 11 | 中间卡片点击图片 | 弹大图预览 |
| 12 | 卡片点击板块名 | 跳 /board/{id} |
| 13 | 帖子详情页顶部 | 显示面包屑 "首页 > 板块名 > 帖子标题" |
| 14 | 帖子详情图片 | 主体大显，宽度自适应 |
| 15 | 顶部"发帖"按钮点击 | 触发 Modal 打开（依赖 P2-M6） |

### 10. 接口测试方法

本模块无新接口，依赖一期 + P2-M3 接口。

### 11. 异常情况测试

| 场景 | 预期 |
|---|---|
| 帖子无图 | 卡片不显示图片网格 |
| 板块列表为空（极端情况） | 左栏显示"暂无板块" |
| 推荐接口异常 | 左栏推荐段显示错误占位 + 可重试 |
| 摘要文本含 HTML | 服务端已 striptags，前端再做一次防御 |

### 12. 当前状态
- **任务状态**：未开始
- **验收状态**：待验收

---

### 原子任务列表（P2-M4）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|---|---|---|---|---|---|---|
| P2-M4-T1 | TiebaLayout.vue 布局壳 | `layouts/TiebaLayout.vue` | 一期已有 | 三栏 Grid + 顶部 Header | 未开始 | 待验收 |
| P2-M4-T2 | BoardCard.vue 板块卡片 | `components/board/BoardCard.vue` | P2-M1-T4 | 头像 + 名 + 关注数 + 帖子数 + 口号 | 未开始 | 待验收 |
| P2-M4-T3 | BoardSidebar.vue 左栏三段 | `components/layout/BoardSidebar.vue` | T2, P2-M3-T10 | 三段折叠 + 加载状态 + 空状态 | 未开始 | 待验收 |
| P2-M4-T4 | PostImageGrid.vue 图片网格 | `components/post/PostImageGrid.vue` | 无 | 1/2/3 三种布局 CSS + 超出角标 + lazy load | 未开始 | 待验收 |
| P2-M4-T5 | PostCard.vue 重构（**覆盖 M3-T20**） | `components/post/PostCard.vue` | T4 | 贴吧风样式 + 摘要 120 字 + 图片网格 | 未开始 | 待验收 |
| P2-M4-T6 | PostFeed.vue 信息流容器 | `components/post/PostFeed.vue` | T5 | 分页/loading/空状态 | 未开始 | 待验收 |
| P2-M4-T7 | Home.vue 重写（**覆盖 M3-T28**） | `views/Home.vue` | T1, T3, T6 | 贴吧风首页（feed 数据暂用一期 /api/posts） | 未开始 | 待验收 |
| P2-M4-T8 | Board.vue 套贴吧风样式 | `views/Board.vue` | T1, T5 | 板块头 + 列表用新 PostCard | 未开始 | 待验收 |
| P2-M4-T9 | PostDetail.vue 加面包屑 | `views/PostDetail.vue` | 一期已有 | 顶部面包屑组件 + 链接正确 | 未开始 | 待验收 |
| P2-M4-T10 | PostDetail.vue 图片样式调整 | `views/PostDetail.vue` | 一期已有 | 图片宽度 100%, max-width 800px, 点击大图 | 未开始 | 待验收 |
| P2-M4-T11 | AppHeader.vue 发帖按钮改 Modal | `components/layout/AppHeader.vue` | P2-M6-T1（store） | 点击触发 store.openPostEditor() | 未开始 | 待验收 |
| P2-M4-T12 | 路由替换默认布局为 TiebaLayout | `router/index.js` | T1, T7 | 一期所有页面套新布局 | 未开始 | 待验收 |
| P2-M4-T13 | 端到端 UI 验收 | 全链路 | T1~T12 | §9 页面测试全部通过 + 视觉对照参考图 | 未开始 | 待验收 |

---

## P2-M5 混合信息流 feed 接口

> 对应 prd.md §5.5、plan.md §3.2.5~3.2.6。新增 /api/posts/feed，不替换一期 /api/posts。

### 1. 模块目标
- 提供 GET /api/posts/feed 接口
- 未登录态：返回全站热门 Top N
- 登录态且有关注：70% 关注板块最新 + 30% 全站热门
- 一期 /api/posts 不动，保持向后兼容

### 2. 用户可见内容
- 首页中间区域加载新接口的数据（接口默认走 feed，仅在 P2-M4 信息流中体现）

### 3. 用户操作流程
- 见 P2-M4 §3

### 4. 前端页面任务
- `api/feed.js`：getFeed(page, size)
- 修改 Home.vue 改调 feed 接口

### 5. Controller 任务
- `FeedController` 或挂在 PostController：
  - GET /api/posts/feed?page=&size=

### 6. Service 任务
- `FeedService`：
  - getFeed(userId, page, size) → 列表 + 总数

### 7. Mapper 任务
- `PostMapper`：
  - selectHotPosts(page, size, excludeIds) — 用 hot_score 排序
  - selectByBoardIdsOrderByCreatedAt(boardIds, page, size)
- `UserBoardFollowMapper`：复用 P2-M3-T2

### 8. Repository / 数据保存任务
- 无新表

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|---|---|---|
| 1 | 未登录访问首页 | 信息流显示全站热门 |
| 2 | 登录但未关注任何板块 | 同未登录态 |
| 3 | 关注 1 个板块 → 刷新 | 70% 来自该板块最新（按时间倒序）+ 30% 全站热门 |
| 4 | 关注的板块帖子很少 | 不足部分用热门补 |
| 5 | 滚动到底 → 加载下一页 | 分页正常 |

### 10. 接口测试方法

| 步骤 | curl | 预期 |
|---|---|---|
| 1 | `GET /api/posts/feed?page=1&size=20` (匿名) | code=0, list 长度 ≤20，按 hot_score DESC |
| 2 | `GET /api/posts/feed?page=1&size=20` (登录无关注) | 同上 |
| 3 | `GET /api/posts/feed?page=1&size=20` (登录关注 ≥1) | list 含关注板块帖（≤70%）+ 热门（≥30%） |
| 4 | 不同 page 翻页 | total 一致，list 无重复 |

### 11. 异常情况测试

| 场景 | 预期 |
|---|---|
| 关注的板块全部被禁用 | 退化为全站热门 |
| 数据库无任何帖子 | list=[]，total=0 |
| size 过大（>100） | 应用层 clamp 到 100 |

### 12. 当前状态
- **任务状态**：未开始
- **验收状态**：待验收

---

### 原子任务列表（P2-M5）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|---|---|---|---|---|---|---|
| P2-M5-T1 | PostMapper.selectHotPosts (含 hot_score SQL) | `mapper/PostMapper.java` 或 xml | 一期 | 单测：相同数据返回顺序符合公式 | 未开始 | 待验收 |
| P2-M5-T2 | PostMapper.selectByBoardIdsOrderByCreatedAt | `mapper/PostMapper.java` | 一期 | 单测：多板块 IN 查询 + 分页 + 排序 | 未开始 | 待验收 |
| P2-M5-T3 | FeedService.getFeed（含 70/30 合并） | `service/FeedService.java` | T1, T2, P2-M3-T2 | 单测 4/4（匿名/无关注/有关注/不足补热门） | 未开始 | 待验收 |
| P2-M5-T4 | FeedController GET /api/posts/feed | `controller/PostController.java` 加方法或新建 | T3 | 接口测试 §10 全部通过 | 未开始 | 待验收 |
| P2-M5-T5 | size clamp ≤100 | Controller 参数校验 | T4 | size=200 时实际返回 ≤100 | 未开始 | 待验收 |
| P2-M5-T6 | 前端 api/feed.js | `api/feed.js` | T4 | getFeed(page, size) | 未开始 | 待验收 |
| P2-M5-T7 | Home.vue 改调 /feed | `views/Home.vue` | T6, P2-M4-T7 | 首页中间区显示混合流 | 未开始 | 待验收 |
| P2-M5-T8 | 端到端联调 | 全链路 | T1~T7 | §9 页面测试全部通过 | 未开始 | 待验收 |

---

## P2-M6 发帖弹窗与表情面板

> 对应 prd.md §5.6、plan.md §4.2.1。Modal 形式的发帖入口，沿用一期 PostEditor.vue（WangEditor 封装）+ ImageUpload.vue。

### 1. 模块目标
- 实现 PostEditorModal.vue 全局弹窗
- 支持顶部按钮触发（板块下拉空）和板块页按钮触发（板块下拉锁定）
- 集成表情面板 EmojiPicker
- 一期 /post/create 路由保留作为后备路径

### 2. 用户可见内容
- 任意页面顶部"发帖"按钮 → 弹出 Modal
- 板块页"发帖"按钮 → 弹出 Modal（板块锁定）
- Modal 结构（参考 image-2.png）：
  - 顶部标题"发贴" + 当前用户头像 + 关闭按钮
  - 「发布到吧」下拉
  - 标题输入框（5-31 字）
  - 正文编辑器（WangEditor）
  - 工具栏：表情 / 图片
  - 右下角"发布"按钮

### 3. 用户操作流程
1. 用户点顶部"发帖" → Modal 弹出 → 选板块 → 填标题正文 → 上传图片 → 插入表情 → 点发布 → 成功 → Modal 关闭 → 跳新帖详情
2. 用户在板块页点"发帖" → Modal 弹出 → 板块下拉锁定为当前板块 → 后续同上
3. 用户填了内容点关闭 → 二次确认弹窗"内容未保存确认关闭？"
4. 未登录用户点"发帖" → 跳登录页

### 4. 前端页面任务
- `components/post-editor/PostEditorModal.vue`：主弹窗
- `components/post-editor/EmojiPicker.vue`：表情面板（包装 vue3-emoji-picker）
- `components/board/BoardSelectDropdown.vue`：板块下拉选择器（带搜索）
- `stores/postEditor.js`：Pinia store 控制 visible + lockedBoardId
- 修改 `App.vue`：全局挂载 PostEditorModal
- 修改 `AppHeader.vue`（P2-M4-T11 已接桩）：点击调 store.open()
- 修改 `Board.vue`：发帖按钮调 store.open({ boardId })

### 5. Controller 任务
- 无新接口（沿用一期 POST /api/posts）

### 6. Service 任务
- 无（沿用一期 PostService.createPost）

### 7. Mapper 任务
- 无

### 8. Repository / 数据保存任务
- 无

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|---|---|---|
| 1 | 顶部"发帖"按钮（未登录） | 跳转登录页 |
| 2 | 顶部"发帖"按钮（登录） | Modal 弹出，板块下拉空 |
| 3 | 板块下拉点击 | 显示板块列表，可搜索 |
| 4 | 板块页"发帖"按钮 | Modal 弹出，下拉锁定为当前板块（灰色不可改） |
| 5 | 标题输入 <5 字 | 提交时校验失败 |
| 6 | 标题输入 >31 字 | 输入框限制 |
| 7 | 工具栏点表情图标 | 表情面板弹出，点表情插入到正文 |
| 8 | 工具栏点图片 | 触发图片上传（沿用一期 ImageUpload） |
| 9 | 填了内容点关闭 | 二次确认弹窗 |
| 10 | 提交成功 | Modal 关闭，跳转新帖详情 |
| 11 | 提交失败（如网络） | Modal 不关，按钮恢复，toast 错误 |
| 12 | 一期 `/post/create` 直接访问 | 仍可用（后备） |

### 10. 接口测试方法

无新接口，复用一期 POST /api/posts。

### 11. 异常情况测试

| 场景 | 预期 |
|---|---|
| 选择已禁用板块（status=0）发帖 | 一期已校验 code=3003，弹窗显示错误 |
| WangEditor 在 Modal 反复打开/关闭 | 调用 editor.destroy() + v-if 保证无内存泄漏 |
| 板块下拉数据加载失败 | 显示重试按钮 |
| 表情面板加载失败（依赖未装） | 工具栏表情按钮置灰，不影响其他功能 |
| 一期发帖限流 10s | 触发后 Modal 显示"操作过于频繁" |

### 12. 当前状态
- **任务状态**：未开始
- **验收状态**：待验收

---

### 原子任务列表（P2-M6）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|---|---|---|---|---|---|---|
| P2-M6-T1 | stores/postEditor.js Pinia store | `stores/postEditor.js` | 一期 | open / close / lockedBoardId 状态 | 未开始 | 待验收 |
| P2-M6-T2 | 安装 vue3-emoji-picker 依赖 | `forum-web/package.json` | 一期 | npm install 成功 + lock 文件更新 | 未开始 | 待验收 |
| P2-M6-T3 | EmojiPicker.vue 包装 | `components/post-editor/EmojiPicker.vue` | T2 | 点表情触发 @select 事件 | 未开始 | 待验收 |
| P2-M6-T4 | BoardSelectDropdown.vue 板块下拉 | `components/board/BoardSelectDropdown.vue` | 一期 boards API | 列出板块 + 搜索 + v-model | 未开始 | 待验收 |
| P2-M6-T5 | PostEditorModal.vue 主弹窗 | `components/post-editor/PostEditorModal.vue` | T1, T3, T4 | 标题校验 + WangEditor 集成 + 表情插入 + 图片上传 | 未开始 | 待验收 |
| P2-M6-T6 | App.vue 全局挂载 PostEditorModal | `App.vue` | T5 | 任何页面都能触发 | 未开始 | 待验收 |
| P2-M6-T7 | AppHeader.vue 发帖按钮接 store | `components/layout/AppHeader.vue` | T1, T6 | 点击调 store.open() | 未开始 | 待验收 |
| P2-M6-T8 | Board.vue 发帖按钮接 store | `views/Board.vue` | T1, T6 | 点击调 store.open({ boardId }) | 未开始 | 待验收 |
| P2-M6-T9 | WangEditor 实例销毁逻辑 | `PostEditorModal.vue` | T5 | Modal 关闭时调用 editor.destroy()，无内存泄漏 | 未开始 | 待验收 |
| P2-M6-T10 | 未保存关闭二次确认 | `PostEditorModal.vue` | T5 | 标题/正文/图片有任一非空时关闭弹确认 | 未开始 | 待验收 |
| P2-M6-T11 | 端到端测试 | 全链路 | T1~T10 | §9 页面测试全部通过 | 未开始 | 待验收 |

---

## P2-M7 搜索增强

> 对应 prd.md §5.7、plan.md §3.2 + §4。扩展一期 /api/search 接口为多类型，重构搜索结果页为 Tab。

### 1. 模块目标
- 一期 `/api/search?q=` 接口扩展 `type` 参数（board / post / user）和 `scope` 参数（title / both）
- 保持向后兼容：无 type / scope 时等同 type=post + scope=both
- 重构一期 Search.vue 为 Tab 切换

### 2. 用户可见内容
- 顶部搜索框（一期已有）输入回车 → 跳 `/search?q=xxx&type=post`（默认）
- 结果页 Tab 切换：板块 / 帖子 / 用户
- 板块结果：卡片网格（头像+名+描述+关注数）
- 帖子结果：贴吧风列表 + 关键词高亮（一期已实现）+ scope 切换（标题/全文）
- 用户结果：列表（头像+昵称+发帖数+加关注）

### 3. 用户操作流程
1. 用户在顶部搜索框输入 "Spring" → 回车 → 跳 `/search?q=Spring&type=post`
2. 切到 "板块" Tab → URL 变 `&type=board` → 显示板块结果
3. 切到 "用户" Tab → 显示用户结果
4. 在 "帖子" Tab 切 "仅标题" → URL 变 `&scope=title` → 重查

### 4. 前端页面任务
- `views/Search.vue`（**覆盖一期 M5-T7**）：Tab 化
- `components/search/SearchResultBoard.vue`
- `components/search/SearchResultPost.vue`（基于 PostCard）
- `components/search/SearchResultUser.vue`
- 修改 `api/search.js`：扩展参数

### 5. Controller 任务
- 修改 `SearchController.search`：
  - GET /api/search?q=&type=board|post|user&scope=title|both&page=&size=
  - 兼容缺省（type=post, scope=both）

### 6. Service 任务
- `SearchService.searchBoards(q, page, size)`
- `SearchService.searchPosts(q, scope, page, size)` — scope=title 走新 SQL
- `SearchService.searchUsers(q, page, size)`

### 7. Mapper 任务
- `BoardMapper.searchByKeyword`：LIKE 多列
- `PostMapper.searchByTitleFulltext`：MATCH(title) AGAINST + ngram
- `UserMapper.searchByKeyword`：LIKE on username/nickname

### 8. Repository / 数据保存任务
- 一期已有 `FULLTEXT idx_search(title, content) WITH PARSER ngram`
- 新增 `FULLTEXT idx_title(title) WITH PARSER ngram`（schema.v2.sql）

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|---|---|---|
| 1 | 顶部搜索框输入"测试" 回车 | 跳 `/search?q=测试&type=post`，默认显示帖子 |
| 2 | 切到"板块" Tab | 显示板块结果 |
| 3 | 切到"用户" Tab | 显示用户结果 |
| 4 | 帖子 Tab 切"仅标题" | URL 加 scope=title，重新查询 |
| 5 | 关键词在标题中匹配 | 标题高亮 |
| 6 | 关键词无任何匹配 | 空状态友好提示 |
| 7 | 短关键词 (<2 字) | 提示"至少 2 个字符"（一期已有） |
| 8 | 一期 URL `/search?q=xxx` 不带 type | 默认 type=post，行为兼容 |

### 10. 接口测试方法

| 步骤 | curl | 预期 |
|---|---|---|
| 1 | `GET /api/search?q=test` | code=0，返回帖子（兼容一期） |
| 2 | `GET /api/search?q=test&type=post&scope=both` | 同上 |
| 3 | `GET /api/search?q=test&type=post&scope=title` | code=0，仅命中 title 中含 test 的帖子 |
| 4 | `GET /api/search?q=test&type=board` | code=0，返回板块列表 |
| 5 | `GET /api/search?q=test&type=user` | code=0，返回用户列表 |
| 6 | `GET /api/search?q=test&type=invalid` | code=2001 |
| 7 | `GET /api/search?q=a&type=post`（<2 字符） | code=2001（一期已有） |

### 11. 异常情况测试

| 场景 | 预期 |
|---|---|
| ngram parser 在新环境未落上 | 中文搜索全 0 结果，需 SHOW CREATE TABLE 验证 |
| 关键词含 SQL 特殊字符 | 一期已转义，安全返回 |
| 限流（一期 30/min） | 触发后返回限流错误 |
| 用户名/昵称完全匹配 | 优先返回（ORDER BY 精确度） |
| 已删除/已禁用用户 | 不出现在结果中 |

### 12. 当前状态
- **任务状态**：未开始
- **验收状态**：待验收

---

### 原子任务列表（P2-M7）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|---|---|---|---|---|---|---|
| P2-M7-T1 | schema.v2.sql 加 idx_title FULLTEXT 索引 | `db/schema.v2.sql` | P2-M1-T1 | SHOW CREATE TABLE post 含 idx_title WITH PARSER ngram | 未开始 | 待验收 |
| P2-M7-T2 | BoardMapper.searchByKeyword | `mapper/BoardMapper.java` | P2-M1-T3 | LIKE on name/description/slogan/tags，分页 | 未开始 | 待验收 |
| P2-M7-T3 | PostMapper.searchByTitleFulltext | `mapper/PostMapper.java` | T1 | MATCH(title) AGAINST + 分页 | 未开始 | 待验收 |
| P2-M7-T4 | UserMapper.searchByKeyword | `mapper/UserMapper.java` | 一期 | LIKE username/nickname + 过滤已禁用 | 未开始 | 待验收 |
| P2-M7-T5 | SearchService.searchBoards | `service/SearchService.java` | T2 | 单测：命中/未命中 | 未开始 | 待验收 |
| P2-M7-T6 | SearchService.searchPosts 扩展 scope | `service/SearchService.java` | T3 + 一期 SearchService | 单测：scope=title vs both 行为差异 | 未开始 | 待验收 |
| P2-M7-T7 | SearchService.searchUsers | `service/SearchService.java` | T4 | 单测 | 未开始 | 待验收 |
| P2-M7-T8 | SearchController 扩展 type / scope 参数 | `controller/SearchController.java` | T5~T7 | 兼容性测试：旧 URL 行为不变；新 type 正确路由 | 未开始 | 待验收 |
| P2-M7-T9 | 前端 api/search.js 扩展参数 | `api/search.js` | T8 | searchPosts/Boards/Users + scope 参数 | 未开始 | 待验收 |
| P2-M7-T10 | SearchResultBoard.vue / Post.vue / User.vue 三组件 | `components/search/*.vue` | T9 | 各自渲染对应数据结构 | 未开始 | 待验收 |
| P2-M7-T11 | Search.vue 重写（**覆盖 M5-T7**） | `views/Search.vue` | T9, T10 | Tab 切换 + URL 同步 + scope toggle | 未开始 | 待验收 |
| P2-M7-T12 | 上线检查：SHOW CREATE TABLE post | 数据库 | T1 | 含 idx_search 和 idx_title，都带 WITH PARSER ngram | 未开始 | 待验收 |
| P2-M7-T13 | 端到端联调 | 全链路 | T1~T12 | §9 页面测试 + §10 接口测试 全部通过 | 未开始 | 待验收 |

---

## P2-M8 板块管理增强（吧主权限）

> 对应 prd.md §5.8、plan.md §3.3。吧主可修改板块信息、本板块内置顶/加精/删帖；管理员可修改 owner_user_id。
>
> **二次盘点修正（2026-05-23）**：一期 `BoardService.updateBoard(id, name, description, sortWeight, operatorId)` 已实现完整字段更新，但缺吧主身份校验。二期 P2-M8-T3 改为**新增** `ownerUpdate` 方法（吧主路径，强制排除 name），**保留**一期 `updateBoard`（管理员路径）不动。

### 1. 模块目标
- 吧主可修改本板块的 description / icon / slogan / tags（**name 不可改**）
- 吧主在本板块内有置顶/加精/删帖权限（一期管理员能力扩展）
- 管理员可在后台修改 board.owner_user_id（吧主移交）

### 2. 用户可见内容
- 板块页右上角：吧主登录后看到"编辑板块"按钮 → 弹出编辑表单
- 板块内帖子操作菜单：吧主看到置顶/加精/删除（与管理员相同）
- 后台板块管理：吧主信息显示 + "移交吧主"按钮（管理员可见）

### 3. 用户操作流程
1. 吧主进入自己的板块 → 看到"编辑板块"按钮 → 点击 → 弹表单 → 改描述/头像/口号/标签 → 保存
2. 吧主在本板块帖子的卡片菜单点"置顶" → 该帖固定列表顶部
3. 管理员在后台板块管理 → 选某板块 → 点"移交吧主" → 输入新吧主 user_id → 确认 → 移交成功

### 4. 前端页面任务
- `components/board/BoardEditForm.vue`：板块编辑表单
- 修改 `Board.vue`：当前用户=owner_user_id 或 admin 时显示"编辑板块"按钮
- 修改一期 `PostCard.vue` 操作菜单：吧主显示管理操作
- 修改一期管理后台板块管理：加"移交吧主"按钮

### 5. Controller 任务
- 新增 `PATCH /api/boards/{id}` — 吧主或管理员可修改
- 新增 `POST /api/admin/boards/{id}/transfer-owner` — 仅管理员

### 6. Service 任务
- `BoardService.update(boardId, dto, currentUserId)` — 权限校验 + 排除 name
- `BoardService.transferOwner(boardId, newOwnerId, adminId)`
- 一期 PostService 中置顶/加精/删帖加权限分支：管理员 OR 板块吧主

### 7. Mapper 任务
- `BoardMapper.updatePartial` — 仅更新非 null 字段
- `BoardMapper.updateOwner`

### 8. Repository / 数据保存任务
- 修改 board 表（已有字段）

### 9. 页面测试方法

| 步骤 | 操作 | 预期结果 |
|---|---|---|
| 1 | 普通用户访问某板块 | 看不到"编辑板块"按钮 |
| 2 | 吧主访问自己的板块 | 看到"编辑板块"按钮 |
| 3 | 吧主点编辑 → 改描述 → 保存 | 板块详情更新 |
| 4 | 吧主尝试改 name | 表单中 name 字段只读 |
| 5 | 吧主在本板块某帖菜单点"置顶" | 帖子置顶 |
| 6 | 吧主在其他板块帖子菜单 | 没有置顶按钮 |
| 7 | 管理员后台 → 板块管理 → 移交吧主 | 移交后板块 owner_user_id 更新 |
| 8 | 旧吧主刷新板块页 | 已无编辑按钮（除非也是管理员） |

### 10. 接口测试方法

| 步骤 | curl | 预期 |
|---|---|---|
| 1 | `PATCH /api/boards/1`（非吧主非管理员） | code=1002 |
| 2 | `PATCH /api/boards/1`（吧主） | code=0，部分字段更新 |
| 3 | `PATCH /api/boards/1` 含 name 字段 | code=2001 "name 不可改" 或忽略 |
| 4 | `POST /api/admin/boards/1/transfer-owner`（非管理员） | code=1002 |
| 5 | `POST /api/admin/boards/1/transfer-owner`（管理员） | code=0，owner_user_id 更新 |
| 6 | 吧主调用一期置顶接口（本板块） | code=0（一期需扩展权限） |
| 7 | 吧主调用一期置顶接口（其他板块） | code=1002 |

### 11. 异常情况测试

| 场景 | 预期 |
|---|---|
| 移交给不存在的 user_id | code=3001 |
| 移交给已禁用的用户 | code=403 |
| 系统板块 (id=1) 修改 name 或移交 | 应用层硬拦截，code=403 |
| 吧主自己取关本板块 | 允许（关注与吧主身份独立） |
| 吧主账号被封禁 | 该板块进入"无主"状态，仅管理员可管 |

### 12. 当前状态
- **任务状态**：未开始
- **验收状态**：待验收

---

### 原子任务列表（P2-M8）

| ID | 任务 | 涉及文件 | 前置依赖 | 验收方式 | 状态 | 验收 |
|---|---|---|---|---|---|---|
| P2-M8-T1 | BoardMapper.updatePartial | `mapper/BoardMapper.java` | P2-M1-T3 | MyBatis 动态 SQL 仅更新非 null 字段 | 未开始 | 待验收 |
| P2-M8-T2 | BoardMapper.updateOwner | `mapper/BoardMapper.java` | P2-M1-T3 | UPDATE board SET owner_user_id=? WHERE id=? | 未开始 | 待验收 |
| P2-M8-T3 | BoardService.ownerUpdate（**新增**吧主路径，权限校验 + 强制排除 name） | `service/BoardService.java` | T1 | 单测 4/4（吧主成功/管理员成功/普通用户 1002/系统板块 id=1 拦截）；保留一期 updateBoard 不动 | 未开始 | 待验收 |
| P2-M8-T4 | BoardService.transferOwner | `service/BoardService.java` | T2 | 单测 3/3（成功/用户不存在/已禁用） | 未开始 | 待验收 |
| P2-M8-T5 | BoardController.patch /api/boards/{id} | `controller/BoardController.java` | T3 | 接口测试 §10 第 1~3 项 | 未开始 | 待验收 |
| P2-M8-T6 | AdminBoardController.transferOwner | `controller/admin/AdminBoardController.java` 或一期对应 | T4 | 接口测试 §10 第 4~5 项 | 未开始 | 待验收 |
| P2-M8-T7 | PostService 置顶/加精/删帖加吧主权限分支 | `service/PostService.java`（一期） | T3 | 单测：管理员/本板块吧主/其他板块吧主 三种身份 | 未开始 | 待验收 |
| P2-M8-T8 | BoardEditForm.vue 表单组件 | `components/board/BoardEditForm.vue` | T5 | 4 字段 + 校验 + 提交 | 未开始 | 待验收 |
| P2-M8-T9 | Board.vue 加"编辑板块"按钮 | `views/Board.vue` | T8 | 仅吧主或管理员显示 | 未开始 | 待验收 |
| P2-M8-T10 | 一期 PostCard 菜单加吧主操作 | `components/post/PostCard.vue` | T7 | 当前用户=吧主时显示置顶/加精/删除 | 未开始 | 待验收 |
| P2-M8-T11 | 一期后台板块管理加"移交吧主"按钮 | 一期 admin 板块管理 view | T6 | 输入 user_id → 确认 → API 调用 | 未开始 | 待验收 |
| P2-M8-T12 | 系统板块 id=1 硬拦截 | `BoardService` | T3, T4 | 单测：修改/移交均抛 403 | 未开始 | 待验收 |
| P2-M8-T13 | 端到端联调 | 全链路 | T1~T12 | §9 页面测试全部通过 | 未开始 | 待验收 |

---

## 部署与上线检查

二期所有模块完成后，按以下顺序部署：

### 数据库迁移
```bash
mysql -u root -p --default-character-set=utf8mb4 forum < forum-server/src/main/resources/db/schema.v2.sql
```

### 上线检查 SQL
```sql
SHOW CREATE TABLE board\G    -- 含 icon/slogan/tags/owner_user_id/follower_count 字段
SHOW CREATE TABLE board_application\G    -- 表存在
SHOW CREATE TABLE user_board_follow\G    -- 表存在
SHOW CREATE TABLE post\G    -- idx_search 和 idx_title 都带 WITH PARSER ngram

SELECT id, name, owner_user_id FROM board;    -- 全部不为 NULL
```

### 后端启动
```bash
cd forum-server
mvn clean install
mvn spring-boot:run
```

### 前端构建
```bash
cd forum-web
npm install    # 安装 vue3-emoji-picker
npm run build
```

### 烟测清单
- [ ] 登录后顶部"发帖"按钮触发 Modal
- [ ] 板块页"发帖"按钮触发 Modal 且锁定板块
- [ ] 关注 1 个板块 → 首页信息流出现关注板块内容
- [ ] 满足资格用户提交申请 → 管理员后台审核通过 → 板块出现在列表
- [ ] 申请人成为新板块吧主，可修改板块信息
- [ ] 搜索框输入关键词 → 跳搜索页 → 切换 Tab 看不同结果
- [ ] 一期 `/post/create` 路由仍可用
- [ ] 一期 `/api/search?q=xxx` 不传新参数行为不变

---

## 二期完成判定

所有 P2-M{n} 模块的"当前状态"=已完成 + 验收状态=自动验收通过 / 人工验收通过 时，二期整体完成。

---

*文档结束*

