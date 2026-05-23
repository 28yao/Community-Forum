# 社区论坛系统 — 技术方案 (plan.md)

> **关联 PRD**: `context/PRD-社区论坛系统.md` v1.1
> **日期**: 2026-05-23
> **范围**: 一期 MVP + 二期预留设计

---

## 1. 技术栈

| 层 | 技术 | 版本/说明 |
|----|------|-----------|
| 后端框架 | Spring Boot | 2.x |
| ORM | MyBatis-Plus | 3.x |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis | 用于 Token、限流、计数 |
| 前端框架 | Vue | 3.x + Vue Router + Pinia |
| UI 组件 | Element Plus | 与 Vue 3 配套 |
| 富文本编辑器 | WangEditor | 轻量、中文友好 |
| 构建工具 | Maven (后端) / Vite (前端) | — |
| JDK | 1.8 | 按项目环境要求 |

---

## 2. 项目结构

### 2.1 后端 `forum-server/`

```
forum-server/
├── src/main/java/com/forum/
│   ├── ForumApplication.java
│   ├── config/                    # 配置类
│   │   ├── WebMvcConfig.java      # 跨域、静态资源
│   │   ├── RedisConfig.java
│   │   ├── MybatisPlusConfig.java # 分页插件、自动填充
│   │   └── SecurityConfig.java    # 拦截器注册
│   ├── common/                    # 通用组件
│   │   ├── Result.java            # 统一响应体
│   │   ├── PageResult.java        # 分页响应
│   │   ├── Constants.java         # 常量
│   │   ├── ErrorCode.java         # 错误码枚举
│   │   └── exception/
│   │       ├── BizException.java  # 业务异常
│   │       └── GlobalExceptionHandler.java
│   ├── interceptor/               # 拦截器
│   │   ├── AuthInterceptor.java   # 用户鉴权
│   │   └── AdminInterceptor.java  # 管理员鉴权
│   ├── entity/                    # 数据库实体
│   │   ├── User.java
│   │   ├── Board.java
│   │   ├── Post.java
│   │   ├── Comment.java
│   │   ├── PostImage.java
│   │   ├── PostLike.java
│   │   ├── PostFavorite.java
│   │   └── VerificationToken.java
│   ├── mapper/                    # MyBatis Mapper
│   │   ├── UserMapper.java
│   │   ├── BoardMapper.java
│   │   ├── PostMapper.java
│   │   ├── CommentMapper.java
│   │   ├── PostImageMapper.java
│   │   ├── PostLikeMapper.java
│   │   ├── PostFavoriteMapper.java
│   │   └── VerificationTokenMapper.java
│   ├── service/                   # 业务逻辑
│   │   ├── UserService.java
│   │   ├── BoardService.java
│   │   ├── PostService.java
│   │   ├── CommentService.java
│   │   ├── FileService.java       # 图片上传
│   │   ├── MailService.java       # 邮件发送
│   │   └── SearchService.java
│   ├── controller/                # 前台接口
│   │   ├── AuthController.java    # 注册/登录
│   │   ├── UserController.java    # 用户资料
│   │   ├── BoardController.java   # 版块
│   │   ├── PostController.java    # 帖子
│   │   ├── CommentController.java # 评论
│   │   ├── LikeController.java    # 点赞
│   │   ├── FavoriteController.java# 收藏
│   │   └── SearchController.java  # 搜索
│   └── controller/admin/          # 后台接口
│       ├── AdminAuthController.java
│       ├── AdminUserController.java
│       ├── AdminBoardController.java
│       ├── AdminPostController.java
│       └── AdminCommentController.java
├── src/main/resources/
│   ├── application.yml
│   ├── mapper/                    # XML 映射文件（复杂查询用）
│   └── db/
│       └── schema.sql             # 建表脚本
└── pom.xml
```

### 2.2 前端 `forum-web/`

```
forum-web/
├── src/
│   ├── main.js
│   ├── App.vue
│   ├── router/
│   │   └── index.js               # 路由定义
│   ├── stores/                    # Pinia 状态管理
│   │   ├── user.js                # 用户登录态
│   │   └── app.js                 # 全局状态（版块列表等）
│   ├── api/                       # 接口封装
│   │   ├── request.js             # Axios 实例（拦截器）
│   │   ├── auth.js
│   │   ├── user.js
│   │   ├── board.js
│   │   ├── post.js
│   │   ├── comment.js
│   │   ├── like.js
│   │   ├── favorite.js
│   │   └── search.js
│   ├── views/                     # 页面
│   │   ├── Home.vue               # 首页
│   │   ├── Board.vue              # 版块帖子列表
│   │   ├── PostDetail.vue         # 帖子详情
│   │   ├── PostCreate.vue         # 发帖
│   │   ├── PostEdit.vue           # 编辑帖子
│   │   ├── Search.vue             # 搜索结果
│   │   ├── Login.vue              # 登录
│   │   ├── Register.vue           # 注册
│   │   ├── VerifyEmail.vue        # 邮箱验证
│   │   ├── UserProfile.vue        # 个人主页
│   │   ├── Settings.vue           # 编辑资料
│   │   └── NotFound.vue           # 404
│   ├── components/                # 通用组件
│   │   ├── layout/
│   │   │   ├── AppHeader.vue      # 顶部导航
│   │   │   ├── AppFooter.vue      # 页脚
│   │   │   └── AppSidebar.vue     # 侧边栏（版块列表）
│   │   ├── post/
│   │   │   ├── PostCard.vue       # 帖子卡片
│   │   │   ├── PostList.vue       # 帖子列表
│   │   │   └── PostEditor.vue     # 富文本编辑器封装
│   │   ├── comment/
│   │   │   ├── CommentTree.vue    # 树形评论
│   │   │   ├── CommentItem.vue    # 单条评论
│   │   │   └── CommentForm.vue    # 评论输入框
│   │   └── common/
│   │       ├── ImageUpload.vue    # 图片上传
│   │       ├── Pagination.vue     # 分页
│   │       └── EmptyState.vue     # 空状态
│   └── admin/                     # 后台管理页面
│       ├── AdminLogin.vue
│       ├── AdminLayout.vue        # 后台布局
│       ├── AdminDashboard.vue     # 仪表盘（二期）
│       ├── AdminUsers.vue         # 用户管理
│       ├── AdminPosts.vue         # 帖子管理
│       ├── AdminComments.vue      # 评论管理
│       └── AdminBoards.vue        # 版块管理
├── public/
├── vite.config.js
└── package.json
```

