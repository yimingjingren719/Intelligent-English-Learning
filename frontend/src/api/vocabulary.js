import request from './request'

// 获取生词本列表
export function getVocabularyPage(params) {
  return request.get('/vocabulary/page', { params })
}

// 添加生词
export function addVocabularyAPI(data) {
  return request.post('/vocabulary', data)
}

// 删除生词
export function deleteVocabularyAPI(id) {
  return request.delete(`/vocabulary/${id}`)
}

// 标记已掌握
export function markMasteredAPI(id) {
  return request.put(`/vocabulary/${id}/master`)
}

