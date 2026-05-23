# [CLAUDE.md](http://CLAUDE.md) — 行为指南与项目知识库

## 通用行为指南

> 减少常见 LLM 编码错误的行为准则。可根据项目需求调整。
> ****权衡：** 这些指南偏向谨慎而非速度。对于简单任务，请自行判断。

### 1. 先思考再编码

**不要假设。不要隐藏困惑。揭示权衡。**

实现前：

- 明确陈述假设。不确定时提问。
- 存在多种解释时，呈现它们——不要默默选择。
- 存在更简单的方法时，说出来。必要时提出反对。
- 不清楚时停下。指出困惑之处，然后提问。

### 2. 简单优先

**解决问题的最少代码。不要投机性扩展。**

- 不添加超出要求的功能。
- 不为一次性代码创建抽象。
- 不添加未请求的"灵活性"或"可配置性"。
- 不为不可能的场景添加错误处理。
- 如果写了 200 行可以缩成 50 行，重写它。

自问："资深工程师会觉得这过度复杂吗？" 如果是，简化。

### 3. 精准修改

**只动必须动的。只清理自己造成的混乱。**

编辑现有代码时：

- 不要"改进"相邻代码、注释或格式。
- 不要重构没有问题的代码。
- 匹配现有风格，即使你会用不同方式。
- 注意到无关的死代码时，提一下——但不要删除。

当你的变更产生孤立代码时：

- 删除你的变更导致的未使用的导入/变量/函数。
- 除非被要求，否则不要删除预先存在的死代码。

测试标准：每行变更都应直接追溯到用户请求。

### 4. 目标驱动执行

**定义成功标准。循环直到验证通过。**

将任务转化为可验证目标：

- "添加验证" → "为无效输入编写测试，然后让它们通过"
- "修复 bug" → "编写重现测试，然后让测试通过"
- "重构 X" → "确保重构前后测试都通过"

多步任务时，陈述简要计划：

```
1. [步骤] → 验证：[检查项]
2. [步骤] → 验证：[检查项]
3. [步骤] → 验证：[检查项]
```

强成功标准让你能独立循环。弱标准（"让它工作"）需要持续澄清。

### 5. 单任务推进

**单次仅执行单个任务模块，完成验收后方可推进下一任务。**

- 即使一次性接到多个任务，也必须拆分为独立模块，逐个推进。
- 当前任务模块未完成或未通过验收前，禁止开始下一个任务。
- 每完成一个模块，必须明确给出验收点（产出物、验证方式或测试结果），等待用户确认或自我验证通过后再继续。
- 多任务推进时，使用 TaskCreate 维护任务列表，状态严格反映当前进度（一次只有一个 in_progress）。
- 验收未通过时，回到当前模块修复，不要绕开问题去做其他任务。

例外：当多个微小操作明确属于同一任务模块的不可分割步骤（如修改同一函数的多处签名）时，可作为一个模块整体提交验收。

---

**这些指南有效的标志：** diff 中不必要的变更更少，因过度复杂导致的重写更少，澄清性问题在实现前提出而非错误后才出现。

---

## 项目知识库

> 跨会话有价值的项目知识，帮助 AI Agent 避免重复犯错。

### 项目信息

- **项目名称**: 社区论坛系统
- **技术栈**: Java 17 / Spring Boot 2.x / MyBatis-Plus / MySQL 8.x / Redis / Vue 3 / Element Plus / Vite
  - JDK 版本：由原定 1.8 放宽为 17（2026-05-23 确认）。Spring Boot 2.7.18 官方支持 JDK 17。
- **项目描述**: 前后端分离的图文社区论坛，含用户前台和管理员后台

### 架构与设计

<!-- 记录架构决策和设计模式 -->

### 构建与运行

```bash
# 后端
cd forum-server
mvn clean install
mvn spring-boot:run

# 前端
cd forum-web
npm install
npm run dev
npm run build
```

### 编码规范

#### Java 规范

1. **类注释要求**：所有类必须包含标准 JavaDoc 注释

   ```java
   /**
    * 类功能描述(说明该类核心职责)
    *
    * @author liuxinsi
    * @date 生成注释的时间
    */
   ```

   规则：

   - 必须使用 JavaDoc 风格 (`/** */`)
   - 必须描述类的主要职责
   - 禁止省略类注释

2. **接口注释要求**：所有接口必须包含标准 JavaDoc 注释

3. **禁止嵌套循环**：代码中禁止使用嵌套循环

4. **技术栈：** Java 17、MySQL 8.x、Spring Boot 2.x、MyBatis-Plus、Redis、Vue 3、Element Plus、Vite、WangEditor

5. 开发环境：Maven、Mysql、JDK 17

#### 数据库规范

1. **禁止危险语句**：禁止生成 `DROP TABLE` / `DROP DATABASE` 语句

