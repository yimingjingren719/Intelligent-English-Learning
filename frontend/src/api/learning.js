import request from './request'

// 分页查询学习记录
export function getLearningPage(params) {
  return request.get('/learning/page', { params })
}

// 获取学习统计
export function getLearningStatistics() {
  return request.get('/learning/statistics')
}
