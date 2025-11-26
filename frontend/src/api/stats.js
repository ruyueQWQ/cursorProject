import client from './client'

/**
 * 记录算法点击
 * @param {number} topicId - 主题ID
 * @param {string} topicTitle - 主题标题
 */
export const logClick = (topicId, topicTitle) => {
    return client.post('/stats/click', { topicId, topicTitle })
}

/**
 * 获取统计仪表板数据
 */
export const fetchDashboardStats = () => {
    return client.get('/stats/dashboard')
}
