# 社区论坛系统 — 二期 PRD（产品需求文档）

> **版本**：v2.0
> **状态**：需求对齐完成，待评审
> **创建日期**：2026-05-23
> **基线**：一期 v1.0 已验收完成（M0~M7 全模块完成）
> **范围**：贴吧风 UI 改版 + 板块申请审核 + 板块关注 + 搜索增强 + 发帖弹窗

---

## ⚠️ 重要校正

**初稿曾误以为板块（版块）是全新领域，实际一期已完整实现 M2 版块系统、M3 帖子（含 board_id）、M5 搜索（含 FULLTEXT ngram）、M6 后台（含板块 CRUD）。二期是纯增量改造，所有改动均在一期基础上扩展，不重建任何已有表/接口/页面。**

## 一、二期目标

- 改造一期的"普通论坛"为"贴吧风社区"
- 引入**用户自治**机制：用户可申请创建板块，管理员审核
- 引入**社交关系**：用户可关注板块，首页按关注定制信息流
- 改造 UI 为贴吧风：左栏导航 + 大图卡片 + 发帖弹窗 + 搜索增强

## 二、一期已有能力盘点（不动）

| 一期模块 | 状态 | 二期复用 |
|---|---|---|
| 用户系统（注册/登录/资料） | ✅ 已完成 | 复用 |
| **板块（version 表）** | ✅ 已完成 CRUD + 列表/详情接口 | **扩展字段** |
| 帖子（发帖/列表/详情/编辑/删除） | ✅ 已完成，post.board_id 已存在 | **改造 UI + 接口扩展** |
| 评论、点赞、收藏 | ✅ 已完成 | 不动 |
| 搜索（FULLTEXT ngram） | ✅ 已完成 | **扩展范围 + UI** |
| 图片上传 | ✅ 已完成 | 复用 |
| 后台管理（用户/版块/帖子/评论 CRUD） | ✅ 已完成 | **新增审核 Tab** |
| Redis 限流 | ✅ 已完成 | 复用（板块申请加限流） |
| WangEditor 富文本 | ✅ 已完成 | 复用（放入 Modal） |

## 三、对一期的破坏性影响（最小化后）

| 项 | 一期 | 二期 | 处理 |
|---|---|---|---|
| `board` 表字段 | name / description / sort_weight / status / post_count | + icon / slogan / tags / owner_user_id / follower_count | **ALTER TABLE 增量加列**，已有数据不变 |
| 一期 3 个种子版块的 `owner_user_id` | 不存在 | 必填 | 数据迁移：全部设为**系统管理员 user_id=1** |
| `post.board_id` | 已存在 NOT NULL | 不变 | 无 |
| 首页 `Home.vue` | 全站最新帖列表（M3-T28） | 贴吧风布局 + 混合信息流 | **覆盖一期 M3-T28** |
| 发帖入口 | `/post/create` 独立页（M3-T25） | Modal 弹窗 | **保留独立页**作为后备，新增 Modal 作为主入口 |
| 搜索接口 `/api/search` | 仅 title+content（M5） | 新增 `scope` 参数 + `type` 参数（板块/帖子/用户） | **向后兼容**：无参数行为不变 |
| 搜索结果页 `/search` | 单一帖子列表（M5-T7） | Tab 切换（板块/帖子/用户） | 重构 `Search.vue` |
| 帖子详情 `PostDetail.vue` | 已有 | 加面包屑 + 图片大显 | 轻改 |
| 版块名唯一性 | UNIQUE(name) | 同 | 二期不动唯一性约束（关闭=禁用，名字仍占位） |

## 四、二期功能模块概览（在一期基础上扩展）

```
P2-M1  板块字段扩展 + 数据迁移         (ALTER board + 种子数据补 owner_user_id)
P2-M2  板块申请审核                   (board_application 表 + 用户提交 + 管理员审核)
P2-M3  板块关注                       (user_board_follow 表 + Follow API + 左栏接口)
P2-M4  贴吧风 UI 重构                 (TiebaLayout + 左栏 + Home 重写 + PostCard 卡片重构)
P2-M5  混合信息流 feed 接口            (新增 /api/posts/feed，不替换原有 /api/posts)
P2-M6  发帖弹窗与表情面板             (PostEditorModal + emoji picker)
P2-M7  搜索增强                       (用户搜 + 板块搜 + scope=title + Tab UI)
P2-M8  板块管理增强                   (吧主可改信息 + 板块详情接口扩展)
```

依赖关系：M1→M2/M3 并行 → M4→M5/M6/M7 并行 → M8 可与 M4~M7 并行。

## 五、详细需求

### 5.1 P2-M1 板块字段扩展 + 数据迁移

**目标**：在不动一期已有数据的前提下，补齐二期需要的字段。

