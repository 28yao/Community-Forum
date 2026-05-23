# 社区论坛系统 — 二期技术方案（plan.md）

> **版本**：v2.0
> **依赖**：`prd.md`（必读）、一期 `CLAUDE.md`、一期 `todo.md`、一期 `schema.sql`
> **基线**：一期 v1.0 已完成（含 M2 版块、M3 帖子、M5 搜索、M6 后台）

---

## 一、技术栈（沿用一期）

不变。二期新增前端依赖：
- `vue3-emoji-picker`（表情面板）
- 可选 `@vueuse/core` 的 `useDebounce`（若需实时搜索）

后端不新增依赖。

## 二、数据库变更

### 2.1 新建迁移脚本 `schema.v2.sql`

放置于 `forum-server/src/main/resources/db/schema.v2.sql`，**只追加变更**，不重写一期表。

```sql
-- ========== P2-M1: 扩展一期 board 表 ==========
ALTER TABLE board
  ADD COLUMN icon          VARCHAR(255) DEFAULT NULL COMMENT '板块头像URL',
  ADD COLUMN slogan        VARCHAR(30)  DEFAULT NULL COMMENT '口号',
  ADD COLUMN tags          VARCHAR(60)  DEFAULT NULL COMMENT '标签，逗号分隔（最多3个）',
  ADD COLUMN owner_user_id BIGINT       NOT NULL DEFAULT 1 COMMENT '吧主用户ID（默认系统管理员）',
  ADD COLUMN follower_count INT          NOT NULL DEFAULT 0 COMMENT '关注数（冗余）';

ALTER TABLE board ADD KEY idx_owner (owner_user_id);
ALTER TABLE board ADD KEY idx_followers (status, follower_count DESC);

-- ========== P2-M2: 板块申请表 ==========
CREATE TABLE IF NOT EXISTS board_application (
  id                BIGINT       NOT NULL AUTO_INCREMENT,
  applicant_user_id BIGINT       NOT NULL                            COMMENT '申请人',
  name              VARCHAR(50)  NOT NULL                            COMMENT '申请板块名（对齐 board.name 长度）',
  description       VARCHAR(200) NOT NULL                            COMMENT '描述',
  icon              VARCHAR(255) DEFAULT NULL,
  slogan            VARCHAR(30)  DEFAULT NULL,
  tags              VARCHAR(60)  DEFAULT NULL,
  status            TINYINT      NOT NULL DEFAULT 1                  COMMENT '1=待审核 2=通过 3=驳回',
  reject_reason     VARCHAR(200) DEFAULT NULL,
  reviewer_admin_id BIGINT       DEFAULT NULL,
  reviewed_at       DATETIME     DEFAULT NULL,
  board_id          BIGINT       DEFAULT NULL                        COMMENT '审核通过后回写',
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_applicant (applicant_user_id, created_at DESC),
  KEY idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块申请记录';

-- ========== P2-M3: 用户-板块关注关系 ==========
CREATE TABLE IF NOT EXISTS user_board_follow (
  user_id    BIGINT   NOT NULL,
  board_id   BIGINT   NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, board_id),
  KEY idx_board (board_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注板块';

-- ========== P2-M7: 标题专用 FULLTEXT 索引 ==========
ALTER TABLE post ADD FULLTEXT INDEX idx_title (title) WITH PARSER ngram;
```

### 2.2 一期种子数据兼容

一期已通过 M2-T6 / M0-T4b 插入 3 个版块。ALTER 后 `owner_user_id` 因 `DEFAULT 1` 自动落上系统管理员（user_id=1）。

**无需额外迁移脚本**，前提是一期 user_id=1 是系统管理员。如不是，需 `UPDATE board SET owner_user_id={admin_id}` 单独修。

### 2.3 上线检查 SQL