---

## 3. 数据库设计

### 3.1 ER 关系概览

```
user ──1:N──> post ──1:N──> comment
  │              │              │
  │              ├──1:N──> post_image
  │              ├──M:N──> user (post_like)
  │              └──M:N──> user (post_favorite)
  │
  └──1:N──> verification_token

board ──1:N──> post
```

### 3.2 表结构定义

#### 3.2.1 `user` 用户表

> PRD 追踪: §5.1.1 邮箱注册、§5.1.2 登录、§5.1.3 个人资料

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户ID |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 邮箱 |
| password | VARCHAR(100) | NOT NULL | 密码（bcrypt） |
| nickname | VARCHAR(20) | UNIQUE, NOT NULL | 昵称 |
| avatar | VARCHAR(255) | DEFAULT NULL | 头像URL |
| bio | VARCHAR(200) | DEFAULT '' | 个人简介 |
| role | VARCHAR(20) | DEFAULT 'user' | 角色: user / admin（预留 moderator） |
| status | TINYINT | DEFAULT 1 | 状态: 0=封禁, 1=正常 |
| ban_reason | VARCHAR(255) | DEFAULT NULL | 封禁原因 |
| nickname_updated_at | DATETIME | DEFAULT NULL | 昵称上次修改时间（30天冷却） |
| login_fail_count | INT | DEFAULT 0 | 连续登录失败次数 |
| locked_until | DATETIME | DEFAULT NULL | 锁定截止时间 |
| email_verified | TINYINT | DEFAULT 0 | 邮箱是否已验证 |
| created_at | DATETIME | NOT NULL | 注册时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT | DEFAULT 0 | 软删除标记 |

**索引**:
- `uk_email` UNIQUE (email)
- `uk_nickname` UNIQUE (nickname)
- `idx_status` (status)
- `idx_role` (role)

#### 3.2.2 `board` 版块表

> PRD 追踪: §5.2.1 版块管理

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 版块ID |
| name | VARCHAR(50) | UNIQUE, NOT NULL | 版块名称 |
| description | VARCHAR(200) | DEFAULT '' | 版块描述 |
| sort_weight | INT | DEFAULT 0 | 排序权重（越大越前） |
| status | TINYINT | DEFAULT 1 | 状态: 0=禁用, 1=启用 |
| post_count | INT | DEFAULT 0 | 帖子数量（冗余计数） |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT | DEFAULT 0 | 软删除标记 |

**索引**:
- `uk_name` UNIQUE (name)
- `idx_sort` (sort_weight DESC)

#### 3.2.3 `post` 帖子表

> PRD 追踪: §5.3.1 发布帖子、§5.3.2 帖子列表、§5.3.3 帖子详情

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 帖子ID |
| board_id | BIGINT | NOT NULL, FK | 所属版块 |
| user_id | BIGINT | NOT NULL, FK | 发帖用户 |
| title | VARCHAR(100) | NOT NULL | 标题 |
| content | TEXT | NOT NULL | 正文内容（富文本HTML） |
| is_anonymous | TINYINT | DEFAULT 0 | 是否匿名（二期，预留字段） |
| is_pinned | TINYINT | DEFAULT 0 | 是否置顶 |
| is_featured | TINYINT | DEFAULT 0 | 是否加精 |
| like_count | INT | DEFAULT 0 | 点赞数（冗余） |
| comment_count | INT | DEFAULT 0 | 评论数（冗余） |
| view_count | INT | DEFAULT 0 | 浏览数 |
| is_edited | TINYINT | DEFAULT 0 | 是否编辑过 |
| last_edited_at | DATETIME | DEFAULT NULL | 最后编辑时间 |
| status | TINYINT | DEFAULT 1 | 状态: 0=删除, 1=正常 |
| created_at | DATETIME | NOT NULL | 发帖时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT | DEFAULT 0 | 软删除标记 |

**索引**:
- `idx_board_id` (board_id, status, is_pinned DESC, created_at DESC) — 版块帖子列表查询
- `idx_user_id` (user_id, status, created_at DESC) — 用户帖子列表
- `idx_created_at` (created_at DESC) — 全局时间排序
- `FULLTEXT idx_search` (title, content) — 全文搜索

#### 3.2.4 `post_image` 帖子图片表