**Schema 变更**：
```sql
ALTER TABLE board
  ADD COLUMN icon VARCHAR(255) DEFAULT NULL COMMENT '板块头像URL',
  ADD COLUMN slogan VARCHAR(30) DEFAULT NULL COMMENT '口号',
  ADD COLUMN tags VARCHAR(60) DEFAULT NULL COMMENT '标签，逗号分隔',
  ADD COLUMN owner_user_id BIGINT NOT NULL DEFAULT 1 COMMENT '吧主用户ID（默认系统管理员）',
  ADD COLUMN follower_count INT NOT NULL DEFAULT 0 COMMENT '关注数（冗余）';

-- 一期 3 个种子版块的 owner_user_id 已经因 DEFAULT 1 落上，无需单独 UPDATE
```

**注意**：
- 二期**不动** name 的 UNIQUE 约束（驳回的板块名仍占位，避免一期已有板块被重名抢占）
- 二期**不引入** status=2 关闭状态，沿用一期 status=0（禁用）/ status=1（启用）

### 5.2 P2-M2 板块申请审核

**目标**：用户申请创建板块，管理员审核通过后**调用一期已有的 BoardService.create**（如果一期有）或新增一个内部方法。

**实体**：`board_application` 新表（字段同初稿设计）

**资格门槛** `[默认值-可调]`：
- 注册时长 ≥ 3 天
- 已发帖数 ≥ 3（status=1 的 post）
- 当前无"待审核"状态的申请

**用户端接口**：
- `GET /api/board-applications/eligibility`
- `POST /api/board-applications`
- `GET /api/board-applications/mine`

**管理端接口**（一期已有 `/api/admin/...` 前缀和 AdminInterceptor）：
- `GET /api/admin/board-applications?status=&page=`
- `POST /api/admin/board-applications/{id}/approve`
- `POST /api/admin/board-applications/{id}/reject`

**审核通过事务**：
```
1. SELECT FOR UPDATE board_application WHERE id=? AND status=1
2. 名称冲突检查：SELECT FROM board WHERE name=? AND status=1（兼容一期 UNIQUE）
3. INSERT board (name, description, icon, slogan, tags, owner_user_id=applicant_user_id, sort_weight=0, status=1, post_count=0, follower_count=1)
4. UPDATE board_application SET status=2, board_id=新id, reviewer_admin_id=?, reviewed_at=NOW()
5. INSERT user_board_follow (applicant_user_id, 新board_id)
```

**用户端界面**：
- 个人中心新增"我的板块申请"Tab（一期已有个人中心，加 Tab）
- 申请表单字段：name（5-20）/ description（10-100）/ icon（可选）/ slogan（可选 1-30）/ tags（可选最多3个，每个1-10）

**管理端界面**：
- 一期管理后台左侧菜单加"板块申请"
- 列表（待审核优先）+ 详情面板 + 通过/驳回操作

### 5.3 P2-M3 板块关注

**目标**：用户可关注板块，首页和左栏体现关注列表。

**实体**：`user_board_follow(user_id, board_id, created_at)` 复合主键

**接口**：
- `POST /api/boards/{id}/follow`（幂等）
- `DELETE /api/boards/{id}/follow`
- `GET /api/boards/followed`
- `GET /api/boards/recommended?limit=10`（热度排序：follower_count DESC + post_count DESC）

**业务规则**：
- 单用户最多关注 50 个板块 `[默认值-可调]`
- 事务内 +1/-1 维护 `board.follower_count`
- 已禁用板块（status=0）仍可见但 UI 标灰

### 5.4 P2-M4 贴吧风 UI 重构

**目标**：把一期 `Home.vue` + `AppSidebar.vue` 重构为贴吧风布局。

**改造范围**：
- `layouts/TiebaLayout.vue`（新增）：替换原默认布局
- `Home.vue`（重写）：左栏（三段：关注/推荐/全部） + 中间信息流（调用 /feed）
- `PostCard.vue`（重构）：贴吧风卡片，含图片网格 1/2/3 大图
- `Board.vue`（一期版块帖子页，轻改）：套贴吧风样式
- `PostDetail.vue`（轻改）：加面包屑（首页 > 板块名 > 帖子）+ 图片主体大显
- `AppHeader.vue`（修改）：搜索框、用户区不动；发帖按钮改为打开 Modal

**列表卡片渲染**：
- 顶部：板块名（链接到板块页）+ 作者头像 + 时间
- 中部：标题加粗 + 摘要前 120 字 + 图片网格
- 底部：点赞 / 评论 / 浏览数
- 图片：最多 3 张（详情页看全部），1 张全宽、2 张 1:1、3 张 1:1:1

### 5.5 P2-M5 混合信息流 feed 接口

**目标**：新增 `/api/posts/feed` 接口，**不删除**一期 `/api/posts`。

**接口**：`GET /api/posts/feed?page=&size=`
- 未登录态：全站 hot_score Top N
- 登录态且已关注 ≥1 板块：70% 关注板块最新 + 30% 全站热门（去重）
- 登录态但未关注：同未登录态