```sql
SHOW CREATE TABLE post\G    -- 确认 idx_search 和 idx_title 都带 WITH PARSER ngram
SHOW CREATE TABLE board\G   -- 确认新字段已落上
SELECT id, name, owner_user_id FROM board;  -- 确认 owner_user_id 不为 NULL
```

## 三、后端实现要点

### 3.1 包结构（在一期基础上扩展）

新增文件：
```
src/main/java/com/forum/
├── controller/
│   ├── BoardApplicationController.java     [P2-M2 用户端]
│   ├── BoardFollowController.java          [P2-M3]
│   ├── FeedController.java                 [P2-M5]  注：feed 也可挂在 PostController 上
│   └── admin/
│       └── AdminBoardApplicationController.java  [P2-M2 管理端]
├── service/
│   ├── BoardApplicationService.java + impl
│   ├── BoardFollowService.java + impl
│   ├── FeedService.java + impl
│   └── BoardService.java                   [P2-M1 修改：增加 update/recommended 等方法]
├── mapper/
│   ├── BoardApplicationMapper.java
│   ├── UserBoardFollowMapper.java
│   ├── BoardMapper.java                    [P2-M1 修改：增加新字段查询/排序方法]
│   └── PostMapper.java                     [P2-M5/M7 修改：增加 feed 查询 + scope=title 查询]
├── entity/
│   ├── BoardApplication.java
│   ├── UserBoardFollow.java
│   └── Board.java                          [修改：加新字段]
├── dto/
│   ├── BoardApplicationSubmitRequest.java
│   ├── BoardApplicationReviewRequest.java
│   ├── BoardUpdateRequest.java
│   ├── FeedItemVO.java
│   └── EligibilityVO.java
```

不动一期已有 Controller/Service 文件的核心方法，只新增方法。

### 3.2 关键 Service 逻辑

#### 3.2.1 资格校验 `BoardApplicationService.checkEligibility(userId)`

```java
EligibilityVO vo = new EligibilityVO();
User u = userMapper.selectById(userId);
long days = ChronoUnit.DAYS.between(u.getCreatedAt(), LocalDateTime.now());
int postCount = postMapper.countByUserAndStatus(userId, 1);
long pending = boardApplicationMapper.countByUserAndStatus(userId, 1);

vo.setRegisteredDays(days);
vo.setPostCount(postCount);
vo.setHasPending(pending > 0);
vo.setEligible(days >= 3 && postCount >= 3 && pending == 0);
return vo;
```

#### 3.2.2 提交申请 `BoardApplicationService.submit(userId, dto)`

```
1. checkEligibility(userId).isEligible() == true，否则抛 403
2. 名称冲突：SELECT FROM board WHERE name=? AND status=1 → 抛 409
3. 内容校验（DTO @Valid）
4. INSERT board_application (status=1)
5. return application.id
```

#### 3.2.3 审核通过 `BoardApplicationService.approve(adminId, applicationId)`

```
@Transactional
1. SELECT FOR UPDATE board_application WHERE id=? AND status=1
2. 二次名称冲突检查（防止两个待审核同名同时通过）
3. INSERT board (name, description, icon, slogan, tags,
                  owner_user_id=申请人, sort_weight=0, status=1,
                  post_count=0, follower_count=1)
4. UPDATE board_application SET status=2, board_id=新id,
                                  reviewer_admin_id=adminId, reviewed_at=NOW()
5. INSERT user_board_follow (申请人, 新id)
```

#### 3.2.4 关注/取关 `BoardFollowService.follow / unfollow`

```
follow:
  @Transactional
  1. 检查 follower count of user < 50
  2. INSERT IGNORE user_board_follow
  3. 如果实际插入了（affectedRows == 1）：UPDATE board SET follower_count = follower_count + 1
  4. 返回幂等成功

unfollow:
  @Transactional
  1. DELETE user_board_follow WHERE user_id=? AND board_id=?
  2. 如果 affectedRows == 1：UPDATE board SET follower_count = follower_count - 1
```

