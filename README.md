# 算法设计与分析 · 智能问答系统

基于 **Spring Boot 3 + MyBatis-Plus + MySQL + Vue 3** 构建的课程智能助教，结合阿里云 DashScope 免费大模型，为《算法设计与分析》课程提供检索增强的问答、知识整理与学习支持。

## 功能亮点

- 📚 课程专属知识库：支持导入概念、算法步骤、复杂度等核心内容，并自动生成知识片段。
- 🔎 检索增强生成（RAG）：结合关键词、语义向量检索，将关键片段注入大模型提示词中，保证回答可信可追溯。
- 🤖 智能问答接口：整合阿里云 DashScope 免费大模型（Qwen 系列），提供算法原理解析、复杂度分析、代码示例等多类型回答。
- 💬 前端聊天界面：支持重点知识筛选、引用展示、响应耗时提示等交互。
- 📊 日志与监控：记录问答日志、响应时延，便于评估准确性与优化模型提示词。

## 项目结构

```
.
├─ backend/        # Spring Boot 3 + MyBatis-Plus 服务
├─ frontend/       # Vue 3 + Vite 聊天前端
└─ docs/           # 架构、测试、数据说明
```

## 快速开始

### 1. 准备环境

- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8（创建数据库 `algorithm_qa` 并执行 `backend/src/main/resources/db/schema.sql`）
- 可选：阿里云 DashScope API Key（如未配置将启用本地模拟回答，便于开发调试）

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

环境变量：

```
export DASHSCOPE_API_KEY=你的APIKey
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev -- --host
```

默认 Vite 代理 `/api` 到 `http://localhost:8080`，可在 `vite.config.js` 调整。

## 核心接口

- `POST /api/qa`：提问并返回模型回答 + 引用片段
- `POST /api/knowledge/ingest`：导入课程知识点
- `GET /api/knowledge/search`：调试知识检索效果

详见 `docs/API.md`。

## 研发路线

1. ✅ 基础骨架：Spring Boot + MyBatis-Plus + Vue 3
2. ✅ 知识库模型及 YAML 示例数据
3. ✅ DashScope API 封装 + 模拟兜底
4. ⏳ 引入真实 Embedding 服务、Milvus/pgvector 等向量存储
5. ⏳ 测试评估脚本与可视化报表

更多设计细节，请参阅 `docs/ARCHITECTURE.md` 与 `docs/DATASETS.md`。

