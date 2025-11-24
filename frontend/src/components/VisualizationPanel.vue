<script setup>
import { storeToRefs } from 'pinia'
import { useChatStore } from '../stores/chatStore'
import MermaidDiagram from './MermaidDiagram.vue'

const chatStore = useChatStore()
const { visualization, visualizationLoading, visualizationError } = storeToRefs(chatStore)

const isVideo = (url) => {
  return url && (url.endsWith('.mp4') || url.endsWith('.webm') || url.endsWith('.mov'))
}
</script>

<template>
  <section class="visual-panel">
    <header>
      <div>
        <h3>算法可视化</h3>
        <p>展示精选算法的核心思想、步骤与复杂度提示</p>
      </div>
      <span v-if="visualization" class="topic-pill">{{ visualization.topicTitle }}</span>
    </header>

    <div v-if="visualizationLoading" class="visual-placeholder">
      正在加载可视化信息…
    </div>
    <div v-else-if="visualizationError" class="visual-placeholder error">
      {{ visualizationError }}
    </div>
    <div v-else-if="!visualization">
      <div class="visual-placeholder">
        点击右侧知识引用即可查看对应算法的可视化提示。
      </div>
    </div>
    <div v-else>
      <article
        v-for="algo in visualization.algorithms"
        :key="algo.id"
        class="viz-card"
      >
        <div class="viz-card__header">
          <h4>{{ algo.name }}</h4>
          <div class="complexity">
            <span>时间：{{ algo.timeComplexity || '—' }}</span>
            <span>空间：{{ algo.spaceComplexity || '—' }}</span>
          </div>
        </div>
        <p class="core-idea">{{ algo.coreIdea }}</p>
        <div class="steps">
          <h5>关键步骤</h5>
          <p>{{ algo.stepBreakdown }}</p>
        </div>
        <pre v-if="algo.codeSnippet" class="code-block"><code>{{ algo.codeSnippet }}</code></pre>
        
        <!-- Mermaid 流程图 -->
        <div v-if="algo.mermaidCode" class="mermaid-wrapper">
          <h5>流程图</h5>
          <MermaidDiagram :chart="algo.mermaidCode" />
        </div>
        
        <!-- 动画演示 -->
        <div v-if="algo.animationUrl" class="animation-section">
          <h5>算法动画演示</h5>
          <video 
            v-if="isVideo(algo.animationUrl)"
            :src="algo.animationUrl" 
            controls 
            class="animation-video"
          >
            您的浏览器不支持视频播放
          </video>
          <img 
            v-else
            :src="algo.animationUrl" 
            alt="算法动画"
            class="animation-gif"
          />
        </div>
        
        <div v-if="algo.visualizationHint" class="hint">
          <strong>可视化建议：</strong>{{ algo.visualizationHint }}
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.visual-panel {
  background: #fff;
  border-radius: 1rem;
  padding: 1.25rem 1.5rem;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}
.topic-pill {
  background: #e0f2fe;
  color: #0369a1;
  padding: 0.35rem 0.9rem;
  border-radius: 999px;
  font-size: 0.85rem;
}
.visual-placeholder {
  border: 1px dashed #cbd5f5;
  border-radius: 0.8rem;
  padding: 1rem;
  text-align: center;
  color: #6b7280;
}
.visual-placeholder.error {
  color: #b91c1c;
  border-color: #fecaca;
  background: #fef2f2;
}
.viz-card {
  border: 1px solid #edf2ff;
  border-radius: 1rem;
  padding: 1rem 1.25rem;
  margin-bottom: 0.8rem;
  background: linear-gradient(145deg, #ffffff, #f8fbff);
}
.viz-card__header {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 0.5rem;
}
.viz-card h4 {
  margin: 0;
  color: #1f2a44;
}
.complexity {
  display: flex;
  gap: 0.75rem;
  font-size: 0.9rem;
  color: #475569;
}
.core-idea {
  margin: 0.6rem 0;
  font-weight: 500;
  color: #2b334d;
}
.steps h5 {
  margin: 0.8rem 0 0.3rem;
  font-size: 0.9rem;
  color: #475569;
}
.steps p {
  margin: 0.3rem 0 0;
  color: #4b5563;
  white-space: pre-wrap;
}
.code-block {
  background: #0f172a;
  color: #e2e8f0;
  padding: 0.75rem;
  border-radius: 0.75rem;
  overflow-x: auto;
  font-size: 0.85rem;
  margin: 0.8rem 0;
}
.hint {
  margin-top: 0.6rem;
  padding: 0.6rem 0.8rem;
  background: #ecfccb;
  border-radius: 0.75rem;
  color: #3f6212;
}
.mermaid-wrapper {
  margin: 1rem 0;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  padding: 1rem;
  background: #fff;
}
.mermaid-wrapper h5 {
  margin: 0 0 0.8rem;
  font-size: 0.9rem;
  color: #475569;
}

/* 动画演示样式 */
.animation-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.animation-section h5 {
  margin: 0 0 1rem;
  font-size: 0.9rem;
  color: #475569;
}

.animation-video,
.animation-gif {
  width: 100%;
  max-height: 400px;
  border-radius: 0.5rem;
  object-fit: contain;
  background: #000;
}

.animation-gif {
  background: #f9fafb;
}
</style>