> PRD 追踪: §5.3.1 图片上传（最多9张）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 图片ID |
| post_id | BIGINT | NOT NULL, FK | 所属帖子 |
| url | VARCHAR(255) | NOT NULL | 图片URL |
| sort_order | INT | DEFAULT 0 | 排序 |
| created_at | DATETIME | NOT NULL | 上传时间 |

**索引**:
- `idx_post_id` (post_id)

#### 3.2.5 `comment` 评论表

> PRD 追踪: §5.4.2 评论与回复（树形，最多2层）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 评论ID |
| post_id | BIGINT | NOT NULL, FK | 所属帖子 |
| user_id | BIGINT | NOT NULL, FK | 评论用户 |
| parent_id | BIGINT | DEFAULT NULL | 父评论ID（NULL=顶级评论） |
| reply_to_user_id | BIGINT | DEFAULT NULL | 被回复的用户ID |
| content | VARCHAR(1000) | NOT NULL | 评论内容 |
| depth | TINYINT | DEFAULT 1 | 层级: 1/2 |
| status | TINYINT | DEFAULT 1 | 状态: 0=删除, 1=正常 |
| created_at | DATETIME | NOT NULL | 评论时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| deleted | TINYINT | DEFAULT 0 | 软删除标记 |

**索引**:
- `idx_post_id` (post_id, status, created_at ASC) — 帖子评论列表
- `idx_parent_id` (parent_id) — 子评论查询
- `idx_user_id` (user_id, status, created_at DESC) — 用户评论列表

#### 3.2.6 `post_like` 点赞表

> PRD 追踪: §5.4.1 点赞（幂等，同一用户对同一帖子只能点赞一次）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | ID |
| post_id | BIGINT | NOT NULL, FK | 帖子ID |
| user_id | BIGINT | NOT NULL, FK | 用户ID |
| created_at | DATETIME | NOT NULL | 点赞时间 |

**索引**:
- `uk_post_user` UNIQUE (post_id, user_id) — 保证幂等
- `idx_user_id` (user_id) — 用户点赞列表

#### 3.2.7 `post_favorite` 收藏表

> PRD 追踪: §5.4.3 收藏（幂等）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | ID |
| post_id | BIGINT | NOT NULL, FK | 帖子ID |
| user_id | BIGINT | NOT NULL, FK | 用户ID |
| created_at | DATETIME | NOT NULL | 收藏时间 |

**索引**:
- `uk_post_user` UNIQUE (post_id, user_id)
- `idx_user_id` (user_id) — 用户收藏列表

#### 3.2.8 `verification_token` 验证令牌表

> PRD 追踪: §5.1.1 邮箱验证、§5.1.2 登录 Token

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | ID |
| user_id | BIGINT | DEFAULT NULL, FK | 关联用户（注册验证时可能为空） |
| email | VARCHAR(100) | NOT NULL | 邮箱 |
| token | VARCHAR(64) | UNIQUE, NOT NULL | 令牌 |
| type | VARCHAR(20) | NOT NULL | 类型: register / reset_password |
| used | TINYINT | DEFAULT 0 | 是否已使用 |
| expires_at | DATETIME | NOT NULL | 过期时间 |
| created_at | DATETIME | NOT NULL | 创建时间 |

**索引**:
- `uk_token` UNIQUE (token)
- `idx_email_type` (email, type)

---

## 4. API 设计

### 4.1 通用约定

