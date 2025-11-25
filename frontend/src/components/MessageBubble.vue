<script setup>
import MarkdownIt from 'markdown-it'
import MarkdownItKatex from 'markdown-it-katex'
import { computed } from 'vue'
import 'katex/dist/katex.min.css'

const md = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true
})
md.use(MarkdownItKatex)

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
  
  // 3. 确保列表项前有换行，但避免误伤数学公式中的减号 (例如 $a - b$)
  // 策略：只有当减号前是标点符号(冒号、句号、问号、感叹号)时才认为是列表开始
  content = content.replace(/([:：.。!！?？])\s*(-\s)/g, '$1\n$2')
  
  // 4. 确保代码块前有换行
  content = content.replace(/([^\n])(```)/g, '$1\n\n$2')
  
  // 5. 确保 $$ 公式块前有换行 (避免被挤在同一行)
  content = content.replace(/([^\n])(\$\$)/g, '$1\n\n$2')

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

