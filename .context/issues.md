# 社区论坛系统 — 验收问题清单

> 记录二次验收过程中发现的问题，按模块分组。
> 更新时间：2026-05-23

---

## M0 基础设施模块

### 1. M0-T13 package.json engines 范围过宽

- **位置**: `forum-web/package.json`
- **现状**: `"node": ">=18"`
- **要求**: C3 决策要求 `"node": ">=18 <19"`，防止误用 Node 20+
- **影响**: 低。Node 20+ 下大概率兼容，但违反 C3 约定
- **修复**: 改为 `">=18 <19"`

### 2. M0-T13 forum-web/.gitignore 缺失

- **位置**: `forum-web/` 根目录
- **现状**: 无 .gitignore 文件
- **要求**: 至少包含 `node_modules/` 和 `dist/`
- **影响**: 中。node_modules（~60MB）和 dist 会被 git 追踪，污染仓库
- **修复**: 创建 .gitignore

### 3. plan.md JDK 版本未同步

- **位置**: `.context/plan/plan.md` §1 技术栈表
- **现状**: JDK 行仍写 `1.8`
- **要求**: C9 决策已改为 JDK 17，CLAUDE.md 和 pom.xml 已更新，plan.md 未同步
- **影响**: 低。不影响运行，但文档不一致可能导致误解
- **修复**: 改为 `17`

---

## M1 用户系统模块

### 4. M1-T14 拦截器注册类命名差异

- **位置**: `forum-server/.../config/ForumWebMvcConfig.java`
- **现状**: AuthInterceptor 在 ForumWebMvcConfig 中注册
- **要求**: todo 指定在 `SecurityConfig.java` 中注册
- **影响**: 无。功能完全等价，路径排除规则正确
- **备注**: 后续 M6 需注册 AdminInterceptor 时，需决定是扩展 ForumWebMvcConfig 还是新建 SecurityConfig

---

*文档结束 — 新增问题追加在对应模块段落下*