**统一响应体**:
```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

**错误码**:
| code | 含义 |
|------|------|
| 0 | 成功 |
| 1001 | 未登录 |
| 1002 | 无权限 |
| 1003 | 账号被封禁 |
| 1004 | 登录失败次数过多 |
| 2001 | 参数校验失败 |
| 2002 | 邮箱已注册 |
| 2003 | 昵称已存在 |
| 2004 | 昵称修改冷却中 |
| 3001 | 帖子不存在 |
| 3002 | 评论不存在 |
| 3003 | 版块不存在 |
| 4001 | 图片格式不支持 |
| 4002 | 图片过大 |
| 4003 | 图片数量超限 |
| 9999 | 系统异常 |

**鉴权方式**: 请求头 `Authorization: Bearer {token}`（JWT），Token 存 Redis，有效期 7 天

**分页参数**: `?page=1&size=20`

### 4.2 前台接口

#### 4.2.1 认证模块 `AuthController`

> PRD 追踪: §5.1.1 邮箱注册、§5.1.2 登录/登出

| 方法 | 路径 | 说明 | 鉴权 | PRD |
|------|------|------|------|-----|
| POST | `/api/auth/register` | 注册 | 无 | §5.1.1 |
| POST | `/api/auth/verify-email` | 验证邮箱 | 无 | §5.1.1 |
| POST | `/api/auth/resend-verification` | 重发验证邮件 | 无 | §5.1.1 |
| POST | `/api/auth/login` | 登录 | 无 | §5.1.2 |
| POST | `/api/auth/logout` | 登出 | 用户 | §5.1.2 |

**POST /api/auth/register**
```json
// Request
{ "email": "user@example.com", "password": "abc123", "nickname": "用户名" }
// Response 200
{ "code": 0, "message": "注册成功，验证邮件已发送", "data": null }
// Error: 邮箱已注册
{ "code": 2002, "message": "该邮箱已注册，请直接登录" }
```

**POST /api/auth/login**
```json
// Request
{ "email": "user@example.com", "password": "abc123" }
// Response 200
{ "code": 0, "data": { "token": "xxx", "user": { "id": 1, "nickname": "用户", "avatar": "..." } } }
// Error: 密码错误
{ "code": 2001, "message": "邮箱或密码错误" }
// Error: 账号封禁
{ "code": 1003, "message": "该账号已被封禁" }
// Error: 锁定
{ "code": 1004, "message": "登录次数过多，请稍后再试" }
```

#### 4.2.2 用户模块 `UserController`

> PRD 追踪: §5.1.3 个人资料

| 方法 | 路径 | 说明 | 鉴权 | PRD |
|------|------|------|------|-----|
| GET | `/api/users/:id` | 获取用户资料 | 无 | §5.1.3 |
| PUT | `/api/users/profile` | 更新个人资料 | 用户 | §5.1.3 |
| POST | `/api/users/avatar` | 上传头像 | 用户 | §5.1.3 |
| GET | `/api/users/:id/posts` | 用户的帖子列表 | 无 | §5.1.3 |

**PUT /api/users/profile**
```json
// Request
{ "nickname": "新昵称", "bio": "个人简介" }
// Response 200
{ "code": 0, "data": { ... } }
// Error: 昵称冷却中
{ "code": 2004, "message": "昵称每30天可修改一次，下次可修改时间：2026-06-22" }
```

#### 4.2.3 版块模块 `BoardController`

> PRD 追踪: §5.2.2 版块浏览

| 方法 | 路径 | 说明 | 鉴权 | PRD |
|------|------|------|------|-----|
| GET | `/api/boards` | 版块列表（启用的） | 无 | §5.2.2 |
| GET | `/api/boards/:id` | 版块详情 | 无 | §5.2.2 |

**GET /api/boards**
```json
// Response 200
{
  "code": 0,
  "data": [
    { "id": 1, "name": "技术交流", "description": "...", "postCount": 42 },
    { "id": 2, "name": "生活日常", "description": "...", "postCount": 18 }
  ]
}
```

#### 4.2.4 帖子模块 `PostController`

> PRD 追踪: §5.3.1-§5.3.4 帖子 CRUD

| 方法 | 路径 | 说明 | 鉴权 | PRD |
|------|------|------|------|-----|
| GET | `/api/posts` | 帖子列表（支持 boardId 筛选） | 无 | §5.3.2 |
| GET | `/api/posts/:id` | 帖子详情 | 无 | §5.3.3 |
| POST | `/api/posts` | 发布帖子 | 用户 | §5.3.1 |
| PUT | `/api/posts/:id` | 编辑帖子 | 用户（作者） | §5.3.4 |
| DELETE | `/api/posts/:id` | 删除帖子（软删除） | 用户（作者） | §5.3.4 |

**GET /api/posts?boardId=1&page=1&size=20**
```json
// Response 200
{
  "code": 0,
  "data": {
    "total": 42,
    "list": [
      {
        "id": 1,
        "title": "帖子标题",
        "summary": "正文前100字符...",
        "author": { "id": 1, "nickname": "用户", "avatar": "..." },
        "boardId": 1,
        "boardName": "技术交流",
        "likeCount": 5,
        "commentCount": 3,
        "isPinned": false,
        "thumbnail": "https://...",
        "createdAt": "2026-05-23T10:00:00"
      }
    ]
  }
}
```

**POST /api/posts**
```json
// Request
{
  "boardId": 1,
  "title": "帖子标题",
  "content": "<p>正文内容</p>",
  "imageUrls": ["https://...", "https://..."]
}
// Response 200
{ "code": 0, "data": { "id": 1 } }
```

#### 4.2.5 评论模块 `CommentController`

> PRD 追踪: §5.4.2 评论与回复

| 方法 | 路径 | 说明 | 鉴权 | PRD |
|------|------|------|------|-----|
| GET | `/api/posts/:postId/comments` | 评论列表（树形） | 无 | §5.4.2 |
| POST | `/api/posts/:postId/comments` | 发表评论 | 用户 | §5.4.2 |
| DELETE | `/api/comments/:id` | 删除评论 | 用户（作者） | §5.4.2 |

**GET /api/posts/1/comments**
```json
// Response 200
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "content": "评论A",
      "author": { "id": 2, "nickname": "用户B", "avatar": "..." },
      "createdAt": "2026-05-23T10:00:00",
      "children": [
        {
          "id": 2,
          "content": "回复A-1",
          "author": { "id": 3, "nickname": "用户C", "avatar": "..." },
          "replyTo": { "id": 2, "nickname": "用户B" },
          "createdAt": "2026-05-23T10:05:00",
          "children": []
        }
      ]
    }
  ]
}
```

**POST /api/posts/1/comments**
```json
// Request（顶级评论）
{ "content": "评论内容" }
// Request（回复）
{ "content": "回复内容", "parentId": 2, "replyToUserId": 3 }
```

#### 4.2.6 点赞模块 `LikeController`

> PRD 追踪: §5.4.1 点赞

| 方法 | 路径 | 说明 | 鉴权 | PRD |
|------|------|------|------|-----|
| POST | `/api/posts/:id/like` | 点赞（幂等） | 用户 | §5.4.1 |
| DELETE | `/api/posts/:id/like` | 取消点赞 | 用户 | §5.4.1 |

**POST /api/posts/1/like** — 重复调用返回成功（幂等）
```json
// Response 200
{ "code": 0, "data": { "liked": true, "likeCount": 6 } }
```

#### 4.2.7 收藏模块 `FavoriteController`

> PRD 追踪: §5.4.3 收藏

| 方法 | 路径 | 说明 | 鉴权 | PRD |
|------|------|------|------|-----|
| POST | `/api/posts/:id/favorite` | 收藏 | 用户 | §5.4.3 |
| DELETE | `/api/posts/:id/favorite` | 取消收藏 | 用户 | §5.4.3 |
| GET | `/api/users/:id/favorites` | 收藏列表 | 用户 | §5.4.3 |

#### 4.2.8 搜索模块 `SearchController`

> PRD 追踪: §5.5 搜索

| 方法 | 路径 | 说明 | 鉴权 | PRD |
|------|------|------|------|-----|
| GET | `/api/search?q=xxx&page=1&size=20` | 搜索帖子 | 无 | §5.5 |

#### 4.2.9 文件上传 `FileController`

> PRD 追踪: §5.3.1 图片上传

| 方法 | 路径 | 说明 | 鉴权 | PRD |
|------|------|------|------|-----|
| POST | `/api/upload/image` | 上传图片 | 用户 | §5.3.1 |

```json
// Request: multipart/form-data, field "file"
// Response 200
{ "code": 0, "data": { "url": "https://..." } }
```

### 4.3 后台接口

> 所有后台接口前缀 `/api/admin/`，需管理员 Token 鉴权

#### 4.3.1 管理员认证

> PRD 追踪: §6.1 管理员登录

| 方法 | 路径 | 说明 | PRD |
|------|------|------|-----|
| POST | `/api/admin/auth/login` | 管理员登录 | §6.1 |

#### 4.3.2 用户管理

> PRD 追踪: §6.2 用户管理

| 方法 | 路径 | 说明 | PRD |
|------|------|------|-----|
| GET | `/api/admin/users` | 用户列表（分页/搜索） | §6.2 |
| GET | `/api/admin/users/:id` | 用户详情 | §6.2 |
| PUT | `/api/admin/users/:id/ban` | 封禁用户 | §6.2 |
| PUT | `/api/admin/users/:id/unban` | 解封用户 | §6.2 |
| PUT | `/api/admin/users/:id/reset-password` | 重置密码 | §6.2 |

**PUT /api/admin/users/1/ban**
```json
// Request
{ "reason": "发布违规内容" }
// Response 200
{ "code": 0, "message": "封禁成功" }
// Error: 封禁自己
{ "code": 1002, "message": "不能封禁自己的账号" }
```

#### 4.3.3 版块管理

> PRD 追踪: §6.4 版块管理

| 方法 | 路径 | 说明 | PRD |
|------|------|------|-----|
| GET | `/api/admin/boards` | 版块列表（含禁用） | §6.4 |
| POST | `/api/admin/boards` | 创建版块 | §6.4 |
| PUT | `/api/admin/boards/:id` | 编辑版块 | §6.4 |
| PUT | `/api/admin/boards/:id/status` | 启用/禁用 | §6.4 |
| DELETE | `/api/admin/boards/:id` | 删除版块 | §6.4 |

#### 4.3.4 帖子管理

> PRD 追踪: §6.3 帖子管理

| 方法 | 路径 | 说明 | PRD |
|------|------|------|-----|
| GET | `/api/admin/posts` | 帖子列表（含已删除） | §6.3 |
| DELETE | `/api/admin/posts/:id` | 删除帖子（软删除） | §6.3 |
| PUT | `/api/admin/posts/:id/restore` | 恢复帖子 | §6.3 |
| PUT | `/api/admin/posts/:id/pin` | 置顶/取消置顶 | §6.3 |
| PUT | `/api/admin/posts/:id/feature` | 加精/取消加精（二期） | §6.3 |

#### 4.3.5 评论管理

> PRD 追踪: §6.3 评论管理

| 方法 | 路径 | 说明 | PRD |
|------|------|------|-----|
| GET | `/api/admin/posts/:postId/comments` | 评论列表 | §6.3 |
| DELETE | `/api/admin/comments/:id` | 删除评论 | §6.3 |
| PUT | `/api/admin/comments/:id/restore` | 恢复评论 | §6.3 |

---

## 5. 前端页面与路由

### 5.1 路由表

> PRD 追踪: §7 前台页面清单、§8 后台管理页面清单

| 路由 | 组件 | 鉴权 | PRD |
|------|------|------|-----|
| `/` | Home.vue | 无 | §7 首页 |
| `/board/:id` | Board.vue | 无 | §7 版块帖子列表 |
| `/post/create` | PostCreate.vue | 用户 | §7 发布帖子 |
| `/post/:id` | PostDetail.vue | 无 | §7 帖子详情 |
| `/post/:id/edit` | PostEdit.vue | 用户（作者） | §7 编辑帖子 |
| `/search` | Search.vue | 无 | §7 搜索结果 |
| `/register` | Register.vue | 无 | §7 注册 |
| `/login` | Login.vue | 无 | §7 登录 |
| `/verify-email` | VerifyEmail.vue | 无 | §7 邮箱验证 |
| `/user/:id` | UserProfile.vue | 无 | §7 个人主页 |
| `/settings/profile` | Settings.vue | 用户 | §7 编辑资料 |
| `/admin/login` | AdminLogin.vue | 无 | §8 后台登录 |
| `/admin/users` | AdminUsers.vue | 管理员 | §8 用户管理 |
| `/admin/posts` | AdminPosts.vue | 管理员 | §8 帖子管理 |
| `/admin/comments` | AdminComments.vue | 管理员 | §8 评论管理 |
| `/admin/boards` | AdminBoards.vue | 管理员 | §8 版块管理 |
| `*` | NotFound.vue | 无 | — 404页 |

### 5.2 关键页面组件结构

#### 5.2.1 首页 `Home.vue`

```
┌─────────────────────────────────────────┐
│ AppHeader (导航栏: Logo, 搜索框, 登录/头像) │
├──────────┬──────────────────────────────┤
│          │                              │
│ Sidebar  │  版块 A 最新帖子 (PostCard x3) │
│ (版块列表) │  版块 B 最新帖子 (PostCard x3) │
│          │  版块 C 最新帖子 (PostCard x3) │
│          │                              │
├──────────┴──────────────────────────────┤
│ AppFooter                               │
└─────────────────────────────────────────┘
```

#### 5.2.2 帖子详情 `PostDetail.vue`

```
┌─────────────────────────────────┐
│ AppHeader                        │
├─────────────────────────────────┤
│ 帖子标题                          │
│ 作者头像 + 昵称 + 时间            │
│ ─────────────────────────────── │
│ 正文内容 (富文本渲染)              │
│ 图片展示区                        │
│ ─────────────────────────────── │
│ [点赞] [收藏]                     │
│ ─────────────────────────────── │
│ 评论区                            │
│ ├─ 评论A                         │
│ │  ├─ 回复A-1                    │
│ │  └─ 回复A-2                    │
│ └─ 评论B                         │
│ ─────────────────────────────── │
│ [评论输入框]                      │
├─────────────────────────────────┤
│ AppFooter                        │
└─────────────────────────────────┘
```

#### 5.2.3 后台管理 `AdminLayout.vue`

```
┌─────────────────────────────────────────┐
│ 顶部栏 (Logo, 管理员信息, 退出)           │
├──────────┬──────────────────────────────┤
│          │                              │
│ 侧边菜单  │  内容区 (router-view)         │
│ - 用户管理 │                              │
│ - 帖子管理 │                              │
│ - 评论管理 │                              │
│ - 版块管理 │                              │
│          │                              │
└──────────┴──────────────────────────────┘
```

---

## 6. 核心业务逻辑

### 6.1 注册流程

> PRD 追踪: §5.1.1

```
1. 校验邮箱格式 + 唯一性
2. 校验密码强度（>=6位，含字母+数字）
3. 校验昵称（2-20字符，唯一）
4. 密码 bcrypt 加密
5. 插入 user 表（email_verified=0）
6. 生成 verification_token（有效期24h）
7. 发送验证邮件
8. 返回成功
```

### 6.2 登录流程

> PRD 追踪: §5.1.2

```
1. 查询用户 by email
2. 检查 status（封禁 → 1003）
3. 检查 locked_until（锁定 → 1004）
4. 验证密码
   - 失败: login_fail_count++
     - count >= 5 → 设置 locked_until = now + 15min
   - 成功: login_fail_count = 0, locked_until = null
