import { defineStore } from 'pinia'
import { askQuestion, fetchVisualization } from '../api/chat'

const formatTimestamp = () =>
  new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(new Date())

export const useChatStore = defineStore('chat', {
  state: () => ({
    messages: [
      {
        role: 'assistant',
        content:
          '你好！我是算法设计与分析课程的 AI 助教，可为你解释算法思想、复杂度推导、伪代码示例等内容。请告诉我你的问题。',
        timestamp: formatTimestamp()
      }
    ],
    references: [],
    loading: false,
    latency: null,
    visualization: null,
    visualizationLoading: false,
    visualizationError: null,
    activeTopicId: null
  }),
  actions: {
    async askQuestion(question, filters = []) {
      this.messages.push({
        role: 'user',
        content: question,
        timestamp: formatTimestamp()
      })
      this.loading = true
      try {
        const { data } = await askQuestion({
          question,
          contextFilters: filters,
          topK: 4
        })
        const payload = data.data
        this.references = payload.references ?? []
        this.latency = payload.latencyMs
        if (this.references.length > 0) {
          this.loadVisualization(this.references[0].topicId)
        } else {
          this.visualization = null
          this.activeTopicId = null
        }
        this.messages.push({
          role: 'assistant',
          content: payload.answer,
          timestamp: formatTimestamp()
        })
      } catch (error) {
        this.messages.push({
          role: 'assistant',
          content: `请求失败：${error.message}`,
          timestamp: formatTimestamp()
        })
      } finally {
        this.loading = false
      }
    },
    async streamAskQuestion(question, filters = []) {
      this.messages.push({
        role: 'user',
        content: question,
        timestamp: formatTimestamp()
      })

      const assistantMessageIndex = this.messages.push({
        role: 'assistant',
        content: '', // 初始为空
        timestamp: formatTimestamp()
      }) - 1

      this.loading = true
      // 重置引用
      this.references = []
      this.visualization = null
      this.activeTopicId = null

      const startTime = Date.now()

      await import('../api/chat').then(({ streamAsk }) => {
        streamAsk(
          {
            question,
            contextFilters: filters,
            topK: 4
          },
          (chunk) => {
            // onChunk: 追加文本
            this.messages[assistantMessageIndex].content += chunk
          },
          (references) => {
            // onReference: 更新引用
            this.references = references
            if (this.references.length > 0) {
              this.loadVisualization(this.references[0].topicId)
            }
          },
          () => {
            // onDone
            this.loading = false
            this.latency = Date.now() - startTime
          },
          (error) => {
            // onError
            this.messages[assistantMessageIndex].content += `\n[出错: ${error.message}]`
            this.loading = false
          }
        )
      })
    },
    async loadVisualization(topicId) {
      console.log('loadVisualization called with:', topicId, 'current active:', this.activeTopicId)
      if (!topicId) {
        console.log('Skipping load: topicId is null')
        return
      }
      this.visualizationLoading = true
      this.visualizationError = null
      try {
        const { data } = await fetchVisualization(topicId)
        this.visualization = data.data
        this.activeTopicId = topicId
      } catch (error) {
        this.visualizationError = error.message
      } finally {
        this.visualizationLoading = false
      }
    }
  }
})

