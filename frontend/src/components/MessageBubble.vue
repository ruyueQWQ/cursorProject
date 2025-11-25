<script setup>
import MarkdownIt from 'markdown-it'
import { computed } from 'vue'

const md = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true
})

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const processedContent = computed(() => {
  let content = props.message.content || ''
  
  // 预处理：修复大模型返回的 Markdown 格式问题
  // 1. 确保标题前有换行 (#### 标题 -> \n\n#### 标题)
  content = content.replace(/([^\n])(#{1,6}\s)/g, '$1\n\n$2')
  
  // 2. 确保水平分割线前有换行 (--- -> \n\n---)
  content = content.replace(/([^\n])(---)/g, '$1\n\n$2')
  
  // 3. 确保列表项前有换行 (文本- 列表项 -> 文本\n- 列表项)
  // 注意：这里比较激进，可能会误伤，但针对当前问题是必要的
  content = content.replace(/([^\n])(\s*-\s)/g, '$1\n$2')
  
  // 4. 确保代码块前有换行
  content = content.replace(/([^\n])(```)/g, '$1\n\n$2')

  return md.render(content)
})
</script>

<template>
  <div class="bubble" :class="message.role">
    <div class="bubble-meta">
      <span>{{ message.role === 'user' ? '学生' : '助教' }}</span>
      <small>{{ message.timestamp }}</small>
    </div>
    <div class="markdown-body" v-html="processedContent" />
  </div>
</template>

<style scoped>
.bubble {
  padding: 1rem 1.25rem;
  border-radius: 1rem;
  max-width: 90%;
  background: #f2f5ff;
  align-self: flex-start;
}
.bubble.user {
  background: #e0f2fe;
  align-self: flex-end;
}
.bubble-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.4rem;
  font-size: 0.8rem;
  color: #64748b;
}
.markdown-body {
  margin: 0;
  font-size: 1rem;
  line-height: 1.6;
}
.markdown-body :deep(p) {
  margin: 0.5em 0;
}
.markdown-body :deep(pre) {
  background: #1e293b;
  color: #e2e8f0;
  padding: 1rem;
  border-radius: 0.5rem;
  overflow-x: auto;
}
.markdown-body :deep(code) {
  font-family: Consolas, Monaco, 'Andale Mono', monospace;
}
</style>

