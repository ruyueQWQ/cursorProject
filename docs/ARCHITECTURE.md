# 架构设计

## 总览

系统采用前后端分离架构，后端负责知识库管理、检索增强与大模型调用，前端提供课程定制的聊天体验。

```
Vue 3 Chat UI  ──>  Spring Boot 3 API  ──>  MySQL / Embedding
                            │
                            └──>  Aliyun DashScope 大模型
```

## 后端

- **技术栈**：Spring Boot 3.2、MyBatis-Plus、MySQL、RestTemplate、Lombok
- **模块**
  - `KnowledgeService`：负责知识点入库、分片、关键词/语义检索
  - `QaService`：构建提示词、调用 DashScope、记录 QA 日志
  - `DashScopeClient`：统一封装文本生成与 Embedding 调用，提供 API Key 缺省时的模拟兜底
  - `DataBootstrap`：启动时导入示例 YAML 数据，便于开箱体验
- **数据模型**
  - `knowledge_topic`：课程主题元信息
  - `algorithm_detail`：算法步骤、复杂度、代码片段
  - `knowledge_chunk`：对算法描述切片 + 轻量级向量（JSON）
  - `qa_log`：记录问题、回答、引用与耗时，用于评估准确性

## 检索增强流程

1. 接收问题与可选的“重点知识”标签
2. `KnowledgeService` 通过关键词 + 伪向量得分筛出 Top-K 片段
3. 构造 prompt：注入片段 + 回答格式要求
4. `DashScopeClient` 调用 Qwen 模型；若未配置 API Key，返回模拟答案
5. 回传回答、引用片段、模型信息、耗时，同时写入 `qa_log`

> 后续可无缝接入 Milvus、OpenSearch 或 pgvector，只需在 `KnowledgeService` 中替换检索实现。

## 前端

- **技术栈**：Vue 3 + Vite + Pinia + Axios
- **界面**
  - 左侧：聊天记录、输入框、重点知识过滤器
  - 右侧：知识引用卡片、响应耗时
- **状态管理**
  - `chatStore` 维护消息列表、引用、加载态与耗时
  - API 代理 `/api` → `localhost:8080`，亦可通过 `VITE_API_BASE` 自定义

## 运维与扩展

- 所有配置集中在 `application.yml`，DashScope API Key 支持环境变量 `DASHSCOPE_API_KEY`
- `schema.sql` 提供数据库初始化脚本
- 测试：`QaServiceImplTest` 示例展示如何 Mock LLM 依赖
- 扩展建议：
  - 引入真实 embedding 存储与相似度计算
  - 增加用户身份与学习进度跟踪
  - 构建自动化评测脚本，对问答准确率/延迟做回归

