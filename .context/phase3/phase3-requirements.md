# 三期需求文档（Phase 3）

> 在一期 MVP + 二期 P2-M1~M8 基础上的扩展期规划
> 文档创建：2026-05-24
> 最后更新：2026-05-24
> 状态：**执行中（R1–R7 + 热修 H1–H7 已落地）**

---

## 一、本期需求清单（来源用户原话）

| # | 需求 | 用户原话 | 状态 |
|---|------|---------|------|
| R1 | 图片圆角 8px | 发布的帖子中 图片要有 8% 比例圆角 | ✅ 已完成 |
| R2 | 三栏布局加宽 | 中间列太窄、网页两边太空，参考贴吧布局 | ✅ 已完成 |
| R3 | 双层公告系统 | 管理员发全站公告 + 吧主在自己板块发板块公告 | ✅ 已完成 |
| R4 | 帖子详情页加侧边栏 | 详情页左侧也显示板块列表、右侧显示该板块公告 | ✅ 已完成 |
| R5 | 主页侧边栏顶部加入口 | 我的关注板块上方加"主页"和"个人中心"两个入口 | ✅ 已完成 |
| R6 | 公告显示内容预览+日期 | 板块公告列表显示标题+4行内容预览+更新时间 | ✅ 已完成 |
| R7 | 板块删除功能 | 编辑板块弹窗增加删除按钮，吧主/管理员可删除 | ✅ 已完成 |

---

## 二、已有能力盘点（盘点表 / 三分类）

### A. 已有 — 不动（直接复用，不在三期触碰）

| 能力 | 一期/二期位置 | 三期沿用方式 |
|---|---|---|
| 用户认证、登录态、Token | 一期 M1 + 二期热修 | 三期所有接口直接复用 AuthInterceptor |
| 板块表 board + 板块申请 + 关注 | 一期 M2 + 二期 P2-M1/M2/M3 | 三期公告依附于 board 表 |
| 帖子表 post / 图片 post_image | 一期 M3 | 不动 |
| 点赞 / 收藏 | 一期 M4 | 不动 |
| FULLTEXT 搜索（含 ngram） | 一期 M5 + 二期 P2-M7 | 不动 |
| 后台管理（用户/版块/帖子/评论） | 一期 M6 | 三期复用后台框架加 announcement 管理页 |
| 板块管理（吧主权限） | 二期 P2-M8 | 三期公告、置顶等权限校验复用 `board.owner_user_id` |
| 发帖/编辑弹窗（贴吧风） | 二期 P2-M6 | 发帖与编辑共用 `PostEditorModal`（见 H4） |
| 评论两层楼中楼 | 一期 M4 | 后端逻辑不动；三期重做前台 UI（见 H5） |

### B. 已有 — 需扩展（在原有基础上调字段 / 加分支）

| 能力 | 原状 | 三期扩展点（实际落地） |
|---|---|---|
| **PostImageGrid** | 6px 圆角、固定 grid | **8px 圆角** + `variant="list\|detail"`；单图原比例、多图 1:1 横排最多 3 张；flex 左对齐（见 H2） |
| **PostDetail.vue 图片** | 内联样式 | 改用 `PostImageGrid variant="detail"`；正文内嵌图 `max-width: calc(100%/3)` |
| **全局布局** | 各页 `max-width: 900px`、App 1200px | `global.css`：`--page-max-width: 1600px` + `.three-col-layout` 公共类（R2） |
| **AppSidebar / BoardSidebar** | 各页面用法不一 | 统一为 `UnifiedSidebar`（R5） |
| **PostDetail.vue 页面结构** | 单栏 | 三栏：左 UnifiedSidebar + 中正文 + 右板块公告（R4） |
| **个人中心** | `/settings` 独立页 | **弹窗** `SettingsModal` + `userSettings` store（见 H6） |
| **帖子置顶** | 仅后台 AdminPosts | 前台吧主/管理员快捷置顶（见 H3）；二期 P2-M8 帖子操作已部分落地 |
| **board 表字段** | 已有 owner 等 | **不再扩展**（公告走独立表） |

### C. 全新 — 要从零新建（R1–R7 规划项）

