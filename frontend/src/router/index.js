import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', noAuth: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '学习仪表盘' }
  },
  {
    path: '/test',
    name: 'Test',
    component: () => import('@/views/Test.vue'),
    meta: { title: '在线测试' }
  },
  {
    path: '/test/result/:sessionId',
    name: 'TestResult',
    component: () => import('@/views/TestResult.vue'),
    meta: { title: '测试结果' }
  },
  {
    path: '/error-practice',
    name: 'ErrorPractice',
    component: () => import('@/views/ErrorPractice.vue'),
    meta: { title: '错题练习' }
  },
  {
    path: '/vocabulary',
    name: 'Vocabulary',
    component: () => import('@/views/Vocabulary.vue'),
    meta: { title: '生词本' }
  },
  {
    path: '/ai-qa',
    name: 'AIQA',
    component: () => import('@/views/AIQA.vue'),
    meta: { title: 'AI智能答疑' }
  },
  {
    path: '/learning-record',
    name: 'LearningRecord',
    component: () => import('@/views/LearningRecord.vue'),
    meta: { title: '学习记录' }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/AdminDashboard.vue'),
    meta: { title: '管理后台', role: 'ADMIN' }
  },
  {
    path: '/admin/questions',
    name: 'QuestionManage',
    component: () => import('@/views/admin/QuestionManage.vue'),
    meta: { title: '题库管理', role: 'ADMIN' }
  },
  {
    path: '/admin/users',
    name: 'UserManage',
    component: () => import('@/views/admin/UserManage.vue'),
    meta: { title: '用户管理', role: 'ADMIN' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 智能评测与个性化英语学习平台` : '智能评测与个性化英语学习平台'

  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')

  // 不需要认证的页面
  if (to.meta.noAuth) {
    if (token && (to.name === 'Login' || to.name === 'Register')) {
      // 已登录用户访问登录页，重定向到首页
      next('/dashboard')
      return
    }
    next()
    return
  }

  // 需要认证但未登录
  if (!token) {
    next('/login')
    return
  }

  // 需要管理员角色
  if (to.meta.role === 'ADMIN' && userRole !== 'ADMIN') {
    next('/dashboard')
    return
  }

  next()
})

export default router
