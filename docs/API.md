# API 说明

## 1. 提问接口

- **URL**：`POST /api/qa`
- **Request Body**

```json
{
  "question": "主定理如何分析分治算法复杂度？",
  "contextFilters": ["分治", "主定理"],
  "topK": 4,
  "useKnowledgeBase": true
}
```

- **Response**

```json
{
  "success": true,
  "data": {
    "answer": "……",
    "references": [
      {
        "topicId": 1,
        "topicTitle": "分治策略",
        "snippet": "……",
        "score": 0.93
      }
    ],
    "model": "qwen-plus",
    "latencyMs": 1420
  }
}
```

## 2. 知识导入

- **URL**：`POST /api/knowledge/ingest`
- **说明**：支持批量导入一个主题及多个算法片段
- **Request 示例**

```json
{
  "title": "动态规划",
  "category": "算法思想",
  "overview": "拆分阶段、状态、转移方程……",
  "keywords": ["dp", "最优子结构"],
  "difficultyLevel": 3,
  "algorithms": [
    {
      "name": "最长上升子序列",
      "coreIdea": "dp[i] 表示以 i 结尾的 LIS 长度",
      "steps": "1. 初始化 dp 为 1 ...",
      "complexity": "时间 O(n^2)，空间 O(n)",
      "codeSnippet": "public int lengthOfLIS(int[] nums) { ... }"
    }
  ]
}
```

## 3. 检索验证

- **URL**：`GET /api/knowledge/search`
- **查询参数**
  - `query`：关键词
  - `filters`：可重复传递的标签，如 `filters=分治&filters=复杂度`
  - `topK`：返回片段数量

## 错误返回

统一使用 `ApiResponse`：

```json
{
  "success": false,
  "message": "错误信息"
}
```

