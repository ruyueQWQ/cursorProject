<template>
  <div class="admin-dashboard">
    <header class="dashboard-header">
      <h1>算法管理后台</h1>
      <div class="user-info">
        <span>{{ username }}</span>
        <button @click="handleLogout">退出登录</button>
      </div>
    </header>

    <div class="dashboard-content">
      <div class="toolbar">
        <button @click="showCreateDialog = true" class="btn-primary">
          + 新增算法
        </button>
      </div>

      <table class="algorithms-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>主题ID</th>
            <th>时间复杂度</th>
            <th>动画</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="algo in algorithms" :key="algo.id">
            <td>{{ algo.id }}</td>
            <td>{{ algo.name }}</td>
            <td>{{ algo.topicId }}</td>
            <td>{{ algo.timeComplexity }}</td>
            <td>
              <span v-if="algo.animationUrl" class="badge-success">已上传</span>
              <span v-else class="badge-warning">未上传</span>
            </td>
            <td>
              <button @click="handleUploadAnimation(algo)" class="btn-sm">上传动画</button>
              <button @click="handleEdit(algo)" class="btn-sm">编辑</button>
              <button @click="handleDelete(algo.id)" class="btn-sm btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 上传动画对话框 -->
    <div v-if="showUploadDialog" class="modal" @click.self="showUploadDialog = false">
      <div class="modal-content">
        <h2>上传动画 - {{ currentAlgorithm?.name }}</h2>
        <input type="file" @change="handleFileSelect" accept="video/*,image/gif" />
        <div class="modal-actions">
          <button @click="uploadFile" :disabled="!selectedFile || uploading">
            {{ uploading ? '上传中...' : '确认上传' }}
          </button>
          <button @click="showUploadDialog = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchAllAlgorithms, deleteAlgorithm, uploadAnimation } from '../api/admin'

const router = useRouter()
const username = ref(localStorage.getItem('admin_username') || '')
const algorithms = ref([])
const showCreateDialog = ref(false)
const showUploadDialog = ref(false)
const currentAlgorithm = ref(null)
const selectedFile = ref(null)
const uploading = ref(false)

onMounted(async () => {
  await loadAlgorithms()
})

const loadAlgorithms = async () => {
  try {
    const { data } = await fetchAllAlgorithms()
    if (data.success) {
      algorithms.value = data.data
    }
  } catch (error) {
    console.error('加载算法列表失败', error)
  }
}

const handleLogout = () => {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_username')
  router.push('/admin/login')
}

const handleUploadAnimation = (algo) => {
  currentAlgorithm.value = algo
  showUploadDialog.value = true
  selectedFile.value = null
}

const handleFileSelect = (event) => {
  selectedFile.value = event.target.files[0]
}

const uploadFile = async () => {
  if (!selectedFile.value) return
  
  uploading.value = true
  try {
    const { data } = await uploadAnimation(currentAlgorithm.value.id, selectedFile.value)
    if (data.success) {
      alert('上传成功！')
      showUploadDialog.value = false
      await loadAlgorithms()
    } else {
      alert('上传失败：' + data.message)
    }
  } catch (error) {
    alert('上传失败')
  } finally {
    uploading.value = false
  }
}

const handleEdit = (algo) => {
  alert('编辑功能待实现，算法ID: ' + algo.id)
}

const handleDelete = async (id) => {
  if (!confirm('确定要删除这个算法吗？')) return
  
  try {
    const { data } = await deleteAlgorithm(id)
    if (data.success) {
      alert('删除成功')
      await loadAlgorithms()
    }
  } catch (error) {
    alert('删除失败')
  }
}
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background: #f5f7fa;
}

.dashboard-header {
  background: white;
  padding: 1.5rem 2rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard-header h1 {
  margin: 0;
  color: #1f2a44;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.dashboard-content {
  padding: 2rem;
}

.toolbar {
  margin-bottom: 1.5rem;
}

.btn-primary {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  cursor: pointer;
  font-weight: 600;
}

.algorithms-table {
  width: 100%;
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.algorithms-table th,
.algorithms-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.algorithms-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #5f6c92;
}

.btn-sm {
  padding: 0.4rem 0.8rem;
  margin-right: 0.5rem;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 0.25rem;
  cursor: pointer;
  font-size: 0.875rem;
}

.btn-danger {
  color: #ef4444;
  border-color: #ef4444;
}

.badge-success {
  background: #10b981;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  font-size: 0.875rem;
}

.badge-warning {
  background: #f59e0b;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  font-size: 0.875rem;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  min-width: 400px;
}

.modal-content h2 {
  margin-top: 0;
}

.modal-content input[type="file"] {
  margin: 1rem 0;
  width: 100%;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.modal-actions button {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
  font-weight: 600;
}

.modal-actions button:first-child {
  background: #667eea;
  color: white;
}

.modal-actions button:last-child {
  background: #e5e7eb;
}
</style>
