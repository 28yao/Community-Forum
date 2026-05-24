# 三期需求文档（Phase 3）

> 在一期 MVP + 二期 P2-M1~M8 基础上的扩展期规划
> 文档创建：2026-05-24
> 状态：**待用户确认**

---

## 一、本期需求清单（来源用户原话）

| # | 需求 | 用户原话 |
|---|------|---------|
| R1 | 图片圆角 8px | 发布的帖子中 图片要有 8% 比例圆角 |
| R2 | 三栏布局加宽 | 中间列太窄、网页两边太空，参考贴吧布局 |
| R3 | 双层公告系统 | 管理员发全站公告 + 吧主在自己板块发板块公告 |
| R4 | 帖子详情页加侧边栏 | 详情页左侧也显示板块列表、右侧显示该板块公告 |
| R5 | 主页侧边栏顶部加入口 | 我的关注板块上方加"主页"和"个人中心"两个入口 |

---

## 二、已有能力盘点（盘点表 / 三分类）

### A. 已有 — 不动（直接复用，不在三期触碰）

| 能力 | 一期/二期位置 | 三期沿用方式 |
|---|---|---|
| 用户认证、登录态、Token | 一期 M1 + 二期热修 | 三期所有接口直接复用 AuthInterceptor |
| 板块表 board + 板块申请 + 关注 | 一期 M2 + 二期 P2-M1/M2/M3 | 三期公告依附于 board 表 |
| 帖子表 post / 图片 post_image | 一期 M3 | 不动 |
| 点赞 / 评论 / 收藏 | 一期 M4 | 不动 |
| FULLTEXT 搜索（含 ngram） | 一期 M5 + 二期 P2-M7 | 不动 |
| 后台管理（用户/版块/帖子/评论） | 一期 M6 | 三期复用后台框架加 announcement 管理页 |
| 板块管理（吧主权限） | 二期 P2-M8 | 三期"吧主发板块公告"权限校验复用 board.owner_user_id 字段 |
| 发帖弹窗（贴吧风） | 二期 P2-M6 + 最新热修 | 不动 |
| App.vue 容器：`max-width: 1200px` | 一期 M0 已设定 | 不动 |
| BoardSidebar / AppSidebar 框架 | 一期 M2 + 二期 P2-M4 | 三期"统一侧边栏"在此之上扩展 |
| 图片网格 PostImageGrid | 一期 M3 + 二期 P2-M4 | R1 只调圆角值 |

### B. 已有 — 需扩展（在原有基础上调字段 / 加分支）

| 能力 | 现状 | 三期扩展点 |
|---|---|---|
| **PostImageGrid 圆角** | 6px | 改 8px（R1）|
| **PostDetail.vue 图片** | 6px | 改 8px（R1）|
| **Home.vue 中列宽** | `max-width: 900px` | 加宽至 1100px 或去掉中列限制，让中列吃满三栏剩余空间（R2）|
| **PostDetail.vue 中列宽** | `max-width: 900px` | 加宽至 1100px 或改为三栏布局（R2 + R4）|
| **AppSidebar / BoardSidebar** | 各页面用法不一 | 统一为 `UnifiedSidebar`：顶部"主页/个人中心"入口 + 我的关注 + 推荐板块 + 全部板块（R5）|
| **PostDetail.vue 页面结构** | 单栏中间 + 右侧无内容 | 改为三栏：左 UnifiedSidebar + 中正文 + 右板块公告（R4）|
| **board 表字段** | 已扩展 icon/slogan/tags/owner_user_id/follower_count | **不再扩展**（公告走独立表，避免单表字段膨胀）|

### C. 全新 — 要从零新建

| 新增能力 | 形态 | 依赖 |
|---|---|---|
| **announcement 表** | 新建数据库表（公告内容、发布人、作用域 site/board、关联 board_id） | 依赖 B 中 board 表的 owner_user_id 做吧主权限校验 |
| **AnnouncementService** | 新 Service：CRUD + 权限校验（site 仅 admin / board 仅 admin+吧主） | 依赖现有 BoardService |
| **AnnouncementController（前台）** | GET /api/announcements?scope=site / GET /api/boards/{id}/announcements | 独立 |
| **AnnouncementController（后台）** | POST/PUT/DELETE /api/admin/announcements + 板块管理路径下吧主接口 | 复用 AdminAuthInterceptor + 新的"吧主或admin"判断器 |
| **AdminAnnouncements.vue** | 后台公告管理页（admin 视角） | 复用 AdminLayout |
| **BoardOwnerAnnouncementPanel.vue** | 板块详情页里给吧主用的"我管理的板块公告"面板 | 依赖 P2-M8 吧主管理入口 |
| **AnnouncementSidebar.vue** | 帖子详情页右侧的"本板块公告"小卡片（前台只读展示） | 独立 |
| **UnifiedSidebar.vue** | 顶部"主页/个人中心"快捷入口 + 我的关注 + 推荐板块（合并 AppSidebar / BoardSidebar 的现有结构） | 替换 Home/Board/PostDetail 三个页面里现有的侧边栏用法 |

