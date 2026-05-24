# 社区论坛系统

> 面向小型社区（<1000 人）的图文论坛，含用户前台和管理员后台。  
> **技术栈**：Java 17 / Spring Boot 2.7 / MyBatis-Plus / MySQL 8 / Redis / Vue 3 / Element Plus / Vite  
> **当前阶段**：一期 MVP + 二期 + 三期 **已基本完成**（2026-05-24）

---

## 一、项目简介

社区论坛系统是一个**前后端分离**的图文社区平台。用户可按板块浏览和发布帖子，通过评论、点赞、收藏互动；管理员和吧主可通过后台或板块页管理内容与公告。

- **核心场景**：小团体 / 兴趣社区的轻量交流
- **内容形式**：图文帖子 + 两层楼中楼评论（贴吧风展示）
- **界面风格**：贴吧风三栏布局（左导航 / 中信息流 / 右公告）
- **架构形态**：Java REST API + Vue 3 SPA（前台 + 后台）

---

## 二、适合谁使用

| 用户类型 | 使用场景 |
|----------|----------|
| 普通用户 | 注册登录、关注板块、浏览信息流、弹窗发帖/编辑、评论互动、弹窗个人中心与改密 |
| 吧主 | 编辑板块、发布板块公告、本吧帖子置顶、删除板块（非系统板块） |
| 管理员 | 后台管理用户 / 板块 / 帖子 / 评论 / 全站公告、审核板块申请；前台可置顶任意帖 |
| 开发者 | 学习 Spring Boot + Vue 全栈，或在此基础上二次开发 |
| 非技术用户 | 按下方「本地运行方式」启动后，在浏览器中试用 |

---

## 三、项目主要功能

> 完整需求与边缘场景见 [`context/PRD-社区论坛系统.md`](context/PRD-社区论坛系统.md)。

### 一期 MVP（已完成）

- **用户系统**：邮箱注册与验证、登录/登出、个人资料（头像 / 昵称 / 简介）、弹窗内修改密码
- **板块系统**：板块列表、按板块浏览帖子
- **帖子系统**：图文帖（弹窗发帖，纯文本转 HTML，最多 9 张图）、列表 / 详情、弹窗编辑 / 软删除
- **互动系统**：点赞 / 收藏（幂等）、树形评论（2 层）
- **搜索**：MySQL FULLTEXT 关键词搜索（含 ngram 中文分词）
- **后台管理**：用户封禁、板块 CRUD、帖子删除/恢复/置顶、评论管理
- **系统收尾**：404 页、接口限流、全局错误提示、按钮防抖

### 二期（已完成）

- **板块扩展**：icon / slogan / tags / 吧主 / 关注数
- **板块申请审核**：用户申请 → 管理员审核 → 自动建板并设吧主
- **板块关注**：关注 / 取关、我关注的 / 推荐板块
- **贴吧风 UI**：三栏首页、图片网格卡片、面包屑详情页
- **混合信息流**：`/api/posts/feed`（关注板块 70% + 全站热门 30%）
- **发帖弹窗**：全局 Modal + 表情面板 + 板块下拉
- **搜索增强**：板块 / 帖子 / 用户 Tab，帖子支持「仅标题 / 全文」
- **吧主权限**：编辑板块信息、管理员移交吧主（本吧置顶见三期 H3）

### 三期（已基本完成）

**原计划 R1–R7（P3-M1～M10）**

- **布局优化**：`--page-max-width: 1600px`、`global.css` 统一三栏（左 240px + 中间 flex + 右 260px）
- **统一侧边栏**：`UnifiedSidebar`（主页 / **个人中心弹窗** + 关注 / 推荐 / 全部板块）
- **双层公告**：全站公告（首页右栏）+ 板块公告（板块页 / 帖子详情右栏）；正文上限 **1000 字**
- **帖子详情三栏**：左导航 + 中正文 + 右板块公告
- **公告预览**：标题 + 内容预览 + 相对时间；普通用户可点公告看只读详情
- **板块删除**：吧主 / 管理员软删除板块（系统板块 id=1 除外）
- **图片展示**：8px 圆角；列表/详情 `PostImageGrid`（单图原比例、多图 1:1 横排最多 3 张）

