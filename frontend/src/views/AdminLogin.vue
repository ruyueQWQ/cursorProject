<template>
  <div class="admin-login">
    <!-- Animated Background -->
    <div class="animated-background"></div>
    
    <!-- Floating Particles -->
    <div class="particles">
      <div class="particle" v-for="i in 20" :key="i" :style="getParticleStyle(i)"></div>
    </div>
    
    <!-- Login Card -->
    <div class="login-card">
      <div class="card-header">
        <div class="logo-icon">🔐</div>
        <h1>管理员登录</h1>
        <p>算法知识库管理系统</p>
      </div>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>
            <span class="label-icon">👤</span>
            用户名
          </label>
          <input 
            v-model="username" 
            type="text" 
            placeholder="请输入用户名"
            required
          />
        </div>
        
        <div class="form-group">
          <label>
            <span class="label-icon">🔑</span>
            密码
          </label>
          <input 
            v-model="password" 
            type="password" 
            placeholder="请输入密码"
            required
          />
        </div>
        
        <div v-if="error" class="error-message">
          <span class="error-icon">⚠️</span>
          {{ error }}
        </div>
        
        <button type="submit" :disabled="loading" class="login-button">
          <span v-if="!loading">登录</span>
          <span v-else class="loading-spinner">
            <span class="spinner"></span>
            登录中...
          </span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '../api/admin'

const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const getParticleStyle = (index) => {
  const size = Math.random() * 60 + 20
  const left = Math.random() * 100
  const delay = Math.random() * 20
  const duration = Math.random() * 10 + 15
  
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const { data } = await adminLogin({
      username: username.value,
      password: password.value
    })
    
    if (data.success) {
      localStorage.setItem('admin_token', data.data.token)
      localStorage.setItem('admin_username', data.data.username)
      router.push('/admin/dashboard')
    } else {
      error.value = data.message || '登录失败'
    }
  } catch (err) {
    error.value = '登录失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* Animated Background */
.animated-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(-45deg, #667eea, #764ba2, #f093fb, #4facfe);
  background-size: 400% 400%;
  animation: gradient-shift 15s ease infinite;
  z-index: 0;
}

@keyframes gradient-shift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* Floating Particles */
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.particle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(2px);
  animation: float 20s infinite ease-in-out;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.3;
  }
  25% {
    transform: translateY(-100px) rotate(90deg);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-200px) rotate(180deg);
    opacity: 0.3;
  }
  75% {
    transform: translateY(-100px) rotate(270deg);
    opacity: 0.6;
  }
}

/* Login Card */
.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  padding: 3rem;
  border-radius: 1.5rem;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 420px;
  z-index: 10;
  position: relative;
  animation: fade-in-up 0.8s ease-out;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.logo-icon {
  font-size: 3.5rem;
  margin-bottom: 1rem;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.card-header h1 {
  margin: 0 0 0.5rem;
  color: #1f2a44;
  font-size: 1.8rem;
  font-weight: 700;
}

.card-header p {
  margin: 0;
  color: #6b7280;
  font-size: 0.95rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  color: #4b5563;
  font-weight: 600;
  font-size: 0.95rem;
}

.label-icon {
  font-size: 1.1rem;
}

.form-group input {
  width: 100%;
  padding: 0.875rem 1.25rem;
  border: 2px solid #e5e7eb;
  border-radius: 0.75rem;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.error-message {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #dc2626;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
  padding: 0.875rem 1rem;
  background: #fee2e2;
  border-radius: 0.75rem;
  border-left: 4px solid #dc2626;
  animation: shake 0.5s;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10px); }
  75% { transform: translateX(10px); }
}

.error-icon {
  font-size: 1.2rem;
}

.login-button {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 0.75rem;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.login-button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.login-button:hover:not(:disabled)::before {
  width: 300px;
  height: 300px;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 480px) {
  .login-card {
    padding: 2rem 1.5rem;
    margin: 1rem;
  }
  
  .card-header h1 {
    font-size: 1.5rem;
  }
}
</style>
