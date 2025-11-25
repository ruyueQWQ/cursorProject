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
          let data = line.slice(5)
          if (data.startsWith(' ')) {
            data = data.slice(1)
          }

          if (data.length === 0) continue

          try {
            // 尝试解析 JSON
            if (data.startsWith('{') || data.startsWith('[')) {
              const parsed = JSON.parse(data)

              // 情况1: 包装的内容对象 {"content": "..."}
              if (parsed.content) {
                onChunk(parsed.content)
                continue
              }

              // 情况2: 引用数组
              if (Array.isArray(parsed) && parsed.length > 0 && parsed[0].topicTitle) {
                onReference(parsed)
                continue
              }
            }
          } catch (e) {
            // 忽略解析错误
          }

          // 降级处理：如果不是 JSON 或解析失败，视为普通文本（兼容旧格式）
          if (!data.startsWith('{') && !data.startsWith('[')) {
            onChunk(data)
          }
        }
      }
    }

    // 处理剩余的 buffer
    if (buffer && buffer.startsWith('data:')) {
      let data = buffer.slice(5)
      if (data.startsWith(' ')) {
        data = data.slice(1)
      }
      try {
        if (data.startsWith('{')) {
          const parsed = JSON.parse(data)
          if (parsed.content) onChunk(parsed.content)
        } else {
          if (data) onChunk(data)
        }
      } catch (e) {
        if (data) onChunk(data)
      }
    }

    onDone()
  } catch (error) {
    onError(error)
  }
}
