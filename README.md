# 社区论坛系统

> 一个面向小型社区（<1000 人）的图文论坛，含用户前台和管理员后台。
> **文档版本**: 与 PRD v1.1 / plan.md 对齐
> **当前阶段**: 一期 MVP 开发中

---

## 一、项目简介

社区论坛系统是一个**前后端分离**的图文社区平台，用户可以按版块分区浏览和发布帖子，并通过评论、点赞、收藏等方式互动。
管理员可通过独立的后台界面管理用户、版块、帖子和评论。

- **核心场景**: 小团体/兴趣社区的轻量交流
- **内容形式**: 以"图文帖子 + 树形评论"为主
- **架构形态**: Java 后端 API + Vue 前端 SPA（前台 + 后台）

---

## 二、适合谁使用

| 用户类型 | 使用场景 |
|----------|---------|
| 普通用户 | 注册账号、浏览版块、发帖、评论、点赞、收藏 |
| 管理员 | 登录后台、管理用户（封禁/解封）、管理版块、管理帖子和评论 |
| 开发者 | 基于本项目二次开发或学习 Spring Boot + Vue 全栈 |
| 非技术用户 | 按"本地运行方式"启动项目，浏览页面进行试用 |

---

## 三、项目主要功能

> 详细功能与边缘场景见 `context/PRD-社区论坛系统.md`。

### 一期 MVP（当前开发范围）

- **用户系统**：邮箱注册、邮箱验证、登录/登出、基础个人资料（头像、昵称、简介）
- **版块系统**：版块列表展示、按版块浏览帖子
- **帖子系统**：发布图文帖子（标题+富文本+最多9张图）、列表浏览、详情查看、作者编辑/删除
- **互动系统**：点赞/取消点赞（幂等）、树形评论（最多 2 层）、收藏/取消收藏
- **搜索**：关键词搜索（标题+正文，MySQL FULLTEXT）
- **后台管理**：管理员登录、用户管理（封禁/解封/重置密码）、版块 CRUD、帖子管理（删除/恢复/置顶）、评论管理（删除/恢复）

### 二期及以后（暂未实现）

- 个人主页（我的帖子、我的收藏）
- 帖子加精、匿名发帖、标签系统
- 站内通知、关注/粉丝
- 举报机制、敏感词过滤
- 数据统计、系统设置
- 等级积分、私信、邮件通知（三期）

---

## 四、项目目录结构说明

> 项目采用 monorepo 风格，后端与前端独立目录。

```
Community Forum/
├── CLAUDE.md                       # AI Agent 行为指南 + 项目知识库
├── README.md                       # 本文件
├── todo.md                         # 任务清单（按功能模块拆分）
├── context/
│   └── PRD-社区论坛系统.md          # 产品需求文档 v1.1
├── .context/
│   └── plan/
│       └── plan.md                 # 技术方案（架构/数据库/API/任务拆解）
│
├── forum-server/                   # 【后端】Spring Boot 2.x（待创建）
│   ├── src/main/java/com/forum/
│   │   ├── ForumApplication.java
│   │   ├── config/                 # 配置：WebMvc / Redis / MybatisPlus / Security
│   │   ├── common/                 # 通用：Result / PageResult / ErrorCode / 异常处理
│   │   ├── interceptor/            # 拦截器：AuthInterceptor / AdminInterceptor
│   │   ├── entity/                 # 8 张表对应的实体
│   │   ├── mapper/                 # MyBatis-Plus Mapper
│   │   ├── service/                # 业务逻辑
│   │   ├── controller/             # 前台接口
│   │   └── controller/admin/       # 后台接口
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── mapper/                 # 复杂查询的 XML 映射
│   │   └── db/schema.sql           # 建表脚本
│   └── pom.xml
│
└── forum-web/                      # 【前端】Vue 3 + Vite（待创建）
    ├── src/
    │   ├── main.js / App.vue
    │   ├── router/                 # 路由定义
    │   ├── stores/                 # Pinia 状态（user / app）
    │   ├── api/                    # Axios 接口封装
    │   ├── views/                  # 前台页面（首页/帖子/登录/资料 等）
    │   ├── components/             # 通用组件（layout / post / comment / common）
    │   └── admin/                  # 后台管理页面
    ├── public/
    ├── vite.config.js
    └── package.json
```