| 新增能力 | 形态 | 依赖 |
|---|---|---|
| **announcement 表** | 新建数据库表 | `board.owner_user_id` 权限校验 |
| **AnnouncementService** | CRUD + 权限 | BoardService |
| **AnnouncementController（前台/后台）** | 见 §三 R3、§十一 | — |
| **AdminAnnouncements.vue** | 后台公告管理 | AdminLayout |
| **BoardOwnerAnnouncementPanel.vue** | 吧主管理面板 | P2-M8 |
| **AnnouncementSidebar.vue** | 前台只读展示 | — |
| **AnnouncementDetailDialog.vue** | 公告只读详情弹窗 | H1 |
| **UnifiedSidebar.vue** | 主页/个人中心 + 关注/推荐/全部 | 替换原侧栏 |

### C+. 执行期新增（热修 H1–H7，见 §九）

| 新增能力 | 形态 |
|---|---|
| `utils/richText.js` | 公告/富文本展示、纯文本预览 |
| `stores/userSettings.js` + `SettingsModal.vue` | 个人中心弹窗 |
| `ChangePasswordModal.vue` | 改密二级弹窗 |
| `SettingsRedirect.vue` | `/settings` 兼容路由 |
| `ChangePasswordRequest` + `PUT /api/users/password` | 用户改密 |
| `POST /api/posts/{id}/pin` | 前台置顶（吧主/管理员） |

---

## 三、需求分项设计

### R1 — 图片 8px 圆角

**变更范围**：列表卡片 + 帖子详情 + 发帖弹窗缩略图

| 文件 | 目标 |
|---|---|
| `PostImageGrid.vue` | `border-radius: 8px`；支持 `variant`（见 H2） |
| `PostEditorModal.vue` `.upload-thumb` | **8px** |
| `PostDetail.vue` | 详情区由 `PostImageGrid` 承担圆角 |

> 注：原话「8% 比例圆角」采纳 **8px** 作为视觉等价值。

---

### R2 — 三栏总宽与中列加宽

**实际落地**（与初稿「保持 1200px」不同，见 §十）：

| 文件 | 目标 |
|---|---|
| `forum-web/src/styles/global.css` | `--page-max-width: 1600px`；`.three-col-layout` 三栏 flex |
| `App.vue` / `AppHeader` / `AppFooter` | 使用 `var(--page-max-width)` |
| `Home.vue` / `Board.vue` / `PostDetail.vue` | 去掉中列 `max-width: 900px`，使用 `.three-col-layout` |

**侧边栏宽度约定**：

- 左侧 UnifiedSidebar：**240px**，`margin-right: 100px`（与 `gap: 16px` 合计左–中间距约 **116px**）
- 右侧栏：**260px**
- 中间列：`flex: 1`，随 `--page-max-width` 自适应
- 移动端：`@media (max-width: 768px)` 隐藏左右栏

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

**落地文件**：`forum-server/src/main/resources/db/schema.v3.sql`

#### 3.2 权限矩阵

| 操作 | 站点公告（site） | 板块公告（board）|
|---|---|---|
| 查看 | 所有人 | 所有人 |
| 新建/编辑/删除 | **仅 admin** | **admin 或 该板块吧主** |

**权限实现**：Service 层 `assertIsBoardOwnerOrAdmin()` 手动校验（无 Aspect）。

#### 3.3 后端 API

**前台（只读）**：
- `GET /api/announcements?scope=site&page=1&size=10`
- `GET /api/boards/{boardId}/announcements?page=1&size=10`

**后台（admin）**：
- `GET/POST/PUT/DELETE /api/admin/announcements`

**吧主管理（认证态前台）**：
- `POST /api/boards/{boardId}/announcements`
- `PUT/DELETE /api/announcements/{id}`（Service 判断吧主）

#### 3.4 前端落地

| 组件/页面 | 角色 |
|---|---|
| `AnnouncementSidebar.vue` | 列表展示（Home 右栏 site / Board、PostDetail 右栏 board） |
| `AnnouncementDetailDialog.vue` | 只读详情（H1：普通用户可点击查看） |
| `BoardOwnerAnnouncementPanel.vue` | 吧主管理 |
| `AdminAnnouncements.vue` | 后台管理 |
| `Board.vue` | 板块页公告 widget + 预览/日期（R6） |

