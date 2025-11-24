import client from './client'

export const askQuestion = (payload) => {
  return client.post('/qa', {
    ...payload,
    useKnowledgeBase: true
  })
}

export const searchKnowledge = (query, filters = []) => {
  return client.get('/knowledge/search', {
    params: {
      query,
      filters
    }
  })
}

export const fetchVisualization = (topicId) => {
  return client.get(`/knowledge/topics/${topicId}/visualizations`)
}

