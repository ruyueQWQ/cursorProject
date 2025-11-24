# 数据与知识库说明

## 数据来源

- 《算法设计与分析》课程讲义、实验指导、历年复习资料
- 公开教材中的典型算法：分治、动态规划、贪心、回溯、图论、NP 完全性等

> 当前仓库仅附带 `data/core_topics.yaml` 作为示例，可在后续将真实资料整理为同样格式导入。

## YAML 格式

```yaml
- title: 分治策略
  category: 算法思想
  overview: 课程概要
  keywords: [分治, 递归]
  difficultyLevel: 2
  algorithms:
    - name: 归并排序
      coreIdea: >
        描述算法核心思想
      steps: |
        1. 步骤
        2. ...
      complexity: 时间 O(n log n)，空间 O(n)
      codeSnippet: |
        public void mergeSort(...) { ... }
      visualizationHint: 可选
```

## 数据清洗建议

1. **去噪**：统一术语、变量命名、公式记号
2. **结构化**：按照「算法思想 → 步骤 → 复杂度 → 代码 → 可视化」拆分
3. **切片**：控制每个 `knowledge_chunk` 在 200~400 字，确保检索粒度合适
4. **标签化**：关键字覆盖算法类别、适用场景、复杂度级别
5. **向量化**：调用 DashScope Embedding，或使用开源模型离线生成并写入 `embedding_json`

## 后续拓展

- 引入学生提问日志，通过人工标注形成监督数据
- 将复杂推导过程拆解为步骤链，便于模型输出时生成结构化答案
- 配置知识版本号，支持不同学期/教材差异化内容