> 注：`forum-server/` 与 `forum-web/` 目录在 Phase 1（项目初始化）任务完成前**尚未创建**。

---

## 五、本地运行方式

### 5.1 环境准备（前置依赖）

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| JDK | **17**（由 CLAUDE.md 原定 1.8 放宽，2026-05-23 确认） | 后端运行环境 |
| Maven | 3.6+ | 后端构建工具 |
| MySQL | 8.x | 数据库 |
| Redis | 任意稳定版 | Token、限流、计数缓存 |
| Node.js | **18 LTS** | 前端构建运行环境 |
| npm / pnpm | 任意 | 前端包管理 |

### 5.2 数据库初始化

```bash
# 1. 登录 MySQL，创建数据库
mysql -u root -p
CREATE DATABASE forum DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 执行建表脚本（注意 --default-character-set=utf8mb4 在 Windows 下必须加，否则中文会被截断）
mysql -u root -p --default-character-set=utf8mb4 forum < forum-server/src/main/resources/db/schema.sql

# 3. 执行种子脚本（默认管理员 admin@forum.com/123456 + 3 个版块；幂等可重复执行）
mysql -u root -p --default-character-set=utf8mb4 forum < forum-server/src/main/resources/db/seed.sql
```

> 共 8 张表：`user / board / post / post_image / comment / post_like / post_favorite / verification_token`，详细字段见 `plan.md §3.2`。
>
> ⚠️ **首次部署务必修改 admin 默认密码 `123456`**（PRD §5.1.1 强度规则要求字母+数字+≥6 位，默认密码仅供开发自测）。

### 5.3 启动后端

```bash
cd forum-server
mvn clean install
mvn spring-boot:run
```

- 默认端口：**8080**
- 健康检查：浏览器访问 `http://localhost:8080/api/boards` 应返回 JSON（即使列表为空）
- 默认管理员账号：**admin@forum.com**（密码由 schema/seed 脚本中的 bcrypt 串决定，首次登录后需立即修改）

### 5.4 启动前端

```bash
cd forum-web
npm install
npm run dev
```

- 默认前端访问地址：**http://localhost:5173**（Vite 默认）
- 后台管理入口：**http://localhost:5173/admin/login**

### 5.5 生产打包

```bash
# 后端
cd forum-server
mvn clean package        # 生成 forum-server/target/*.jar

# 前端
cd forum-web
npm run build            # 生成 forum-web/dist/
```

---

## 六、环境变量说明

> 后端配置主要通过 `application.yml`，前端配置通过 `.env.*` 文件。
> **待确认**: 具体配置项以 Phase 1 完成后的实际文件为准。

### 6.1 后端（application.yml 关键项）