#### 3.2.5 混合信息流 `FeedService.getFeed(userId, page, size)`

```
if (userId == null) {
    return getHotPosts(page, size);
}
List<Long> followedBoardIds = userBoardFollowMapper.selectBoardIdsByUserId(userId);
if (followedBoardIds.isEmpty()) {
    return getHotPosts(page, size);
}

int followedCount = (int) Math.round(size * 0.7);
int hotCount = size - followedCount;

List<Post> followedPosts = postMapper.selectByBoardIdsOrderByCreatedAt(
    followedBoardIds, page, followedCount);
List<Post> hotPosts = postMapper.selectHotPosts(page, hotCount, /*excludeIds*/);

return merge(followedPosts, hotPosts);  // 简单拼接即可，前端无感
```

#### 3.2.6 热度排序 SQL

```sql
SELECT *, (
  (like_count * 3 + comment_count * 2 + view_count * 0.1) *
  POWER(0.5, TIMESTAMPDIFF(HOUR, created_at, NOW()) / 72.0)
) AS hot_score
FROM post
WHERE status = 1 AND deleted = 0
  AND id NOT IN (...)  -- 去重
ORDER BY hot_score DESC
LIMIT ?, ?
```

### 3.3 权限与拦截器（沿用一期）

- **登录态**：`/api/board-applications/**`、`/api/boards/*/follow` 等需登录，加入一期 AuthInterceptor 白名单的 inverse（一期默认拦截 `/api/**` 除非显式放行）
- **管理员**：`/api/admin/**` 沿用一期 AdminInterceptor
- **吧主**：不引入拦截器，在具体接口 Service 层动态判断：
  ```java
  Long uid = SecurityContext.currentUserId();
  Board board = boardMapper.selectById(boardId);
  boolean canManage = uid.equals(board.getOwnerUserId()) || userService.isAdmin(uid);
  if (!canManage) throw new BusinessException(1002, "无权限");
  ```

### 3.4 限流（沿用一期 RateLimitService）

新增限流规则：
- `POST /api/board-applications` → 60s 内一次/用户（防滥用）
- `POST /api/boards/{id}/follow` → 不限流（幂等且廉价）

## 四、前端实现要点

### 4.1 目录结构（在一期基础上扩展）

新增文件：
```
forum-web/src/
├── api/
│   ├── boardApplication.js          [P2-M2]
│   ├── boardFollow.js               [P2-M3]
│   ├── feed.js                      [P2-M5]
│   └── search.js                    [P2-M7 修改：加 type/scope 参数]
├── layouts/
│   └── TiebaLayout.vue              [P2-M4 新增]
├── components/
│   ├── layout/
│   │   ├── BoardSidebar.vue         [P2-M4 替代/包装一期 AppSidebar.vue]
│   │   └── AppHeader.vue            [修改：发帖按钮触发 Modal；搜索框不动]
│   ├── board/
│   │   ├── BoardCard.vue            [P2-M4 新增]
│   │   ├── BoardHeader.vue          [P2-M4 板块页头]
│   │   ├── BoardFollowButton.vue    [P2-M3]
│   │   └── BoardSelectDropdown.vue  [P2-M6 发帖 Modal 用]
│   ├── post/
│   │   ├── PostCard.vue             [P2-M4 重构 — 覆盖一期 M3-T20]
│   │   ├── PostImageGrid.vue        [P2-M4 1/2/3 自适应]
│   │   └── PostFeed.vue             [P2-M5 信息流容器]
│   ├── post-editor/
│   │   ├── PostEditorModal.vue      [P2-M6 新增]
│   │   └── EmojiPicker.vue          [P2-M6 包装 vue3-emoji-picker]
│   ├── search/
│   │   ├── SearchResultTabs.vue     [P2-M7]
│   │   ├── SearchResultBoard.vue
│   │   ├── SearchResultPost.vue
│   │   └── SearchResultUser.vue
│   └── board-application/
│       ├── ApplicationForm.vue      [P2-M2 用户端]
│       ├── ApplicationList.vue
│       └── EligibilityHint.vue
├── views/
│   ├── Home.vue                     [P2-M4 重写 — 覆盖一期 M3-T28]
│   ├── Board.vue                    [P2-M4 套贴吧风样式，不重写]
│   ├── PostDetail.vue               [P2-M4 加面包屑 + 图片大显]
│   ├── Search.vue                   [P2-M7 重构 Tab — 覆盖一期 M5-T7]
│   ├── user/
│   │   └── MyApplications.vue       [P2-M2 个人中心新增]
│   └── admin/
│       └── BoardApplicationReview.vue  [P2-M2 管理端]
├── stores/
│   ├── boardFollow.js               [P2-M3 缓存关注列表]
│   └── postEditor.js                [P2-M6 控制 Modal 开关]
└── router/index.js                  [增加新路由]
```

