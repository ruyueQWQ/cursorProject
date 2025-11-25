<script setup>
import MarkdownIt from 'markdown-it'
import MarkdownItKatex from 'markdown-it-katex'
import MarkdownItTable from 'markdown-it-multimd-table'
import { computed } from 'vue'
import 'katex/dist/katex.min.css'

const md = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true
})
md.use(MarkdownItKatex)
md.use(MarkdownItTable, {
  multiline: false,
  rowspan: false,
  headerless: false,
  multibody: true,
  autolabel: true
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
  
  // 调试：打印原始内容（可选）
  // console.log('原始内容：', content)
  
  // 0. 修复表格中重复的分隔符行
  // 移除只包含 | 和 - 的错误分隔符行（保留正确的表头分隔符）
  const lines = content.split('\n')
  const cleanedLines = []
  let inTable = false
  let separatorSeen = false
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    
    // 检测是否是表格行
    if (line.startsWith('|') && line.endsWith('|')) {
      inTable = true
      
      // 检测是否是分隔符行（只包含 |、-、:、空格）
      const isSeparator = /^\|[\s-:|]+\|$/.test(line)
      
      if (isSeparator) {
        // 如果是表格分隔符
        if (!separatorSeen) {
          // 第一个分隔符保留（表头和表体之间）
          cleanedLines.push(lines[i])
          separatorSeen = true
        }
        // 其他分隔符跳过
      } else {
        // 普通表格行
        cleanedLines.push(lines[i])
      }
    } else {
      // 非表格行
      inTable = false
      separatorSeen = false
      cleanedLines.push(lines[i])
    }
  }
  
  content = cleanedLines.join('\n')
  
  // 1. 确保标题前有换行 (#### 标题 -> \n\n#### 标题)
  content = content.replace(/([^\n])(#{1,6}\s)/g, '$1\n\n$2')
  
  // 2. 确保水平分割线前有换行，但不要误伤表格分隔符
  // 只处理不在 | 开头的行
  content = content.replace(/^(?!\|)(.*?)(---)(?!-)/gm, '$1\n\n$2')
  
  // 3. 确保列表项前有换行，但避免误伤数学公式中的减号 (例如 $a - b$)
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

/* 表格样式 */
.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
  font-size: 0.95em;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid #cbd5e1;
  padding: 0.6rem 0.8rem;
  text-align: left;
}

.markdown-body :deep(th) {
  background: #f1f5f9;
  font-weight: 600;
  color: #334155;
}

.markdown-body :deep(tbody tr:nth-child(even)) {
  background: #f8fafc;
}

.markdown-body :deep(tbody tr:hover) {
  background: #f1f5f9;
}

/* 表格中代码的样式 */
.markdown-body :deep(table code) {
  background: #e2e8f0;
  padding: 0.2em 0.4em;
  border-radius: 0.25em;
  font-size: 0.9em;
}

/* 表格中的emoji */
.markdown-body :deep(table) {
  white-space: nowrap;
}
</style>
