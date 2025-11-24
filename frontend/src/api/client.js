import axios from 'axios'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE ?? '/api',
  timeout: 50000
})

// 请求拦截器 - 添加 token
client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token && config.url.includes('/admin/')) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理错误和 token 过期
client.interceptors.response.use(
  (response) => response,
  (error) => {
    // Token 过期处理
    if (error.response?.status === 401 && window.location.pathname.startsWith('/admin/')) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_username')
      window.location.href = '/admin/login'
    }

    const message = error.response?.data?.message ?? error.message ?? '请求失败'
    return Promise.reject(new Error(message))
  }
)

export default client
