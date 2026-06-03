import request from './request'

// 登录
export function loginAPI(data) {
  return request.post('/user/login', data)
}

// 注册
export function registerAPI(data) {
  return request.post('/user/register', data)
}

// 获取用户信息
export function getUserInfoAPI() {
  return request.get('/user/info')
}

// 更新用户信息
export function updateUserInfoAPI(data) {
  return request.put('/user/info', data)
}
