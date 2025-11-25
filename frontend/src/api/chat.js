import client from './client'

export const askQuestion = (payload) => {
  return client.post('/qa', {
    ...payload,
    useKnowledgeBase: true
  })
}

export const searchKnowledge = (query, filters = []) => {
  return client.get('/knowledge/search', {
    params: {
      query,
      filters
    }
  })
}

export const fetchVisualization = (topicId) => {
  return client.get(`/knowledge/topics/${topicId}/visualizations`)
}

export const fetchFilters = () => {
  return client.get('/knowledge/filters')
}

export const streamAsk = async (payload, onChunk, onReference, onDone, onError) => {
  try {
    const response = await fetch('/api/qa/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        ...payload,
        useKnowledgeBase: true
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { value, done } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      // 保留最后一行（可能不完整）在 buffer 中
      buffer = lines.pop()

      for (const line of lines) {
        if (line.startsWith('data:')) {
          // 只去除开头的 'data:' 和紧随其后的一个空格（如果有）
          // 不要使用 trim()，否则会丢弃纯换行符或空格
          let data = line.slice(5)
          if (data.startsWith(' ')) {
            data = data.slice(1)
          }

          // 如果是空字符串（比如 keep-alive），跳过
          if (data.length === 0) continue

          try {
            // 尝试解析 JSON (针对 reference 事件)
            if (data.startsWith('[') || data.startsWith('{')) {
              const parsed = JSON.parse(data)
              // 简单的判断是否是引用数组
              if (Array.isArray(parsed) && parsed.length > 0 && parsed[0].topicTitle) {
                onReference(parsed)
                continue
              }
            }
          } catch (e) {
            // 忽略解析错误，视为普通文本
          }

          // 默认视为文本块
          onChunk(data)
        }
      }
    }

    // 处理剩余的 buffer
    if (buffer && buffer.startsWith('data:')) {
      let data = buffer.slice(5)
      if (data.startsWith(' ')) {
        data = data.slice(1)
      }
      if (data) onChunk(data)
    }

    onDone()
  } catch (error) {
    onError(error)
  }
}