**字数上限（热修后）**：title 50 字；content **1000 字**（见 §七 #12、H1）。

---

### R4 — 帖子详情页三栏

| 文件 | 改动 |
|---|---|
| `PostDetail.vue` | `.three-col-layout`；左 `UnifiedSidebar`；右 `AnnouncementSidebar(scope=board)` |

---

### R5 — 主页侧边栏「主页 / 个人中心」

**入口行为（热修后）**：

| 入口 | 行为 |
|---|---|
| 主页 | `router.push('/')` |
| 个人中心 | `useUserSettingsStore().open()` 打开 **SettingsModal**（非跳转独立页） |
| `/settings` | `SettingsRedirect.vue`：打开弹窗后 `replace('/')`（兼容旧链接） |

侧栏「个人中心」高亮：`settingsStore.visible === true`。

---

### R6 — 公告显示内容预览 + 日期

| 文件 | 改动 |
|---|---|
| `Board.vue` / `AnnouncementSidebar.vue` | 标题 + 内容预览（`toPlainTextPreview`）+ 相对时间 |
| `utils/richText.js` | `formatRichContent` / `toPlainTextPreview` |

---

### R7 — 板块删除功能

| 文件 | 改动 |
|---|---|
| `BoardService.deleteBoard` | 软删除；系统板块 id=1 不可删 |
| `DELETE /api/boards/{id}` | 吧主或 admin |
| `BoardEditForm.vue` | 红色「删除板块」+ 二次确认 |

---

## 四、任务拆解与完成状态

### 4.1 原计划（P3-M1 ~ P3-M10）

| ID | 任务 | 类别 | 状态 |
|---|---|---|---|
| **P3-M1** | R1 图片圆角 8px | B | ✅ 已完成 |
| **P3-M2** | R2 全局三栏 + 1600px 加宽 | B | ✅ 已完成 |
| **P3-M3** | R5 UnifiedSidebar + 三页接入 | B | ✅ 已完成 |
| **P3-M4** | R3.1 announcement 表 + Entity + Mapper | C | ✅ 已完成 |
| **P3-M5** | R3.2 AnnouncementService + 单测 | C | ✅ 已完成 |
| **P3-M6** | R3.3 AnnouncementController | C | ✅ 已完成 |
| **P3-M7** | R3.4 公告前端组件 + Admin 页 | C | ✅ 已完成 |
| **P3-M8** | R4 PostDetail 三栏 | B | ✅ 已完成 |
| **P3-M9** | R6 公告预览 + 日期 | B | ✅ 已完成 |
| **P3-M10** | R7 板块删除 | C | ✅ 已完成 |

### 4.2 执行期热修（H1 ~ H7）

| ID | 任务 | 要点 | 主要文件 | 状态 |
|---|---|---|---|---|
| **H1** | 公告修复与增强 | 普通用户只读详情；API `res.records`；content **1000 字** | `AnnouncementDetailDialog.vue`、`AnnouncementService.java`、`richText.js` | ✅ |
| **H2** | 帖子图片展示规则 | `variant`；单图/多图布局；flex 紧挨 | `PostImageGrid.vue`、`PostDetail.vue` | ✅ |
| **H3** | 前台帖子置顶 | 吧主本吧 / 管理员全站；详情页按钮 | `PostService.pinPost`、`PostController`、`PostDetail.vue` | ✅ |
| **H4** | 编辑帖与发帖统一 | 删除 `PostEdit.vue`；`openForEdit` | `PostEditorModal.vue`、`postEditor.js` | ✅ |
| **H5** | 评论修复 + 贴吧 UI | 补 `CommentForm` import；两层；展开回复 | `CommentTree.vue`、`CommentItem.vue`、`PostDetail.vue` | ✅ |
| **H6** | 个人中心弹窗 | 替代独立设置页 | `SettingsModal.vue`、`userSettings.js` | ✅ |
| **H7** | 修改密码 | 入口 + 二级弹窗 | `ChangePasswordModal.vue`、`PUT /api/users/password` | ✅ |

