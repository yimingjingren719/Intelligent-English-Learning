import request from './request'

// 分页查询题目
export function getQuestionsPage(params) {
  return request.get('/question/page', { params })
}

// 新增题目
export function addQuestion(data) {
  return request.post('/question', data)
}

// 更新题目
export function updateQuestion(id, data) {
  return request.put(`/question/${id}`, data)
}

// 删除题目
export function deleteQuestion(id) {
  return request.delete(`/question/${id}`)
}