### 4.2 关键交互

#### 4.2.1 发帖弹窗的全局控制

```js
// stores/postEditor.js
export const usePostEditorStore = defineStore('postEditor', {
  state: () => ({ visible: false, lockedBoardId: null }),
  actions: {
    open({ boardId } = {}) {
      this.visible = true;
      this.lockedBoardId = boardId || null;
    },
    close() {
      this.visible = false;
      this.lockedBoardId = null;
    },
  },
});

// App.vue
<PostEditorModal />   // 全局挂载，由 store 控制开关

// 顶部按钮
usePostEditorStore().open();

// 板块页按钮
usePostEditorStore().open({ boardId: route.params.id });
```

#### 4.2.2 图片网格自适应规则

```vue
<!-- PostImageGrid.vue -->
<template>
  <div :class="['grid', `grid-${displayCount}`]">
    <img v-for="(url, idx) in displayUrls" :key="idx" :src="url" />
    <div v-if="hasMore" class="more-overlay">+{{ images.length - 3 }}</div>
  </div>
</template>

<style>
.grid-1 { grid-template-columns: 1fr; max-height: 400px; }
.grid-2 { grid-template-columns: 1fr 1fr; aspect-ratio: 2/1; }
.grid-3 { grid-template-columns: repeat(3, 1fr); aspect-ratio: 3/1; }
img { width: 100%; height: 100%; object-fit: cover; border-radius: 8px; }
</style>
```

#### 4.2.3 关注按钮乐观更新

```vue
const toggleFollow = async () => {
  const wasFollowed = isFollowed.value;
  isFollowed.value = !wasFollowed;   // 立即切换 UI
  followerCount.value += wasFollowed ? -1 : 1;
  try {
    if (wasFollowed) await unfollowApi(boardId);
    else await followApi(boardId);
  } catch (e) {
    isFollowed.value = wasFollowed;   // 回滚
    followerCount.value += wasFollowed ? 1 : -1;
    ElMessage.error('操作失败');
  }
};
```

### 4.3 兼容一期的注意点

- 一期 `/post/create` 路由保留作为后备（直接路径访问仍可用）
- 一期 `AppHeader.vue` 的搜索框 / 用户区不动，只改发帖按钮的点击行为
- 一期 `Search.vue` 现有 URL `/search?q=xxx` 兼容，新增 `type` 参数缺省时仍按帖子搜索
- 一期 `PostCard.vue` 的 props 接口尽量兼容（如能复用就复用，差距过大就独立组件）
- 一期 ImageUpload.vue（9 张上限 + 5MB）在 Modal 中复用

## 五、迁移与回归测试

### 5.1 部署步骤

1. `git pull` 二期分支
2. 执行 `schema.v2.sql`（顺序：ALTER board → CREATE board_application → CREATE user_board_follow → ALTER post idx_title）
3. 跑上线检查 SQL（§2.3）
4. 后端 `mvn clean install && mvn spring-boot:run`
5. 前端 `npm install`（新增 vue3-emoji-picker）→ `npm run build`
6. 烟测：登录、发帖（顶部按钮 / 板块页按钮）、关注板块、申请板块、管理员审核

