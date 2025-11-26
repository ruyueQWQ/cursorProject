<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import ChatPanel from './components/ChatPanel.vue'

const route = useRoute()
const showLanding = ref(true)

const handleStart = () => {
  showLanding.value = false
}

const getParticleStyle = (i) => {
  return {
    left: `${Math.random() * 100}%`,
    top: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 5}s`,
    animationDuration: `${5 + Math.random() * 10}s`
  }
}
</script>

<template>
  <!-- 管理后台路由 -->
  <router-view v-if="route.path.startsWith('/admin')" />
  
  <template v-else>
    <!-- 欢迎页 -->
    <Transition name="warp">
      <div v-if="showLanding" class="landing-page">
        <div class="landing-content">
          <div class="logo-container">
            <h1 class="glitch-text" data-text="ALGORITHM">ALGORITHM</h1>
            <h1 class="glitch-text sub" data-text="QA SYSTEM">QA SYSTEM</h1>
          </div>
          <p class="subtitle">分治 · 动态规划 · 贪心 · 回溯</p>
          
          <button class="start-btn" @click="handleStart">
            <span class="btn-content">开启探索</span>
            <div class="btn-glitch"></div>
          </button>
        </div>
        
        <!-- 动态背景 -->
        <div class="cyber-grid"></div>
        <div class="particles">
          <div class="particle" v-for="i in 50" :key="i" :style="getParticleStyle(i)"></div>
        </div>
      </div>
    </Transition>

    <!-- 主界面 -->
    <Transition name="app-enter">
      <div v-if="!showLanding" class="app-shell">
        <header class="app-header">
          <div class="header-content">
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
    </Transition>
  </template>
</template>

<style scoped>
/* Landing Page Styles */
.landing-page {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: #0f172a;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  z-index: 100;
  perspective: 1000px;
}

.landing-content {
  text-align: center;
  z-index: 10;
  position: relative;
}

.logo-container {
  margin-bottom: 2rem;
  transform-style: preserve-3d;
  animation: float 6s ease-in-out infinite;
}

.glitch-text {
  font-size: 5rem;
  font-weight: 900;
  color: #fff;
  letter-spacing: 0.5rem;
  position: relative;
  text-shadow: 2px 2px 0px #4facfe, -2px -2px 0px #f093fb;
  margin: 0;
}

.glitch-text.sub {
  font-size: 2.5rem;
  color: #667eea;
  text-shadow: none;
  letter-spacing: 1rem;
  margin-top: 0.5rem;
  opacity: 0.9;
}

.subtitle {
  color: #94a3b8;
  font-size: 1.2rem;
  letter-spacing: 0.2rem;
  margin-bottom: 4rem;
  opacity: 0;
  animation: fade-in 1s ease 1s forwards;
}

/* Start Button */
.start-btn {
  position: relative;
  padding: 1rem 3rem;
  background: transparent;
  border: 2px solid #667eea;
  color: #fff;
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
  opacity: 0;
  animation: fade-in 1s ease 1.5s forwards;
}

.start-btn:hover {
  background: #667eea;
  box-shadow: 0 0 30px rgba(102, 126, 234, 0.5);
  transform: scale(1.05);
}

.btn-content {
  position: relative;
  z-index: 2;
}

/* Background Effects */
.cyber-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 200%;
  height: 200%;
  background-image: 
    linear-gradient(rgba(102, 126, 234, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(102, 126, 234, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  transform: perspective(500px) rotateX(60deg) translateY(-100px) translateZ(-200px);
  animation: grid-move 20s linear infinite;
  z-index: 1;
}

.particles {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 2;
}

.particle {
  position: absolute;
  width: 2px;
  height: 2px;
  background: #fff;
  border-radius: 50%;
  animation: particle-float linear infinite;
}

/* Animations */
@keyframes grid-move {
  0% { transform: perspective(500px) rotateX(60deg) translateY(0) translateZ(-200px); }
  100% { transform: perspective(500px) rotateX(60deg) translateY(50px) translateZ(-200px); }
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotateX(0); }
  50% { transform: translateY(-20px) rotateX(10deg); }
}

@keyframes particle-float {
  0% { transform: translateY(0); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translateY(-100vh); opacity: 0; }
}

@keyframes fade-in {
  to { opacity: 1; }
}

/* Transitions */
.warp-leave-active {
  transition: all 1.5s cubic-bezier(0.7, 0, 0.3, 1);
}

.warp-leave-to {
  opacity: 0;
  transform: scale(5) translateZ(1000px);
  filter: blur(20px);
}

.app-enter-active {
  transition: all 1.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.app-enter-from {
  opacity: 0;
  transform: translateY(100px) scale(0.9);
}

/* App Shell Styles */
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

.header-content h1 {
  margin: 0;
  font-size: 1.8rem;
  color: #1f2a44;
  background: linear-gradient(135deg, #1f2a44 0%, #667eea 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-content p {
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
  
  .glitch-text {
    font-size: 3rem;
  }
  
  .glitch-text.sub {
    font-size: 1.5rem;
  }
}
</style>
