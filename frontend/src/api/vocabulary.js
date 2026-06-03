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

// 获取待复习单词
export function getReviewWords(params) {
  return request.get('/vocabulary/review', { params })
}

// 获取系统词库（六级词汇）
export function getSystemWords(params) {
  return request.get('/vocabulary/system', { params })
}

// 从系统词库学习单词
export function learnSystemWord(id) {
  return request.post(`/vocabulary/learn/${id}`)
}
