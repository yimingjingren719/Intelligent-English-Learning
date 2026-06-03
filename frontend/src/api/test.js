import request from './request'

// 生成测试
export function generateTestAPI(params) {
  return request.get('/test/generate', { params })
}

// 提交测试
export function submitTestAPI(data) {
  return request.post('/test/submit', data)
}

// 获取测试详情
export function getTestDetailAPI(sessionId) {
  return request.get(`/test/detail/${sessionId}`)
}