**hot_score 公式** `[默认值-可调]`：
```
score = (like_count×3 + comment_count×2 + view_count×0.1) × 0.5^((now - created_at) / 72h)
```

**实施**：方案 A（查询时实时算 + ORDER BY），方案 B（定时任务写 hot_score 字段）作为升级路径。二期选 A。

### 5.6 P2-M6 发帖弹窗与表情面板

**目标**：取代一期独立发帖页 `/post/create` 的主入口。一期的 `/post/create` 路由**保留**作为后备路径。

**入口**：
- 顶部"发帖"按钮 → 打开 Modal，板块下拉空、需手选
- 板块页"发帖"按钮 → 打开 Modal，板块下拉默认锁定为当前板块

**Modal 字段**：
- 顶部标题"发贴" + 当前用户头像 + 关闭按钮
- 「发布到吧」下拉（带搜索）
- 标题（5-31 字，placeholder："请输入完整贴子标题(5-31个字)"）
- 正文（沿用一期 PostEditor.vue 即 WangEditor 封装）
- 工具栏：表情 / 图片（图片复用一期 ImageUpload）
- 右下角"发布"主按钮

**砍掉**（YAGNI）：@用户 / #话题 / 展示范围 / 内容声明

**表情面板**：使用 `vue3-emoji-picker`，纯前端，插入到正文光标位置。

### 5.7 P2-M7 搜索增强

**目标**：扩展一期 `/api/search` 接口，**保持兼容**（无参数等同一期 type=post + scope=both）。

**接口扩展**：
- `GET /api/search?q=&type=board&page=&size=` 搜板块
- `GET /api/search?q=&type=post&scope=title|both&page=&size=` 搜帖子（scope 默认 both，兼容一期）
- `GET /api/search?q=&type=user&page=&size=` 搜用户

**实现**：
- 板块：LIKE on name / description / slogan / tags
- 帖子（scope=both）：复用一期 PostMapper.searchByKeyword（M5-T2）
- 帖子（scope=title）：新增 SQL，需新建 `FULLTEXT idx_title(title) WITH PARSER ngram`，或用 LIKE 兜底
- 用户：LIKE on username / nickname

**前端**：重写 `Search.vue`（M5-T7），加 Tab 切换（默认 type=post）。

**上线检查**：必须 `SHOW CREATE TABLE post\G` 确认两个 FULLTEXT 索引带 ngram parser。

### 5.8 P2-M8 板块管理增强

**吧主权限**：
- `PATCH /api/boards/{id}` 修改板块（仅 description / icon / slogan / tags，**name 不可改**）
- 本板块内：置顶/加精/删帖（一期已支持管理员，扩展为"管理员 OR 吧主"）

**管理员权限**（扩展一期 M6 已有的）：
- 修改 `board.owner_user_id`（吧主移交）
- 关闭板块（用一期已有的 status=0 即禁用，沿用一期路径）

## 六、`[默认值-可调]` 汇总

| 编号 | 项 | 默认值 |
|---|---|---|
| D1 | 申请门槛 | 注册 ≥3 天 + 发帖 ≥3 |
| D2 | 单用户关注上限 | 50 |
| D3 | 单帖图片数 | **沿用一期 9 张**（M3 已实现） |
| D4 | 驳回期间板块名 | 不锁定（一期 UNIQUE 仍生效，先到先得） |
| D5 | hot_score 公式 | 点赞×3 + 评论×2 + 浏览×0.1，半衰期 72h |
| D6 | 匿名用户权限 | 可读可搜，不可写（**沿用一期**） |
| D7 | 吧主可改字段 | description / icon / slogan / tags |
| D8 | 吧主退位/移交 | 二期不做（需管理员介入） |
| D9 | feed 混合比 | 70% 关注 + 30% 热门 |
| D10 | 推荐板块数 | Top 10 |

## 七、不在二期范围内（YAGNI）

- @用户 / #话题 / 站内通知 / 邮件
- 帖子展示范围 / AI 内容声明 / 原创声明
- 板块多级分类
- 吧主竞选 / 用户关注用户
- 评论楼层号 / 只看楼主
- feed 性能升级方案 B（定时任务写 hot_score）

## 八、影响一期任务追溯

二期 todo.md 中将显式标注以下覆盖关系：
- `M3-T28 首页直接展示全站最新帖列表` → 被 P2-M4 覆盖（首页重写）
- `M3-T20 PostCard 卡片` → 被 P2-M4 覆盖（卡片重构）
- `M5-T7 Search.vue` → 被 P2-M7 覆盖（Tab 化）
- `AppSidebar.vue M2-T9 版块导航` → 被 P2-M4 覆盖（重构为三段）
- `AppHeader.vue M2-T8` → 被 P2-M6 影响（发帖按钮改为打开 Modal）

其余一期任务不动。

---

*文档结束 — 见 plan.md 技术方案 与 todo.md 任务清单*
