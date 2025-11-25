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
        <button @click="openCreateDialog" class="btn-primary">
          + 新增算法
        </button>
      </div>

      <table class="algorithms-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>主题</th>
            <th>时间复杂度</th>
            <th>动画</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="algo in algorithms" :key="algo.id">
            <td>{{ algo.id }}</td>
            <td>{{ algo.name }}</td>
            <td>{{ getTopicName(algo.topicId) }}</td>
            <td>{{ algo.timeComplexity }}</td>
            <td>
              <span v-if="algo.animationUrl" class="badge-success">已上传</span>
              <span v-else class="badge-warning">未上传</span>
            </td>
            <td>
              <button @click="handleUploadAnimation(algo)" class="btn-sm">上传动画</button>
              <button @click="openEditDialog(algo)" class="btn-sm">编辑</button>
              <button @click="handleDelete(algo.id)" class="btn-sm btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增/编辑算法对话框 -->
    <div v-if="showFormDialog" class="modal" @click.self="closeFormDialog">
      <div class="modal-content form-dialog">
        <h2>{{ editMode ? '编辑算法' : '新增算法' }}</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>主题 *</label>
            <select v-model="formData.topicId" required>
              <option value="">请选择主题</option>
              <option v-for="topic in topics" :key="topic.id" :value="topic.id">
                {{ topic.title }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>算法名称 *</label>
            <input v-model="formData.name" type="text" required placeholder="例如：快速排序" />
          </div>

          <div class="form-group">
            <label>核心思想 *</label>
            <textarea v-model="formData.coreIdea" required rows="3" placeholder="描述算法的核心思想..."></textarea>
          </div>

          <div class="form-group">
            <label>步骤分解 *</label>
            <textarea v-model="formData.stepBreakdown" required rows="4" placeholder="详细描述算法步骤..."></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>时间复杂度 *</label>
              <input v-model="formData.timeComplexity" type="text" required placeholder="例如：O(n log n)" />
            </div>

            <div class="form-group">
              <label>空间复杂度 *</label>
              <input v-model="formData.spaceComplexity" type="text" required placeholder="例如：O(log n)" />
            </div>
          </div>

          <div class="form-group">
            <label>代码示例</label>
            <textarea v-model="formData.codeSnippet" rows="6" placeholder="粘贴代码示例..."></textarea>
          </div>

          <div class="form-group">
            <label>可视化提示</label>
            <textarea v-model="formData.visualizationHint" rows="3" placeholder="描述如何可视化这个算法..."></textarea>
          </div>

          <div class="form-group">
            <label>Mermaid 流程图代码</label>
            <textarea v-model="formData.mermaidCode" rows="6" placeholder="粘贴 Mermaid 代码..."></textarea>
          </div>

          <div class="modal-actions">
            <button type="submit" class="btn-primary" :disabled="submitting">
              {{ submitting ? '提交中...' : (editMode ? '保存' : '创建') }}
            </button>
            <button type="button" @click="closeFormDialog">取消</button>
          </div>
        </form>
      </div>
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
import { 
  fetchAllAlgorithms, 
  createAlgorithm,
  updateAlgorithm,
  deleteAlgorithm, 
  uploadAnimation,
  fetchTopics 
} from '../api/admin'

const router = useRouter()
const username = ref(localStorage.getItem('admin_username') || '')
const algorithms = ref([])
const topics = ref([])
const showFormDialog = ref(false)
const showUploadDialog = ref(false)
const currentAlgorithm = ref(null)
const selectedFile = ref(null)
const uploading = ref(false)
const editMode = ref(false)
const submitting = ref(false)

const formData = ref({
  topicId: '',
  name: '',
  coreIdea: '',
  stepBreakdown: '',
  timeComplexity: '',
  spaceComplexity: '',
  codeSnippet: '',
  visualizationHint: '',
  mermaidCode: ''
})

onMounted(async () => {
  await loadAlgorithms()
  await loadTopics()
})

const loadAlgorithms = async () => {
  try {
    const { data } = await fetchAllAlgorithms()
    if (data.success) {
      algorithms.value = data.data
    }
  } catch (error) {
    console.error('加载算法列表失败', error)
    alert('加载算法列表失败')
  }
}

const loadTopics = async () => {
  try {
    const { data } = await fetchTopics()
    if (data.success) {
      topics.value = data.data
    }
  } catch (error) {
    console.error('加载主题列表失败', error)
  }
}

const getTopicName = (topicId) => {
  const topic = topics.value.find(t => t.id === topicId)
  return topic ? topic.title : topicId
}

const resetForm = () => {
  formData.value = {
    topicId: '',
    name: '',
    coreIdea: '',
    stepBreakdown: '',
    timeComplexity: '',
    spaceComplexity: '',
    codeSnippet: '',
    visualizationHint: '',
    mermaidCode: ''
  }
}

const openCreateDialog = () => {
  editMode.value = false
  resetForm()
  showFormDialog.value = true
}

const openEditDialog = (algo) => {
  editMode.value = true
  currentAlgorithm.value = algo
  formData.value = {
    topicId: algo.topicId || '',
    name: algo.name || '',
    coreIdea: algo.coreIdea || '',
    stepBreakdown: algo.stepBreakdown || '',
    timeComplexity: algo.timeComplexity || '',
    spaceComplexity: algo.spaceComplexity || '',
    codeSnippet: algo.codeSnippet || '',
    visualizationHint: algo.visualizationHint || '',
    mermaidCode: algo.mermaidCode || ''
  }
  showFormDialog.value = true
}

const closeFormDialog = () => {
  showFormDialog.value = false
  resetForm()
  currentAlgorithm.value = null
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (editMode.value) {
      // 编辑模式
      const { data } = await updateAlgorithm(currentAlgorithm.value.id, formData.value)
      if (data.success) {
        alert('更新成功！')
        closeFormDialog()
        await loadAlgorithms()
      } else {
        alert('更新失败：' + data.message)
      }
    } else {
      // 新增模式
      const { data } = await createAlgorithm(formData.value)
      if (data.success) {
        alert('创建成功！')
        closeFormDialog()
        await loadAlgorithms()
      } else {
        alert('创建失败：' + data.message)
      }
    }
  } catch (error) {
    console.error('提交失败', error)
    alert('提交失败：' + error.message)
  } finally {
    submitting.value = false
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

const handleDelete = async (id) => {
  if (!confirm('确定要删除这个算法吗？此操作不可恢复！')) return
  
  try {
    const { data } = await deleteAlgorithm(id)
    if (data.success) {
      alert('删除成功')
      await loadAlgorithms()
    } else {
      alert('删除失败：' + data.message)
    }
  } catch (error) {
    console.error('删除失败', error)
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

.user-info button {
  padding: 0.5rem 1rem;
  border: none;
  background: #e5e7eb;
  border-radius: 0.375rem;
  cursor: pointer;
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
  font-size: 1rem;
}

.btn-primary:hover {
  background: #5568d3;
}

.btn-primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
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

.algorithms-table tbody tr:hover {
  background: #f9fafb;
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

.btn-sm:hover {
  background: #f3f4f6;
}

.btn-danger {
  color: #ef4444;
  border-color: #ef4444;
}

.btn-danger:hover {
  background: #fef2f2;
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
  max-height: 90vh;
  overflow-y: auto;
}

.form-dialog {
  min-width: 600px;
  max-width: 800px;
}

.modal-content h2 {
  margin-top: 0;
  color: #1f2a44;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.form-group input[type="text"],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.625rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-family: inherit;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
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

.modal-actions button:first-child:not(.btn-primary) {
  background: #667eea;
  color: white;
}

.modal-actions button:last-child {
  background: #e5e7eb;
}

.modal-actions button:hover:not(:disabled) {
  opacity: 0.9;
}

.modal-actions button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
