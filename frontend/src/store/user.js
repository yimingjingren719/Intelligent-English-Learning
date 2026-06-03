import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginAPI, registerAPI, getUserInfoAPI, updateUserInfoAPI } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const userRole = ref(localStorage.getItem('userRole') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userRole.value === 'ADMIN')
  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  // 登录
  async function login(credentials) {
    const res = await loginAPI(credentials)
    // res 是拦截器处理后的 data，即 { code, message, data: { token, user } }
    const { token: newToken, user } = res.data
    token.value = newToken
    userInfo.value = user
    userRole.value = user.role
    localStorage.setItem('token', newToken)
    localStorage.setItem('userRole', user.role)
    return res
  }

  // 注册
  async function register(data) {
    return await registerAPI(data)
  }

  // 获取用户信息
  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await getUserInfoAPI()
      userInfo.value = res.data
      userRole.value = userInfo.value.role
      localStorage.setItem('userRole', userInfo.value.role)
    } catch {
      // token过期，清除登录状态
      logout()
    }
  }

  // 更新用户信息
  async function updateInfo(data) {
    const res = await updateUserInfoAPI(data)
    userInfo.value = res.data
    return res
  }

  // 恢复登录状态
  async function restoreLogin() {
    if (token.value) {
      await fetchUserInfo()
    }
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = null
    userRole.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
  }

  return {
    token,
    userInfo,
    userRole,
    isLoggedIn,
    isAdmin,
    username,
    nickname,
    login,
    register,
    fetchUserInfo,
    updateInfo,
    restoreLogin,
    logout
  }
})