---

## 三、需求分项设计

### R1 — 图片 8px 圆角

**变更范围**：列表卡片 + 帖子详情页

| 文件 | 现状 | 目标 |
|---|---|---|
| `forum-web/src/components/post/PostImageGrid.vue` 第 60 行 `border-radius: 6px` | 6px | **8px** |
| `forum-web/src/views/PostDetail.vue` 第 293 行 `border-radius: 6px` | 6px | **8px** |
| `forum-web/src/components/post-editor/PostEditorModal.vue` `.upload-thumb` | 6px | **8px**（保持视觉一致）|

**工作量**：3 处 CSS 修改，约 5 分钟。

> 注：原话"8% 比例圆角"无法直接落地（CSS border-radius 不支持百分比的统一直觉值，使用 % 会得到椭圆）。采纳 **8px** 作为约 8% 视觉等价值。

---

### R2 — 三栏总宽与中列加宽

**当前布局诊断**：

- App.vue 容器：`max-width: 1200px` ✅ 已经够宽
- Home.vue 中列：`max-width: 900px` ❌ 太窄，三栏总和才约 1130px
- PostDetail.vue 主体：`max-width: 900px` ❌ 同上

**真正问题不是外层窄，而是内层中列被人为限定 900px**。

**变更范围**：

| 文件 | 现状 | 目标 |
|---|---|---|
| `forum-web/src/views/Home.vue` 中列 max-width | 900px | **去掉 max-width**，让中列吃满 `flex: 1`（左右栏定宽，中列自适应） |
| `forum-web/src/views/Board.vue` 中列 | 同上 | 同上 |
| `forum-web/src/views/PostDetail.vue` 主体容器 | 900px | 同上 |
| `forum-web/src/App.vue` `max-width: 1200px` | 1200px | **保持**（视用户反馈，必要时调到 1280px）|

**侧边栏宽度约定**：

- 左侧 UnifiedSidebar：240px
- 右侧栏（公告 / 推荐 / 热门）：260px
- 中间列：剩余空间（约 660~720px 不等，取决于视口）

---

### R3 — 双层公告系统

#### 3.1 数据库设计（新表）

```sql
CREATE TABLE announcement (
    id              BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    scope           VARCHAR(10) NOT NULL COMMENT '作用域：site/board',
    board_id        BIGINT UNSIGNED DEFAULT NULL COMMENT 'scope=board 时必填',
    title           VARCHAR(100) NOT NULL,
    content         TEXT NOT NULL,
    pinned          TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶',
    sort_weight     INT NOT NULL DEFAULT 0,
    publisher_id    BIGINT UNSIGNED NOT NULL,
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '1=显示 0=隐藏',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT NOT NULL DEFAULT 0,
    INDEX idx_scope_board (scope, board_id, status, deleted),
    INDEX idx_publisher (publisher_id),
    INDEX idx_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='公告（站点级 / 板块级双层）';
```

**约束说明**：
- `scope='site'` 时 `board_id` 必须为 NULL；`scope='board'` 时必填
- Service 层做这层校验，不依赖 DB 触发器

**落地文件**：`forum-server/src/main/resources/db/schema.v3.sql`

#### 3.2 权限矩阵

| 操作 | 站点公告（site） | 板块公告（board）|
|---|---|---|
| 查看 | 所有人 | 所有人 |
| 新建/编辑/删除 | **仅 admin** | **admin 或 该板块吧主** |
| 查看作用域 | 全站可见 | 仅限对应板块页 |

**新增权限判断工具**：`@RequireBoardOwnerOrAdmin` 注解 + Aspect，或在 Service 层做 `assertIsBoardOwnerOrAdmin(userId, boardId)`。**优先在 Service 层手动调用**，避免引入 Aspect 复杂度（参考 CLAUDE.md "简单优先"）。

#### 3.3 后端 API

**前台（只读）**：
- `GET /api/announcements?scope=site&page=1&size=10` — 站点公告列表
- `GET /api/boards/{boardId}/announcements?page=1&size=10` — 板块公告列表

