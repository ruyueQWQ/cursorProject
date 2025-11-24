<script setup>
import { ref, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useChatStore } from '../stores/chatStore'
import MessageBubble from './MessageBubble.vue'
import KnowledgeReference from './KnowledgeReference.vue'
import VisualizationPanel from './VisualizationPanel.vue'

const chatStore = useChatStore()
const { messages, references, loading, latency } = storeToRefs(chatStore)
const question = ref('')
const selectedFilters = ref([])

const presetFilters = [
  { label: '分治策略', value: '分治' },
  { label: '动态规划', value: '动态规划' },
  { label: '贪心算法', value: '贪心' },
  { label: '图算法', value: '图' },
  { label: '复杂度分析', value: '复杂度' }
]

const canSend = computed(() => question.value.trim().length > 0 && !loading.value)

const toggleFilter = (value) => {
  if (selectedFilters.value.includes(value)) {
    selectedFilters.value = selectedFilters.value.filter((v) => v !== value)
  } else {
    selectedFilters.value = [...selectedFilters.value, value]
  }
}

const handleSubmit = () => {
  if (!canSend.value) return
  chatStore.askQuestion(question.value, selectedFilters.value)
  question.value = ''
}

const handleReferenceSelect = (reference) => {
  chatStore.loadVisualization(reference.topicId)
}
</script>

<template>
  <div class="chat-wrapper">
    <section class="chat-panel">
      <div class="message-list">
        <MessageBubble
          v-for="(message, index) in messages"
          :key="index"
          :message="message"
        />
        <div v-if="loading" class="typing-indicator">模型正在思考...</div>
      </div>
      <div class="composer">
        <div class="filter-row">
          <span>重点知识：</span>
          <button
            v-for="item in presetFilters"
            :key="item.value"
            type="button"
            class="filter-chip"
            :class="{ active: selectedFilters.includes(item.value) }"
            @click="toggleFilter(item.value)"
          >
            {{ item.label }}
          </button>
        </div>
        <textarea
          v-model="question"
          placeholder="例如：解释主定理如何推导分治算法的时间复杂度？"
          @keydown.enter.exact.prevent="handleSubmit"
        />
        <div class="composer-actions">
          <small v-if="latency">响应耗时：{{ latency }} ms</small>
          <button type="button" :disabled="!canSend" @click="handleSubmit">
            {{ loading ? '生成中...' : '发送' }}
          </button>
        </div>
      </div>
    </section>
    <aside class="insight-column">
      <section class="reference-panel">
        <header>
          <h3>知识引用</h3>
          <p>确保回答基于课程知识库</p>
        </header>
        <div v-if="references.length === 0" class="reference-empty">
          暂无引用，提问时可以启用“重点知识”标签获得检索增强提示。
        </div>
        <KnowledgeReference
          v-for="(ref, idx) in references"
          :key="idx"
          :reference="ref"
          @select="handleReferenceSelect"
        />
      </section>
      <VisualizationPanel />
    </aside>
  </div>
</template>

<style scoped>
.chat-wrapper {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
  height: calc(100vh - 220px);
}
.chat-panel,
.insight-column {
  background: #fff;
  border-radius: 1rem;
  box-shadow: 0 25px 60px rgba(15, 23, 42, 0.05);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
}
.insight-column {
  gap: 1.25rem;
  background: transparent;
  box-shadow: none;
  padding: 0;
}
.reference-panel {
  background: #fff;
  border-radius: 1rem;
  padding: 1.25rem 1.5rem;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.04);
}
.message-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.composer {
  border-top: 1px solid #f0f2f8;
  padding-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.filter-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #5f6c92;
}
.filter-chip {
  border: 1px solid #e0e7ff;
  border-radius: 999px;
  padding: 0.2rem 0.8rem;
  background: transparent;
  cursor: pointer;
  color: #3f51b5;
  transition: all 0.2s;
}
.filter-chip.active {
  background: #3f51b5;
  color: #fff;
}
textarea {
  width: 100%;
  min-height: 100px;
  resize: none;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  padding: 0.75rem 1rem;
  font-size: 1rem;
  font-family: "Microsoft YaHei", sans-serif;
}
.composer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.composer-actions button {
  background: #2663eb;
  color: #fff;
  border: none;
  border-radius: 0.75rem;
  padding: 0.6rem 1.8rem;
  font-size: 1rem;
  cursor: pointer;
  transition: opacity 0.2s;
}
.composer-actions button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.reference-panel header h3 {
  margin: 0;
  color: #1f2a44;
}
.reference-panel header p {
  margin: 0.2rem 0 1rem;
  color: #6b7280;
  font-size: 0.9rem;
}
.reference-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #94a3b8;
  border: 1px dashed #cbd5f5;
  border-radius: 0.75rem;
  padding: 1rem;
}
.typing-indicator {
  font-size: 0.9rem;
  color: #64748b;
  font-style: italic;
}
@media (max-width: 1024px) {
  .chat-wrapper {
    grid-template-columns: 1fr;
    height: auto;
  }
  .insight-column {
    padding: 0 0 1.25rem;
  }
}
</style>