**执行期热修 H1–H7**（详见 [三期文档 §九](.context/phase3/phase3-requirements.md)）

| 编号 | 能力 |
|------|------|
| H3 | 吧主 / 管理员前台置顶（`POST /api/posts/{id}/pin`） |
| H4 | 编辑帖子与发帖共用 `PostEditorModal`（已移除独立编辑页） |
| H5 | 评论区修复 + 贴吧式两层 UI（展开 N 条回复） |
| H6 | 个人中心 `SettingsModal`（`/settings` 兼容打开弹窗） |
| H7 | 修改密码：`PUT /api/users/password` + 二级弹窗 |

---

## 四、项目目录结构

```
Community Forum/
├── CLAUDE.md                          # AI 编码指南 + 项目知识库（常见陷阱）
├── README.md                          # 本文件
├── todo.md                            # 一期任务归档（只读）
├── context/
│   └── PRD-社区论坛系统.md               # 产品需求文档 v1.1
├── .context/
│   ├── phase2/                        # 二期 PRD / plan / todo
│   └── phase3/
│       └── phase3-requirements.md     # 三期需求、P3-M1~10、热修 H1~H7
│
├── forum-server/                      # 后端 Spring Boot
│   ├── src/main/java/com/forum/
│   │   ├── config/                    # WebMvc / Redis / MyBatis-Plus
│   │   ├── common/                    # Result / ErrorCode / 异常处理
│   │   ├── interceptor/               # Auth / Admin 拦截器
│   │   ├── entity/                    # 实体（含 announcement 等）
│   │   ├── mapper/
│   │   ├── service/
│   │   ├── controller/                # 前台 API
│   │   └── controller/admin/          # 后台 API
│   └── src/main/resources/
│       ├── application.yml
│       └── db/
│           ├── schema.sql             # 一期建表（8 张表）
│           ├── schema.v2.sql          # 二期迁移（板块扩展 + 申请 + 关注）
│           ├── schema.v3.sql          # 三期迁移（announcement 表）
│           ├── seed.sql               # 种子数据（管理员 + 默认板块）
│           └── cleanup-test-data.sql  # 开发环境清测试数据（保留 admin + 种子板块）
│
└── forum-web/                         # 前端 Vue 3 + Vite
    ├── src/
    │   ├── views/                     # 页面（Home / Board / PostDetail / admin 等）
    │   ├── components/                # layout / post / announcement / board / user 等
    │   │   ├── user/                  # SettingsModal、ChangePasswordModal
    │   │   └── post-editor/           # PostEditorModal（发帖 + 编辑）
    │   ├── stores/                    # user / app / boardFollow / postEditor / userSettings
    │   ├── utils/richText.js          # 公告与富文本展示
    │   ├── api/
    │   ├── router/
    │   └── styles/global.css          # --page-max-width + .three-col-layout
    └── package.json
```

---

## 五、本地运行方式

### 5.1 环境准备

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| JDK | **17** | 后端运行环境 |
| Maven | 3.6+ | 后端构建 |
| MySQL | 8.x | 数据库 |
| Redis | 任意稳定版 | Token、限流（**登录前必须先启动**） |
| Node.js | **18 LTS** | 前端构建运行 |

### 5.2 数据库初始化

**新环境（推荐顺序）：**

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE forum DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 一期建表 + 种子数据（Windows 下必须加 --default-character-set=utf8mb4）
mysql -u root -p --default-character-set=utf8mb4 forum < forum-server/src/main/resources/db/schema.sql
mysql -u root -p --default-character-set=utf8mb4 forum < forum-server/src/main/resources/db/seed.sql

# 3. 二期迁移
mysql -u root -p --default-character-set=utf8mb4 forum < forum-server/src/main/resources/db/schema.v2.sql

