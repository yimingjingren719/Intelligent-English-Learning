import request from './request'

// 分页查询错题
export function getErrorPage(params) {
  return request.get('/error/page', { params })
}

// 获取错题详情
export function getErrorDetail(id) {
  return request.get(`/error/${id}`)
}

// 标记错题已掌握
export function markErrorMastered(id) {
  return request.put(`/error/${id}/master`)
}

// 错题练习
export function getErrorPractice(params) {
  return request.get('/error/practice', { params })
}
