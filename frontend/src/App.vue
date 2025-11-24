<script setup>
import { useRoute } from 'vue-router'
import ChatPanel from './components/ChatPanel.vue'

const route = useRoute()
</script>

<template>
  <!-- 管理后台路由 -->
  <router-view v-if="route.path.startsWith('/admin')" />
  
  <!-- 用户端界面 -->
  <div v-else class="app-shell">
    <header class="app-header">
      <div>
        <h1>算法设计与分析 · 智能助教</h1>
        <p>分治、动态规划、贪心、回溯等核心算法的一站式问答助手</p>
      </div>
      <div class="header-badges">
        <span>Spring Boot 3 + MyBatis-Plus</span>
        <span>Aliyun DashScope</span>
      </div>
    </header>
    <main class="app-main">
      <ChatPanel />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: radial-gradient(circle at top, #f5f7ff, #ffffff 45%);
}
.app-header {
  padding: 2rem 3rem 1rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 1.5rem;
}
.app-header h1 {
  margin: 0;
  font-size: 1.8rem;
  color: #1f2a44;
}
.app-header p {
  margin: 0.25rem 0 0;
  color: #4f5b7d;
}
.header-badges {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}
.header-badges span {
  background: #eef2ff;
  color: #3f51b5;
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  font-size: 0.85rem;
  border: 1px solid rgba(63, 81, 181, 0.2);
}
.app-main {
  flex: 1;
  padding: 0 3rem 2rem;
}
@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