# 4. 三期迁移
mysql -u root -p --default-character-set=utf8mb4 forum < forum-server/src/main/resources/db/schema.v3.sql
```

**已有旧库升级**：若已执行过 `schema.sql`，只需补跑尚未执行的 `schema.v2.sql` / `schema.v3.sql`（脚本含 `IF NOT EXISTS`，可重复执行）。

**部署后建议检查全文索引：**

```sql
SHOW CREATE TABLE post\G
-- 确认 idx_search、idx_title 均含 WITH PARSER ngram
```

> 默认管理员：`admin@forum.com` / `123456`（仅供开发，**上线后务必改密**）。

### 5.3 后端配置

复制开发配置并按本地环境修改：

```bash
# 参考 forum-server/src/main/resources/application-dev.example.yml
# 本地未提交副本：application-dev.yml（含数据库密码、QQ 邮箱 SMTP 授权码）
```

### 5.4 启动后端

```bash
cd forum-server
mvn spring-boot:run
```

- 默认端口：**8080**
- 健康检查：访问 `http://localhost:8080/api/boards` 应返回 JSON

### 5.5 启动前端

```bash
cd forum-web
npm install
npm run dev
```

| 地址 | 说明 |
|------|------|
| `http://localhost:5173` | 前台首页 |
| `http://localhost:5173/admin/login` | 后台登录 |

### 5.6 生产打包

```bash
cd forum-server && mvn clean package
cd forum-web && npm run build    # 产物在 forum-web/dist/
```

---

## 六、环境变量与配置

### 6.1 后端（`application.yml` 关键项）

| 配置项 | 说明 |
|--------|------|
| `spring.datasource.*` | MySQL 连接 |
| `spring.redis.*` | Redis 连接（未启动会导致登录失败） |
| `spring.mail.*` | QQ SMTP（注册验证邮件；授权码放 `application-dev.yml`） |
| `forum.upload.path` | 图片本地存储目录（dev 默认 `./uploads`） |
| `forum.jwt.secret` | JWT 签名密钥（生产环境用 64 位随机串） |
| `forum.jwt.expire-days` | Token 有效期（默认 7 天） |

### 6.2 前端（`.env` 可选）

| 变量 | 示例值 | 说明 |
|------|--------|------|
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | 后端接口基址 |

---

## 七、常用命令

```bash
# 后端
cd forum-server
mvn clean install          # 编译 + 测试
mvn spring-boot:run        # 启动（不要用 -q，否则看不到启动日志）
mvn test                   # 运行单元测试

# 前端
cd forum-web
npm run dev                # 开发服务
npm run build              # 生产构建
npm run preview            # 预览构建产物
```

---

## 八、文档索引

| 文档 | 路径 | 用途 |
|------|------|------|
| 行为指南 + 知识库 | [`CLAUDE.md`](CLAUDE.md) | 编码规范、构建命令、常见陷阱 |
| 产品需求 (PRD) | [`context/PRD-社区论坛系统.md`](context/PRD-社区论坛系统.md) | 完整功能需求（**v1.2**，含三期交付） |
| 一期任务归档 | [`todo.md`](todo.md) | M0~M7 执行记录（只读） |
| 二期文档 | [`.context/phase2/`](.context/phase2/) | PRD / plan / todo |
| 三期文档 | [`.context/phase3/phase3-requirements.md`](.context/phase3/phase3-requirements.md) | 三期需求与任务 |

---

## 九、如何测试功能

### 9.1 启动检查

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 启动 MySQL、Redis | 无端口冲突 |
| 2 | `mvn spring-boot:run` | 控制台出现 `Started ForumApplication` |
| 3 | 访问 `http://localhost:8080/api/boards` | 返回 `{"code":0,...}` |
| 4 | `npm run dev` | 输出 `http://localhost:5173` |
| 5 | 打开首页 | 三栏布局：左板块导航、中间信息流、右全站公告 |