---

## 五、冲突自检清单

- [x] announcement 表名 — 与现有表无重复 ✅
- [x] `/api/announcements`、`/api/admin/announcements` — 路径无重复 ✅
- [x] `PUT /api/users/password`、`POST /api/posts/{id}/pin` — 与现有路径无冲突 ✅
- [x] 组件命名无冲突 ✅
- [x] B 类扩展不破坏一期接口语义 ✅

---

## 六、二期 → 三期 覆盖映射

| 二期任务 | 覆盖状态 | 三期对应 |
|---|---|---|
| BoardSidebar / AppSidebar | 重构合并 | **P3-M3** UnifiedSidebar |
| P2-M4 贴吧风 UI | 沿用 + 微调 | P3-M1、P3-M2、H2 |
| PostDetail 单栏 | 三栏 | **P3-M8** |
| **P2-M8 吧主本吧置顶** | 二期标注暂缓 | **H3 已在前台落地**（删帖/加精仍暂缓） |
| P2-M6 发帖弹窗 | 沿用 | H4 扩展为编辑共用 |

---

## 七、用户已拍板的决策点（2026-05-24 确认）

| # | 决策 | 选择 | 备注 |
|---|---|---|---|
| 1 | 图片圆角值 | **8px** | — |
| 2 | 中列宽度策略 | **去掉中列 max-width，flex 自适应** | 外层 **1600px**（见 §十） |
| 3 | 公告权限判断 | **Service 层手动校验** | — |
| 4 | 吧主公告入口 | 板块页公告 widget + 吧主点击管理 | 已落地 |
| 5 | 个人中心 | **弹窗 SettingsModal** | ~~独立 /settings 页~~ 已废弃；`/settings` 仅兼容 |
| 6 | 执行顺序 | P3-M1 → M10 + 热修 | 逐模块 commit |
| 7 | 公告正文格式 | HTML 存储 + v-html / richText 展示 | — |
| 8 | 公告数量上限 | 分页，不限条数 | — |
| 9 | PostDetail 右栏 | 仅板块公告 | — |
| 10 | Home 右栏 | 仅站点公告 | — |
| 11 | 公告 title 上限 | **50 字** | — |
| 12 | 公告 content 上限 | **1000 字** | ~~500 字~~ 热修 H1 调整 |

---

## 八、风险与注意事项

- **公告独立表**：便于后续扩展，避免 board 字段膨胀。
- **三栏移动端**：768px 以下隐藏侧栏，中栏全宽。
- **吧主越权**：置顶、公告、删板块等须校验 `owner_user_id`。
- **评论点赞/等级/IP 属地**：贴吧截图中的能力**未实现**（H5 仅 UI 与两层回复）。
- **测试数据清理**：`db/cleanup-test-data.sql` 仅开发用，见 README / CLAUDE.md。

---

## 九、执行期追加交付说明（H1–H7）

### H1 — 公告系统修复与增强

- 普通用户点击板块公告 → 只读 `AnnouncementDetailDialog`（吧主/管理员仍进编辑）
- `AnnouncementSidebar` 列表解析 `res?.records`（非 `res.data.records`）
- `append-to-body` 避免弹窗被裁剪
- 正文字数：前后端 **1000** 字

**验收**：未登录可浏览公告详情；超长正文弹窗内滚动。

### H2 — 帖子图片展示

- `PostImageGrid`：`variant="list"`（列表）/ `"detail"`（详情）
- 单图（=1）：原比例，最大宽 ≈ 容器 1/3
- 多图（>1）：1:1 方格横排，最多 3 张，flex 左对齐 gap 4px
- 详情正文内嵌 `<img>`：`max-width: calc(100% / 3)`

### H3 — 吧主/管理员前台置顶

- `PostService.pinPost(postId, pin, operatorId, isAdmin)`：非 admin 须为帖子所在吧吧主
- `POST /api/posts/{id}/pin` body `{ pinned: true|false }`
- `GET /api/posts/{id}` 的 `board.ownerUserId` 供前端判断
- `PostDetail`：置顶 / 取消置顶按钮；后台 `AdminPosts` 逻辑不变