### 5.2 必跑的一期回归

| 一期模块 | 二期影响 | 必测项 |
|---|---|---|
| M1 用户系统 | 无 | 登录注册不变 |
| M2 版块系统 | board 表加字段 | 一期版块列表/详情接口返回数据多了字段（前端兼容） |
| M3 帖子模块 | UI 重构 | 发帖（Modal + 老路径都通）、列表、详情、编辑、删除 |
| M4 互动 | 无 | 点赞、评论、收藏 |
| M5 搜索 | 接口扩展 | `/api/search?q=xxx` 不传 type/scope 仍返回帖子列表（兼容） |
| M6 后台 | 加审核 Tab | 一期已有的用户/版块/帖子管理不变 |
| M7 限流 | 加申请限流 | 一期发帖/评论/搜索限流不变 |

### 5.3 二期新功能测试

每模块见 `todo.md` 的"页面测试 / 接口测试 / 异常测试"段。

## 六、实施顺序

```
依赖关系图：

P2-M1 (board ALTER) ─┬─→ P2-M2 (申请审核) ──→ todo 内独立完整测试
                     ├─→ P2-M3 (关注)
                     └─→ P2-M4 (UI 重构) ─┬─→ P2-M5 (feed)
                                          ├─→ P2-M6 (发帖 Modal)
                                          ├─→ P2-M7 (搜索)
                                          └─→ P2-M8 (吧主)
```

建议顺序：M1 → M3 (关注，feed 依赖它) → M2 (审核独立) ∥ M4 (UI) → M5 (feed) → M6 (Modal) → M7 (搜索) → M8 (吧主)

每个模块 PR 单独提交，commit 前缀 `[phase2]`。合并前自检：todo.md 该模块所有任务 ✓、单元测试通过、手工跑通页面测试、不破坏一期回归。

## 七、风险与缓解

| 风险 | 缓解 |
|---|---|
| 一期 ngram parser 在新环境丢失 | 部署后必跑 `SHOW CREATE TABLE post\G` |
| board 表 ALTER 时锁表 | 数据量小（< 10 行），瞬时完成，不影响生产 |
| 一期 PostCard 重构破坏现有版块帖子页 | 保留旧 PostCard 命名（或重构后版块帖子页同步切换） |
| 发帖 Modal 在 WangEditor 上有内存泄漏 | Modal 关闭时调用 editor.destroy() + v-if 强制重建 |
| feed 数据量大时慢 | 二期数据小不会触发；后续上 hot_score 字段升级方案 |
| 同时审核同名申请通过 | approve 内事务 SELECT FOR UPDATE + 二次名称冲突检查 |

## 八、对一期 todo.md 的覆盖映射

| 一期任务 | 覆盖状态 | 二期对应 |
|---|---|---|
| M2-T8 AppHeader.vue | **修改** | P2-M6（发帖按钮改触发 Modal） |
| M2-T9 AppSidebar.vue 版块导航 | **覆盖** | P2-M4 BoardSidebar.vue 三段重构 |
| M2-T11 Home.vue 首页布局 | **覆盖** | P2-M4 Home.vue 重写 |
| M3-T20 PostCard.vue | **覆盖** | P2-M4 PostCard.vue 贴吧风重构 |
| M3-T28 Home.vue 显示最新帖 | **覆盖** | P2-M5 feed 接口 + P2-M4 PostFeed.vue |
| M5-T7 Search.vue | **覆盖** | P2-M7 Search.vue Tab 化 |
| 其他一期任务 | 不动 | — |

二期 todo.md 中每个相关任务项会显式标注 `↳ 覆盖一期 M{x}-T{y}`。

---

*文档结束 — 下一步：编写 `phase2/todo.md` 按模块拆分原子任务*