5. 生成 JWT Token，存 Redis（7天过期）
6. 返回 token + 用户基本信息
```

### 6.3 发帖流程

> PRD 追踪: §5.3.1

```
1. 校验 board_id 对应版块存在且启用
2. 校验标题（1-100字符）
3. 校验内容（非空）
4. 校验图片数量（<=9）
5. 插入 post 表
6. 批量插入 post_image 表
7. board.post_count++
8. 返回帖子 ID
```

### 6.4 评论流程（树形）

> PRD 追踪: §5.4.2

```
1. 校验帖子存在且未删除
2. 校验内容（1-1000字符）
3. 如果 parentId 不为空:
   a. 查询父评论，确认存在且属于同一帖子
   b. 校验 depth < 2（否则拒绝）
   c. 设置 depth = 父评论.depth + 1
   d. 设置 reply_to_user_id = 父评论.user_id
4. 插入 comment 表
5. post.comment_count++
6. 返回评论数据
```

### 6.5 点赞流程（幂等）

> PRD 追踪: §5.4.1

```
1. 查询 post_like 表 (post_id, user_id)
2. 如果已存在 → 删除记录，post.like_count--，返回 liked=false
3. 如果不存在 → 插入记录，post.like_count++，返回 liked=true
4. 使用 UNIQUE 约束兜底并发场景
```

### 6.6 软删除策略

> PRD 追踪: §11 已确认事项

所有删除操作统一采用软删除：
- 设置 `deleted = 1`
- 前台查询统一加 `WHERE deleted = 0` 条件
- 后台查询可选是否包含已删除
- 恢复操作: 设置 `deleted = 0`
- 帖子删除时级联标记评论 `deleted = 1`

---

## 7. 任务拆解（实现顺序）

> 每个任务标注 PRD 追踪编号，按依赖关系排序

### Phase 1: 项目初始化

| # | 任务 | 产出 | PRD |
|---|------|------|-----|
| 1.1 | 后端项目骨架搭建 | Spring Boot 项目 + Maven 配置 + 目录结构 | — |
| 1.2 | 数据库建表脚本 | schema.sql（8张表） | §3.2 |
| 1.3 | MyBatis-Plus 配置 | 分页插件、自动填充（created_at/updated_at） | — |
| 1.4 | 通用组件 | Result、PageResult、ErrorCode、GlobalExceptionHandler | — |
| 1.5 | 前端项目骨架搭建 | Vue 3 + Vite + Element Plus + Router + Pinia | — |
| 1.6 | 前端 Axios 封装 | request.js（拦截器、Token 注入、错误处理） | — |

### Phase 2: 用户系统

| # | 任务 | 产出 | PRD |
|---|------|------|-----|
| 2.1 | User 实体 + Mapper | entity/User.java + mapper/UserMapper.java | §5.1 |
| 2.2 | 注册接口 | POST /api/auth/register | §5.1.1 |
| 2.3 | 邮箱验证接口 | POST /api/auth/verify-email, /resend-verification | §5.1.1 |
| 2.4 | 登录接口 | POST /api/auth/login（含失败锁定逻辑） | §5.1.2 |
| 2.5 | 登出接口 | POST /api/auth/logout | §5.1.2 |
| 2.6 | Auth 拦截器 | AuthInterceptor（Token 校验 + 用户注入） | §5.1.2 |
| 2.7 | 用户资料接口 | GET /api/users/:id, PUT /api/users/profile | §5.1.3 |
| 2.8 | 头像上传接口 | POST /api/users/avatar | §5.1.3 |
| 2.9 | 前端-注册页 | Register.vue | §7 |
| 2.10 | 前端-登录页 | Login.vue | §7 |
| 2.11 | 前端-邮箱验证页 | VerifyEmail.vue | §7 |
| 2.12 | 前端-用户状态管理 | stores/user.js（登录态、Token） | §7 |
| 2.13 | 前端-编辑资料页 | Settings.vue | §7 |

### Phase 3: 版块系统

| # | 任务 | 产出 | PRD |
|---|------|------|-----|
| 3.1 | Board 实体 + Mapper | entity/Board.java + mapper/BoardMapper.java | §5.2 |
| 3.2 | 版块列表接口 | GET /api/boards | §5.2.2 |
| 3.3 | 前端-首页布局 | Home.vue + AppHeader + AppSidebar | §7 |
| 3.4 | 前端-版块列表组件 | AppSidebar（版块导航） | §7 |

### Phase 4: 帖子系统

| # | 任务 | 产出 | PRD |
|---|------|------|-----|
| 4.1 | Post + PostImage 实体 + Mapper | entity/Post.java, PostImage.java | §5.3 |
| 4.2 | 发帖接口 | POST /api/posts | §5.3.1 |
| 4.3 | 帖子列表接口 | GET /api/posts（分页、版块筛选） | §5.3.2 |
| 4.4 | 帖子详情接口 | GET /api/posts/:id | §5.3.3 |
| 4.5 | 编辑帖子接口 | PUT /api/posts/:id | §5.3.4 |
| 4.6 | 删除帖子接口（软删除） | DELETE /api/posts/:id | §5.3.4 |
| 4.7 | 图片上传接口 | POST /api/upload/image | §5.3.1 |
| 4.8 | 前端-帖子卡片组件 | PostCard.vue | §7 |
| 4.9 | 前端-帖子列表组件 | PostList.vue | §7 |
| 4.10 | 前端-版块帖子页 | Board.vue | §7 |
| 4.11 | 前端-富文本编辑器 | PostEditor.vue（WangEditor 封装） | §7 |
| 4.12 | 前端-图片上传组件 | ImageUpload.vue | §7 |
| 4.13 | 前端-发帖页 | PostCreate.vue | §7 |
| 4.14 | 前端-编辑帖子页 | PostEdit.vue | §7 |
| 4.15 | 前端-帖子详情页 | PostDetail.vue | §7 |

### Phase 5: 互动系统

| # | 任务 | 产出 | PRD |
|---|------|------|-----|
| 5.1 | PostLike 实体 + Mapper | 点赞表 | §5.4.1 |
| 5.2 | 点赞接口（幂等） | POST/DELETE /api/posts/:id/like | §5.4.1 |
| 5.3 | Comment 实体 + Mapper | 评论表 | §5.4.2 |
| 5.4 | 评论接口（树形） | GET/POST /api/posts/:postId/comments | §5.4.2 |
| 5.5 | 删除评论接口 | DELETE /api/comments/:id | §5.4.2 |
| 5.6 | PostFavorite 实体 + Mapper | 收藏表 | §5.4.3 |
| 5.7 | 收藏接口 | POST/DELETE /api/posts/:id/favorite | §5.4.3 |
| 5.8 | 前端-点赞组件 | 点赞按钮（防抖） | §7 |
| 5.9 | 前端-评论树组件 | CommentTree + CommentItem | §7 |
| 5.10 | 前端-评论输入框 | CommentForm | §7 |
| 5.11 | 前端-收藏组件 | 收藏按钮 | §7 |

### Phase 6: 搜索

| # | 任务 | 产出 | PRD |
|---|------|------|-----|
| 6.1 | 搜索接口 | GET /api/search（MySQL FULLTEXT） | §5.5 |
| 6.2 | 前端-搜索结果页 | Search.vue | §7 |

### Phase 7: 后台管理

| # | 任务 | 产出 | PRD |
|---|------|------|-----|
| 7.1 | Admin 拦截器 | AdminInterceptor | §6.1 |
| 7.2 | 管理员登录接口 | POST /api/admin/auth/login | §6.1 |
| 7.3 | 用户管理接口 | 列表/封禁/解封/重置密码 | §6.2 |
| 7.4 | 版块管理接口 | CRUD + 启用/禁用 | §6.4 |
| 7.5 | 帖子管理接口 | 列表/删除/恢复/置顶 | §6.3 |
| 7.6 | 评论管理接口 | 列表/删除/恢复 | §6.3 |
| 7.7 | 前端-后台登录页 | AdminLogin.vue | §8 |
| 7.8 | 前端-后台布局 | AdminLayout.vue | §8 |
| 7.9 | 前端-用户管理页 | AdminUsers.vue | §8 |
| 7.10 | 前端-帖子管理页 | AdminPosts.vue | §8 |
| 7.11 | 前端-评论管理页 | AdminComments.vue | §8 |
| 7.12 | 前端-版块管理页 | AdminBoards.vue | §8 |

### Phase 8: 收尾

| # | 任务 | 产出 | PRD |
|---|------|------|-----|
| 8.1 | 404 页面 | NotFound.vue | §10.3 |
| 8.2 | 全局错误处理 | 前端错误页、网络异常提示 | §10.3 |
| 8.3 | 接口限流 | Redis 限流（发帖10s、评论5s、搜索） | §10.2 |
| 8.4 | 前端防抖 | 点赞/收藏/提交按钮防抖 | §10.2 |

### 二期预留任务（不在一期 MVP 范围）

| # | 任务 | 产出 | PRD |
|---|------|------|-----|
| F.1 | 个人主页 | UserProfile.vue + 用户帖子列表接口 | PRD §4 二期 |
| F.2 | 帖子加精功能 | 后台加精/取消加精接口 + 前端标识 | PRD §4 二期 |
| F.3 | 关注/粉丝 | 关注/取消关注接口 + 个人主页展示 | PRD §4 二期 |
| F.4 | 站内通知 | 通知接口 + 通知中心页面 | PRD §4 二期 |
| F.5 | 举报机制 | 举报接口 + 后台处理 | PRD §4 二期 |
| F.6 | 敏感词过滤 | 敏感词库 + 内容过滤 | PRD §4 二期 |
| F.7 | 匿名发帖 | 匿名标识 + 前端隐藏作者 | PRD §4 二期 |
| F.8 | 标签系统 | 标签表 + 帖子标签关联 | PRD §4 二期 |
| F.9 | 数据统计面板 | 后台仪表盘 | PRD §4 二期 |
| F.10 | 系统设置 | 站点配置 + 敏感词库管理 | PRD §4 二期 |

---

## 8. PRD 追踪矩阵

> 从 PRD 功能项到 plan.md 任务的完整映射

| PRD 功能项 | 任务编号 | 数据库表 | API 端点 |
|-----------|---------|---------|---------|
| §5.1.1 邮箱注册 | 2.1-2.3, 2.9 | user, verification_token | POST /api/auth/register, /verify-email, /resend-verification |
| §5.1.2 登录/登出 | 2.4-2.6, 2.10 | user | POST /api/auth/login, /logout |
| §5.1.3 个人资料 | 2.7-2.8, 2.13 | user | GET/PUT /api/users/*, POST /api/users/avatar |
| §5.2 版块浏览 | 3.1-3.4 | board | GET /api/boards |
| §5.3.1 发布帖子 | 4.1-4.2, 4.7, 4.11-4.13 | post, post_image | POST /api/posts, POST /api/upload/image |
| §5.3.2 帖子列表 | 4.3, 4.8-4.10 | post | GET /api/posts |
| §5.3.3 帖子详情 | 4.4, 4.15 | post, post_image | GET /api/posts/:id |
| §5.3.4 编辑/删除帖子 | 4.5-4.6, 4.14 | post | PUT/DELETE /api/posts/:id |
| §5.4.1 点赞 | 5.1-5.2, 5.8 | post_like | POST/DELETE /api/posts/:id/like |
| §5.4.2 评论/回复 | 5.3-5.5, 5.9-5.10 | comment | GET/POST /api/posts/:postId/comments, DELETE /api/comments/:id |
| §5.4.3 收藏 | 5.6-5.7, 5.11 | post_favorite | POST/DELETE /api/posts/:id/favorite |
| §5.5 搜索 | 6.1-6.2 | post (FULLTEXT) | GET /api/search |
| §6.1 管理员登录 | 7.1-7.2 | user | POST /api/admin/auth/login |
| §6.2 用户管理 | 7.3, 7.9 | user | GET/PUT /api/admin/users/* |
| §6.3 内容管理 | 7.5-7.6, 7.10-7.11 | post, comment | GET/DELETE/PUT /api/admin/posts/*, /api/admin/comments/* |
| §6.4 版块管理 | 7.4, 7.12 | board | GET/POST/PUT/DELETE /api/admin/boards/* |

---

## 9. 技术决策记录

| 决策 | 选择 | 原因 |
|------|------|------|
| 评论存储 | 邻接表（parent_id）+ 应用层组装树 | 小规模数据，查询简单，避免闭表复杂度 |
| 搜索方案 | MySQL FULLTEXT | 小规模数据，无需引入 Elasticsearch |
| 图片存储 | 本地文件系统 / 可替换为 OSS | 一期简单处理，接口抽象便于后续切换 |
| Token 方案 | JWT + Redis | 无状态验证 + Redis 存储支持踢人 |
| 计数字段 | 冗余存储（like_count 等） | 避免每次 COUNT 查询，通过原子更新保证一致性 |
| 富文本编辑器 | WangEditor | 轻量、中文友好、Vue 3 支持好 |
| 软删除 | deleted 字段 + 统一查询条件 | 数据可恢复，实现简单 |

---

*方案结束 — 按 Phase 1→9 顺序实现，每个 Phase 完成后可独立验证*