**验收**：吧主仅本吧可置顶；其他吧无按钮；强行调 API 返回 1002。

### H4 — 编辑帖子与发帖统一

- `postEditor.openForEdit(post, onSaved)` 预填数据
- `PostEditorModal`：Tab「编辑」、按钮「保存」、`toPlainTextPreview` 回填正文
- `/post/:id/edit` → 详情页 + 自动打开编辑弹窗

### H5 — 评论修复与贴吧式 UI

- **Bug**：`PostDetail` 未 import `CommentForm` 导致评论区不可用 → 已修复
- 一级评论大头像（40px）；楼中楼小头像（32px）+ 灰底区块
- 二级文案：`回复 {nickname} : {content}`
- 底部：相对时间 + 回复 + ··· 删除
- 回复超过 2 条：**展开 N 条回复** / 收起

**未做**：评论点赞、用户等级、IP 属地。

### H6 — 个人中心弹窗

- `App.vue` 挂载 `SettingsModal`
- 顶栏下拉、左侧栏「个人中心」→ `userSettings.open()`
- 资料：头像、昵称、简介

### H7 — 修改密码

- `PUT /api/users/password`：`oldPassword` + `newPassword`（6–50 位，含字母数字）
- 个人中心「账号安全」→ `ChangePasswordModal` 二级弹窗
- 改密后**保持当前登录态**（不踢 Redis token）

---

## 十、与原文档差异说明

| 项 | 原文档 | 实际落地 |
|---|---|---|
| App 最大宽度 | 1200px 保持 | **1600px**（`global.css`） |
| 三栏 CSS | 各页分散 | **抽离 `global.css`** |
| 个人中心 | `/settings` 页面 | **弹窗** + `/settings` 重定向 |
| 公告 content 上限 | 500 字 | **1000 字** |
| PostImageGrid | 仅改圆角 | **variant + 布局规则**（H2） |
| P2-M8 吧主置顶 | 暂缓 | **H3 已完成** |
| PostEdit 独立页 | 一期遗留 | **已删除**，合并发帖弹窗 |

---

## 十一、三期新增/扩展 API 汇总

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/api/announcements` | 站点公告列表 | 公开 |
| GET | `/api/boards/{id}/announcements` | 板块公告列表 | 公开 |
| POST | `/api/boards/{id}/announcements` | 发板块公告 | 登录 + 吧主/admin |
| PUT/DELETE | `/api/announcements/{id}` | 编辑/删公告 | 登录 + 权限 |
| GET/POST/PUT/DELETE | `/api/admin/announcements` | 后台公告 CRUD | admin |
| DELETE | `/api/boards/{id}` | 软删板块 | 吧主/admin |
| POST | `/api/posts/{id}/pin` | 置顶切换 | 登录 + 吧主本吧或 admin |
| PUT | `/api/users/password` | 修改密码 | 登录 + 原密码校验 |
| GET | `/api/posts/{id}` | 帖子详情 | 响应含 **`board.ownerUserId`**（H3） |

后台置顶仍用：`POST /api/admin/posts/{id}/pin`。

---

## 十二、验收清单（文档与交付对照）

- [x] R1–R7 各模块
- [x] 公告 1000 字、只读详情、点击行为
- [x] 布局 1600px + `global.css` 三栏
- [x] PostImageGrid 列表/详情规则
- [x] 前台置顶（吧主/管理员）
- [x] 编辑帖子弹窗
- [x] 评论可用 + 贴吧式 UI
- [x] 个人中心弹窗 + 改密二级弹窗
- [x] 新增 API 已上线
- [x] 二期 P2-M8 置顶项 closure 说明

---

## 十三、交叉引用

- 项目总览与运行方式：`README.md`（含三期完成摘要）
- 开发陷阱（MySQL 字符集、Redis、FULLTEXT 等）：`CLAUDE.md`
- 测试数据清理脚本：`forum-server/src/main/resources/db/cleanup-test-data.sql`（仅开发环境）

---

> **维护说明**：后续若仅做小改动，请同步更新 §四 状态表、§九 热修说明及 §十二 验收项，避免与代码漂移。