2. **参数化查询**：所有 SQL 必须使用参数化查询，禁止字符串拼接

3. **环境验证**：操作前必须验证环境（生产/测试/开发）

4. **二次确认**：危险操作必须添加二次确认机制

### 常见陷阱

- **`mvn spring-boot:run` 在 Windows 下的孤儿 Java 进程**：通过任何"杀 Maven 父进程"的方式（Ctrl+C 在某些终端、TaskStop、父 shell 退出）停止运行时，子 Java 进程会逃逸为孤儿继续监听端口 8080，导致下次启动报 `Port 8080 already in use`。
  - 排查：`netstat -ano | grep ":8080"` 找出 PID，确认 `tasklist | grep <PID>` 是 `java.exe`
  - 清理：`taskkill //F //PID <pid>`
  - 长期方案：开发期间在 IDE 里启动；或为 spring-boot-maven-plugin 配置 `<jvmArguments>` 让子进程随父进程一起退出。
- **Maven `-q` 抑制启动日志**：`mvn spring-boot:run -q` 会让 INFO 全部消失，看不见 "Started ApplicationContext" 这类成功标志。验收启动场景**不要加 `-q`**。
- **Windows mysql CLI 默认连接字符集是 latin1**：手动执行包含中文的 SQL 脚本时必须加 `--default-character-set=utf8mb4`，否则 `INSERT '系统管理员'` 会报 `Data too long for column`（1 字符被算成 3 字节），或者数据被截断后保存。SQL 脚本本身已经是 utf8 编码不能解决问题，这是 client→server 协议层的事。
  - 正确：`mysql -u root -p123 --default-character-set=utf8mb4 forum < seed.sql`
  - 应用端连接通过 JDBC URL 的 `characterEncoding=utf8` 已正确，不受影响。
- **Windows 控制台中文日志乱码**：Windows cmd/PowerShell 默认 GBK，Spring Boot 日志中文（来自 application.yml UTF-8 资源）输出到 stdout 会显示成乱码（如 `�´ο��޸�`）。**不影响业务**，只影响命令行可读性。两种解法：
  - 临时：执行前 `chcp 65001` 切换控制台到 UTF-8
  - 应用层：启动参数加 `-Dfile.encoding=UTF-8`（在 IDE 或 spring-boot-maven-plugin 的 jvmArguments 配置）
- **没有 `@Transactional+@Rollback` 的测试会污染 DB**：使用真 MySQL 跑 `@SpringBootTest` 时，每个写入 user/post 的测试方法都必须挂 `@Transactional` + `@Rollback`，否则插入的 email/nickname/post 会留在库里。下次跑同一个测试，因为 UNIQUE 约束直接报错。修复：所有涉及 register/createPost 的测试加事务回滚；已污染的库用 `DELETE FROM user WHERE email LIKE 'i-%@example.com'` 清理。
- **FULLTEXT 索引在未提交事务里"看不见"刚 INSERT 的数据**：跑 `@Transactional+@Rollback` 测试时，在事务里 `INSERT post` 然后立刻 `MATCH AGAINST` 搜索，FULLTEXT 索引可能仍然看不见这条数据（InnoDB 全文索引的"缓存"机制 + 事务可见性差异）。表现：单元测试 expected `true` actual `false`，但同一段代码用真实流程跑能搜到。修复：搜索类测试**不要**用 `@Transactional+@Rollback`，改用唯一关键词 + try/finally 手工清理。
- **FULLTEXT 索引的 `WITH PARSER ngram` 容易"静默丢失"，且 SHOW INDEX 看不出来**：schema.sql 写了 `FULLTEXT idx_search(title,content) WITH PARSER ngram`，但在某些情况下（早期手工建表、迁移、版本差异）实际索引会落成默认 parser，对中文整段当 1 个 token，搜任何中文词永远 0 结果。表现：英文/纯数字关键词能命中（因为有空格分词），中文关键词全部空结果，但 schema.sql 看上去是对的。
  - 检查方法：`SHOW INDEX FROM post` **不会**告诉你 parser；必须 `SHOW CREATE TABLE post\G`，找到 `FULLTEXT KEY idx_search ... /*!50100 WITH PARSER ngram */`，没看到 `WITH PARSER ngram` 就是丢了。
  - 修复：`ALTER TABLE post DROP INDEX idx_search; ALTER TABLE post ADD FULLTEXT INDEX idx_search (title,content) WITH PARSER ngram;` 不影响数据，立刻生效。
  - 部署新环境后**第一件事**就该跑 `SHOW CREATE TABLE post\G` 验证 ngram 是否落上了。

### 重要决策记录

<!-- 记录重要的技术决策和原因 -->

### 依赖与配置

<!-- 记录关键依赖和配置说明 -->