**后台（admin）**：
- `POST /api/admin/announcements` — 新建站点公告
- `PUT /api/admin/announcements/{id}` — 编辑（admin 可改任意，吧主仅可改自己板块的）
- `DELETE /api/admin/announcements/{id}` — 删除（同上）
- `GET /api/admin/announcements?scope=&boardId=&page=&size=` — 列表查询

**吧主管理（认证态前台）**：
- `POST /api/boards/{boardId}/announcements` — 吧主在自己板块发公告
- 共用 `PUT /api/admin/announcements/{id}` 和 `DELETE`，由 Service 层判断当前用户是否吧主

#### 3.4 前端落地

| 组件/页面 | 角色 | 位置 |
|---|---|---|
| `AnnouncementSidebar.vue` | 公共展示组件 | `forum-web/src/components/announcement/` |
| `BoardOwnerAnnouncementPanel.vue` | 吧主管理面板 | `forum-web/src/components/announcement/` |
| `AdminAnnouncements.vue` | 后台公告管理页 | `forum-web/src/views/admin/` |
| 接入点 | Home.vue 右栏（site）、Board.vue 右栏（board）、PostDetail.vue 右栏（board）| — |
| 路由 | `/admin/announcements` | 后台菜单加入口 |

---

### R4 — 帖子详情页加左侧 SideNav + 右侧板块公告

**现状**：PostDetail.vue 是单栏（max-width: 900px 居中）。

**目标**：改为三栏（左 SideNav / 中正文 / 右板块公告）

**变更范围**：

| 文件 | 改动 |
|---|---|
| `forum-web/src/views/PostDetail.vue` | 外层 wrap 改为 3 列布局；左侧用 UnifiedSidebar；右侧用 AnnouncementSidebar（scope=board, boardId=post.boardId）|

**布局示意**：

```
┌─────────────────────────────────────────┐
│ AppHeader                               │
├──────┬──────────────────────────┬───────┤
│ 240  │  Post 正文 + 评论树       │ 260   │
│ 左栏 │  （flex:1，吃满中间）     │ 右栏  │
│ 板块 │                          │ 板块  │
│ 导航 │                          │ 公告  │
└──────┴──────────────────────────┴───────┘
```

---

### R5 — 主页侧边栏顶部加"主页/个人中心"入口

**现状**：
- `AppSidebar.vue`：只有"版块导航"标题 + 板块列表
- `BoardSidebar.vue`（二期 P2-M4 引入）：包含"我关注的"、"推荐板块"、"全部板块"三段（参考 image-4.png 现状）

**目标**：合并为 `UnifiedSidebar.vue`，结构（从上至下）：

```
┌─────────────────────┐
│ 🏠 主页              │  ← 新增（R5）
│ 👤 个人中心          │  ← 新增（R5）
├─────────────────────┤
│ ⭐ 我关注的          │  ← 现有 BoardSidebar
│  └ 板块卡片列表       │
├─────────────────────┤
│ 📈 推荐板块          │  ← 现有
│  └ ...               │
├─────────────────────┤
│ 📋 全部板块          │  ← 现有
│  └ ...               │
└─────────────────────┘
```

**变更范围**：

| 文件 | 改动 |
|---|---|
| `forum-web/src/components/layout/UnifiedSidebar.vue` | **新建**，包含顶部"主页/个人中心"区块 + 现有 BoardSidebar 三段 |
| `forum-web/src/components/layout/BoardSidebar.vue` | 内容迁移到 UnifiedSidebar 后**删除** 或保留为内部子组件 |
| `forum-web/src/components/layout/AppSidebar.vue` | 同上判断（推荐删除，因为 P2-M4 已实质替代）|
| `Home.vue` / `Board.vue` / `PostDetail.vue` | 引用从 AppSidebar/BoardSidebar 切换为 UnifiedSidebar |

**入口跳转**：
- "主页" → router.push('/')
- "个人中心" → router.push(`/users/${userStore.id}`)（注：当前用户主页路由可能未实现，需确认路由表）

---

## 四、任务拆解（建议执行顺序）

> 按"先简后繁、先 UI 后接口"的顺序，可独立交付、可独立验收。

