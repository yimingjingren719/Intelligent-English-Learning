import request from './request'

// AI 智能答疑
export function aiChat(data) {
  return request.post('/ai/chat', data)
}

// 获取能力画像
export function getAIProfile() {
  return request.get('/ai/profile')
}

// 获取薄弱点分析
export function getWeakPoints() {
  return request.get('/ai/weak-points')
}

// 获取学习建议
export function getStudyPlan() {
  return request.get('/ai/study-plan')
}
