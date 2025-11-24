<script setup>
import { onMounted, ref, watch, nextTick } from 'vue'
import mermaid from 'mermaid'

const props = defineProps({
  chart: {
    type: String,
    required: true
  }
})

const containerRef = ref(null)

// 初始化 mermaid 配置
mermaid.initialize({
  startOnLoad: false,
  theme: 'default',
  securityLevel: 'loose',
  fontFamily: 'sans-serif'
})

const renderChart = async () => {
  if (!containerRef.value || !props.chart) return

  try {
    const { svg } = await mermaid.render(
      `mermaid-${Date.now()}`,
      props.chart
    )
    containerRef.value.innerHTML = svg
  } catch (error) {
    console.error('Mermaid render error:', error)
    containerRef.value.innerHTML = '<div class="error">图表渲染失败</div>'
  }
}

onMounted(() => {
  renderChart()
})

watch(() => props.chart, () => {
  nextTick(renderChart)
})
</script>

<template>
  <div class="mermaid-container" ref="containerRef"></div>
</template>

<style scoped>
.mermaid-container {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 1rem 0;
  overflow-x: auto;
}
:deep(svg) {
  max-width: 100%;
  height: auto;
}
.error {
  color: #ef4444;
  font-size: 0.9rem;
  padding: 1rem;
  text-align: center;
  background: #fef2f2;
  border-radius: 0.5rem;
}
</style>