| ID | 任务 | 类别 | 文件数 | 预估 | 依赖 |
|---|---|---|---|---|---|
| **P3-M1** | R1 图片圆角 8px | B 类（CSS 微调）| 3 | 5 分钟 | 无 |
| **P3-M2** | R2 中列加宽 + 整体布局调整 | B 类 | 4-5 | 30 分钟 | 无 |
| **P3-M3** | R5 UnifiedSidebar 新建 + Home/Board/PostDetail 接入 | B 类（重构） | 5-8 | 60-90 分钟 | 无 |
| **P3-M4** | R3.1 announcement 表 + Entity + Mapper | C 类 | 4 | 30 分钟 | 无 |
| **P3-M5** | R3.2 AnnouncementService + 权限判断 + 单元测试 | C 类 | 3 | 60 分钟 | M4 |
| **P3-M6** | R3.3 前台 + 后台 AnnouncementController + 集成测试 | C 类 | 3 | 45 分钟 | M5 |
| **P3-M7** | R3.4 AnnouncementSidebar + BoardOwnerPanel + AdminAnnouncements 页 | C 类 | 4-6 | 90 分钟 | M4, M6 |
| **P3-M8** | R4 PostDetail 改三栏 + 接入 UnifiedSidebar + AnnouncementSidebar | B 类 | 1 | 30 分钟 | M3, M7 |

**预估总时长**：约 6 小时（实际可能 4-8 小时）

---

## 五、冲突自检清单

- [x] announcement 表名 — 与现有 10 表无重复 ✅
- [x] 新建接口 `/api/announcements`、`/api/admin/announcements` — 与现有路径无重复 ✅
- [x] AnnouncementService / AnnouncementController — 与现有 Service/Controller 无重名 ✅
- [x] UnifiedSidebar.vue / AnnouncementSidebar.vue — 与 components/ 现有组件无重名 ✅
- [x] AdminAnnouncements.vue — 与 views/admin/ 现有页面无重名 ✅
- [x] 所有"全新建"项都能在盘点表 C 类找到对应行 ✅
- [x] B 类扩展全部走 ALTER / CSS 调整 / 重构，不破坏一期接口语义 ✅

---

## 六、二期 → 三期 覆盖映射

| 二期任务 | 覆盖状态 | 三期对应 |
|---|---|---|
| 二期 BoardSidebar / AppSidebar | 重构合并 | **P3-M3** UnifiedSidebar |
| 二期 P2-M4 贴吧风 UI | 继续沿用，仅微调 | P3-M1 图片圆角、P3-M2 宽度调整 |
| 一期 PostDetail.vue 单栏 | 重写为三栏 | **P3-M8** |
| 其余二期任务 | 不动 | — |

---

## 七、用户已拍板的决策点（2026-05-24 确认）

| # | 决策 | 选择 |
|---|---|---|
| 1 | 图片圆角值 | **8px** |
| 2 | 中列宽度策略 | **去掉中列 max-width，flex 自适应吃满** |
| 3 | 公告权限判断 | **Service 层手动校验** `assertIsBoardOwnerOrAdmin()` |
| 4 | 吧主公告入口 | 板块管理页面加"公告管理" tab（待 P3-M7 落地时再细化）|
| 5 | 个人中心路由 | **复用现有路由**（点"个人中心"跳到已有的 /settings 或 /users/{id}，按当前路由表实际情况决定）|
| 6 | 执行顺序 | **P3-M1 → M8 逐模块 commit + push**（沿用二期质量标准）|
| 7 | 公告正文格式 | **沿用帖子 HTML 存储**（plainToHtml 转换，v-html 渲染）|
| 8 | 公告数量上限 | **不限数量**，靠分页控制 |
| 9 | PostDetail 右栏内容 | **仅放板块公告**，不混入推荐/热门 |
| 10 | Home 右栏内容 | **仅放站点公告**（scope=site）|
| 11 | 公告 title 字数上限 | **50 字** |
| 12 | 公告 content 字数上限 | **500 字** |

---

## 八、风险与注意事项

- **公告作为"独立表"而非 board 字段**：避免 board 表字段膨胀；未来若有公告评论、阅读统计等扩展，独立表更友好。
- **PostDetail 改三栏对移动端的影响**：移动端可能需要保留单栏。建议加 `@media (max-width: 768px)` 媒体查询，侧边栏在小屏隐藏。
- **吧主权限的"是否本板块"校验**：必须每个吧主接口都校验 `board.owner_user_id == currentUserId`，避免越权管理其他板块的公告。
- **announcement.deleted 软删除字段保留**：与一期所有业务表风格保持一致。

---

> **请逐项确认上方"七、需用户拍板的决策点"。我会在你确认后开始按 P3-M1 → M8 顺序逐个实施 + 单模块 commit/push。**
