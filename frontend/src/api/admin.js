import client from './client'

export const adminLogin = (credentials) => {
    return client.post('/admin/login', credentials)
}

export const verifyToken = () => {
    return client.get('/admin/verify')
}

export const fetchAllAlgorithms = () => {
    return client.get('/admin/algorithms')
}

export const fetchAlgorithmById = (id) => {
    return client.get(`/admin/algorithms/${id}`)
}

export const createAlgorithm = (data) => {
    return client.post('/admin/algorithms', data)
}

export const updateAlgorithm = (id, data) => {
    return client.put(`/admin/algorithms/${id}`, data)
}

export const deleteAlgorithm = (id) => {
    return client.delete(`/admin/algorithms/${id}`)
}

export const uploadAnimation = (id, file) => {
    const formData = new FormData()
    formData.append('file', file)
    return client.post(`/admin/algorithms/${id}/animation`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

export const fetchTopics = () => {
    return client.get('/admin/algorithms/topics')
}