| 配置项 | 示例值 | 说明 |
|--------|--------|------|
| `server.port` | 8080 | 后端端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/forum?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai` | 数据库连接 |
| `spring.datasource.username` | root | 数据库用户名 |
| `spring.datasource.password` | （根据本地环境） | 数据库密码 |
| `spring.redis.host` | localhost | Redis 地址 |
| `spring.redis.port` | 6379 | Redis 端口 |
| `spring.mail.host` | smtp.qq.com | 邮件服务器（QQ 邮箱 SMTP） |
| `spring.mail.port` | 465 | SMTP 端口（SSL） |
| `spring.mail.username` | xxx@qq.com | 发件人 QQ 邮箱 |
| `spring.mail.password` | （QQ 邮箱**授权码**，非登录密码） | 在 QQ 邮箱设置→账户中开启 SMTP 并获取授权码 |
| `forum.upload.path` | dev: `./uploads`  /  prod: `/data/forum-uploads` | 图片上传本地存储目录 |
| `forum.upload.access-prefix` | `/static/uploads/**` | 静态资源映射前缀（WebMvcConfig 中注册） |
| `forum.jwt.secret` | （**首次部署生成 64 位随机串并固化**） | JWT 签名密钥 |
| `forum.jwt.expire-days` | 7 | Token 有效期 |
| `forum.ratelimit.fallback` | `allow` | Redis 不可用时的限流降级策略：`allow`=放行 |

> 📦 **图片存储设计**：一期使用本地文件系统，但 `FileService` 须抽象为 `FileStorage` 接口（本地实现 `LocalFileStorage`），便于二期无缝切换 OSS（如阿里云）。

> 📧 **邮件 SMTP 提示**：QQ 邮箱授权码获取路径——QQ 邮箱网页版 → 设置 → 账户 → 开启 "POP3/SMTP 服务" → 生成授权码（16 位）。

### 6.2 前端（.env / .env.production）

| 变量 | 示例值 | 说明 |
|------|--------|------|
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | 后端接口基址 |
| `VITE_UPLOAD_URL` | `http://localhost:8080/api/upload/image` | 图片上传地址 |
| `VITE_STATIC_BASE` | `http://localhost:8080/static/uploads` | 静态资源（上传图片）访问基址 |

---

## 七、常用命令

### 后端

```bash
mvn clean install            # 清理并编译打包
mvn spring-boot:run          # 启动开发服务
mvn test                     # 运行单元测试（如有）
mvn clean package -DskipTests  # 跳过测试打包
```

### 前端

```bash
npm install                  # 安装依赖
npm run dev                  # 启动开发服务（默认 5173）
npm run build                # 生产环境打包
npm run preview              # 本地预览生产构建
```

### 数据库

```bash
# 重置数据库（开发环境）
mysql -u root -p -e "DROP DATABASE IF EXISTS forum; CREATE DATABASE forum DEFAULT CHARSET utf8mb4;"
mysql -u root -p forum < forum-server/src/main/resources/db/schema.sql
```

> ⚠️ **生产环境禁止执行 `DROP DATABASE`**。

---

## 八、文档索引

| 文档 | 路径 | 用途 |
|------|------|------|
| 行为指南 + 项目知识库 | `CLAUDE.md` | AI Agent 编码规则、技术栈、构建命令 |
| 产品需求文档 (PRD) | `context/PRD-社区论坛系统.md` | 完整功能需求、字段规则、边缘场景 |
| 技术方案 | `.context/plan/plan.md` | 架构、数据库、API、任务拆解、技术决策 |
| 任务清单 | `todo.md` | 按功能模块拆分的开发任务及状态 |

---

## 九、开发流程说明

> 项目按 `plan.md` 的 **Phase 1 → Phase 8** 顺序推进，每个 Phase 完成后可独立验证。

### 9.1 总体阶段

1. **Phase 1：项目初始化** — 搭后端/前端骨架、建表脚本、通用组件
2. **Phase 2：用户系统** — 注册、登录、个人资料
3. **Phase 3：版块系统** — 版块列表、首页布局
4. **Phase 4：帖子系统** — 发帖、列表、详情、编辑、删除、图片上传
5. **Phase 5：互动系统** — 点赞、评论（树形）、收藏
6. **Phase 6：搜索** — 关键词搜索
7. **Phase 7：后台管理** — 用户/版块/帖子/评论管理
8. **Phase 8：收尾** — 404、错误处理、限流、防抖

### 9.2 任务推进规则（出自 `CLAUDE.md`）

- **单任务推进**：一次只做一个模块，完成验收后再做下一个
- **任务原子化**：一个任务只改/建一个主要文件
- **状态严格**：`todo.md` 中一次只有一个任务为"进行中"
- **状态枚举**：未开始 / 进行中 / 已完成 / 阻塞 / 待确认
- **验收枚举**：待验收 / 自动验收通过 / 人工验收通过 / 验收驳回 / 重新提交 / 无需验收

### 9.3 编码规范要点

- Java 类与接口**必须**带标准 JavaDoc 注释（含 `@author liuxinsi` 和 `@date`）
- 禁止嵌套循环
- SQL 必须参数化，**禁止** `DROP TABLE` / `DROP DATABASE`
- 操作前验证环境（生产/测试/开发）
- 详见 `CLAUDE.md §编码规范`

---

## 十、如何测试功能是否正常

> 面向非技术用户的"看得到、点得到、测得到"的验证方式。

### 10.1 启动检查（确认环境无问题）

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 启动 MySQL 和 Redis | 服务正常运行，无端口冲突 |
| 2 | 执行 `mvn spring-boot:run` | 控制台输出 "Started ForumApplication"，无报错堆栈 |
| 3 | 浏览器访问 `http://localhost:8080/api/boards` | 返回 JSON（`{"code":0, "data":[...]}`） |
| 4 | 执行 `npm run dev` | 控制台显示访问地址，无报错 |
| 5 | 浏览器访问 `http://localhost:5173` | 显示首页（含顶部导航、版块列表区） |

### 10.2 核心功能体验路径

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 打开 `/register` 注册账号 | 填写邮箱/密码/昵称提交后，提示"验证邮件已发送" |
| 2 | 打开邮箱点击验证链接 | 跳转 `/verify-email` 显示"验证成功" |
| 3 | 用刚才账号登录 `/login` | 跳转首页，右上角显示用户头像 |
| 4 | 进入任一版块发帖 `/post/create` | 填写标题+正文（可加图片）提交后跳转新帖子详情 |
| 5 | 在帖子详情点击点赞 | 数字 +1，按钮高亮 |
| 6 | 发表一条评论 | 评论区出现新评论 |
| 7 | 点击别人评论的"回复" | 展开回复输入框，发布后显示在子级 |
| 8 | 点击收藏 | 按钮高亮，再次点击取消 |
| 9 | 用搜索框搜帖子标题关键词 | 跳转 `/search?q=xxx` 显示结果列表 |
| 10 | 登出后访问 `/post/create` | 自动跳转登录页 |

### 10.3 后台功能体验路径

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 访问 `/admin/login` 用管理员账号登录 | 进入后台首页 |
| 2 | 进入"版块管理"创建一个新版块 | 列表显示新版块 |
| 3 | 进入"用户管理"封禁某个测试账号 | 该账号无法再登录前台 |
| 4 | 进入"帖子管理"删除一条帖子 | 前台该帖不再展示 |
| 5 | 在"帖子管理"恢复刚删除的帖子 | 前台重新可见 |

### 10.4 出错时先看哪里

| 现象 | 排查位置 |
|------|---------|
| 前端页面打不开 | 1) 前端 `npm run dev` 控制台报错；2) 浏览器 F12 → Console / Network 错误 |
| 接口报 401（未登录） | 1) 浏览器 Local Storage 中 Token 是否存在；2) Token 是否过期 |
| 接口报 500 | 后端控制台堆栈 → 通常是 Service 层异常或 SQL 错误 |
| 数据没保存 | 1) 数据库表是否创建；2) 后端日志的 SQL 是否执行；3) `deleted=1`（软删除）导致查询不到 |
| 图片上传失败 | 1) 上传目录权限；2) 文件大小/格式校验；3) 静态资源映射配置 |
| 邮件没收到 | 1) `application.yml` 邮件配置；2) 后端日志的邮件发送结果；3) 垃圾邮件文件夹 |

---

## 附录：技术决策速查

> 完整版见 `plan.md §9 技术决策记录`。

| 决策 | 选择 | 简要原因 |
|------|------|---------|
| 评论存储 | 邻接表（parent_id） | 小规模数据，查询简单 |
| 搜索 | MySQL FULLTEXT | 无需引入 ES |
| 图片存储 | 本地文件系统（接口抽象，可换 OSS） | 一期简单 |
| Token | JWT + Redis | 无状态 + 可踢人 |
| 计数字段 | 冗余存储 + 原子更新 | 避免 COUNT 查询 |
| 富文本 | WangEditor | 轻量、中文友好 |
| 删除策略 | 软删除（`deleted` 字段） | 数据可恢复 |

---

*最后更新：与 PRD v1.1 / plan.md (2026-05-23) 对齐*