### 9.2 前台核心路径

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | `/register` 注册并验证邮箱 | 验证后可登录 |
| 2 | 登录后点顶栏「发帖」 | 弹出发帖 Modal（非跳转页面） |
| 3 | 进入某板块 → 点「关注」 | 按钮变为「已关注」，左栏「我关注的」出现该板块 |
| 4 | 刷新首页 | 信息流含关注板块内容 |
| 5 | 进入帖子详情 | 三栏：左导航 / 中正文+评论 / 右板块公告 |
| 6 | 作者点「编辑」 | 打开发帖同款 Modal，保存后留在详情页 |
| 7 | 吧主/管理员在详情页 | 可见「置顶」按钮；板块列表内置顶帖靠前 |
| 8 | 登录后发表评论 / 回复 | 两层楼中楼；超过 2 条回复可「展开 N 条回复」 |
| 9 | 顶栏或侧栏「个人中心」 | 弹窗改资料；「账号安全」→ 二级弹窗改密 |
| 10 | 顶栏搜索关键词 | 跳转 `/search`，可切换板块/帖子/用户 Tab |
| 11 | 用户下拉 →「我的板块申请」 | 可申请新板块（需满足注册天数与发帖数） |

### 9.3 后台核心路径

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | `/admin/login` 管理员登录 | 进入后台总览 |
| 2 | 「板块申请审核」→ 通过 | 新板块出现在前台列表 |
| 3 | 「公告管理」→ 发全站公告 | 首页右栏显示 |
| 4 | 「用户管理」→ 封禁用户 | 该用户无法登录前台 |
| 5 | 「帖子管理」→ 删除 / 恢复 | 前台同步变化 |

### 9.4 出错时先看哪里

| 现象 | 排查 |
|------|------|
| 登录报「系统异常」 | Redis 是否启动（见 `CLAUDE.md`） |
| 中文搜索无结果 | `SHOW CREATE TABLE post` 检查 ngram 索引 |
| 注册后长时间无响应 | SMTP 网络慢；邮件已改异步，可点重发验证 |
| 端口 8080 被占用 | Windows 下可能有孤儿 Java 进程，见 `CLAUDE.md` |
| 前端接口 401 | Local Storage 中 Token 是否过期 |
| 图片上传失败 | `forum.upload.path` 目录权限、文件格式/大小 |

---

## 十、开发说明

### 10.1 阶段划分

| 阶段 | 范围 | 状态 |
|------|------|------|
| 一期 | M0~M7 基础 MVP | 已完成 |
| 二期 | P2-M1~M8 贴吧风 + 板块生态 | 已完成 |
| 三期 | P3-M1~M10 + 热修 H1~H7 | 已基本完成 |

### 10.2 推进规则

- **单任务推进**：一次只做一个模块，验收通过后再继续
- **commit 约定**：二期 `[phase2]`、三期 `[phase3]` 前缀便于追溯
- 详见 [`CLAUDE.md`](CLAUDE.md)

### 10.3 编码规范要点

- Java 类/接口须带标准 JavaDoc（`@author liuxinsi`）
- SQL 必须参数化；禁止 `DROP TABLE` / `DROP DATABASE`
- 禁止嵌套循环

---

## 附录：技术决策速查

| 决策 | 选择 | 原因 |
|------|------|------|
| 评论存储 | 邻接表（parent_id） | 小规模，查询简单 |
| 搜索 | MySQL FULLTEXT + ngram | 无需 ES |
| 图片存储 | 本地文件 + `FileStorage` 接口 | 可换 OSS |
| 登录态 | JWT + Redis | 无状态 + 可踢人 |
| 前台发帖/编辑 | 弹窗纯文本 + `plainToHtml` | 与二期 Modal 一致；旧 `/post/create` 路由仍存在 WangEditor 页 |
| 公告/展示 | `richText.js` + v-html | 纯文本换行与 HTML 混排兼容 |
| 删除策略 | 软删除（`deleted` 字段） | 可恢复 |
| 公告 | 独立 `announcement` 表 | 与 board 解耦，易扩展 |

---

*最后更新：2026-05-24（同步三期 P3-M1~10、热修 H1~H7；详见 `.context/phase3/phase3-requirements.md`）*
