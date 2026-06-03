<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="login-left">
      <div class="left-content">
        <div class="brand-icon">
          <div class="icon-circle">
            <el-icon :size="48"><Reading /></el-icon>
          </div>
        </div>
        <h1 class="brand-title">智能评测与个性化英语学习平台</h1>
        <p class="brand-subtitle">AI 驱动的英语学习助手，精准定位薄弱点，定制专属学习路径</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><EditPen /></el-icon>
            <span>智能模拟测试</span>
          </div>
          <div class="feature-item">
            <el-icon><TrendCharts /></el-icon>
            <span>能力六维画像</span>
          </div>
          <div class="feature-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>AI 实时答疑</span>
          </div>
          <div class="feature-item">
            <el-icon><Document /></el-icon>
            <span>六级词汇库</span>
          </div>
        </div>
      </div>
      <div class="left-decoration">
        <div class="circle c1"></div>
        <div class="circle c2"></div>
        <div class="circle c3"></div>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="login-right">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录您的账号继续学习</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              class="custom-input"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="login-btn"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>

        <div class="demo-hint">
          <el-divider>演示账号</el-divider>
          <div class="demo-accounts">
            <span class="demo-tag">管理员：admin / admin123</span>
            <span class="demo-tag">学生：前往注册</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push(userStore.isAdmin ? '/admin' : '/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  overflow: hidden;
}

/* ====== 左侧品牌区 ====== */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 70%, #533483 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 60px;
}

.left-content {
  position: relative;
  z-index: 2;
  max-width: 480px;
}

.brand-icon { margin-bottom: 32px; }

.icon-circle {
  width: 88px; height: 88px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(102,126,234,0.3), rgba(118,75,162,0.3));
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.15);
  display: flex; align-items: center; justify-content: center;
  color: #fff;
}

.brand-title {
  font-size: 30px; font-weight: 800; color: #fff;
  line-height: 1.3; margin-bottom: 16px;
  background: linear-gradient(135deg, #a8b8ff, #c4b5fd);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}

.brand-subtitle {
  font-size: 15px; color: rgba(255,255,255,0.6); line-height: 1.6; margin-bottom: 40px;
}

.features {
  display: grid; grid-template-columns: 1fr 1fr; gap: 14px;
}

.feature-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 16px;
  background: rgba(255,255,255,0.06);
  border-radius: 10px; border: 1px solid rgba(255,255,255,0.08);
  color: rgba(255,255,255,0.8); font-size: 14px;
  transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255,255,255,0.12);
  border-color: rgba(255,255,255,0.2);
  transform: translateY(-2px);
}

/* 装饰圆形 */
.left-decoration { position: absolute; inset: 0; }

.circle {
  position: absolute; border-radius: 50%;
  background: radial-gradient(circle, rgba(102,126,234,0.2), transparent);
}

.c1 { width: 400px; height: 400px; top: -120px; right: -100px; animation: float 8s ease-in-out infinite; }
.c2 { width: 300px; height: 300px; bottom: -80px; left: -60px; animation: float 10s ease-in-out infinite reverse; }
.c3 { width: 200px; height: 200px; top: 50%; left: 50%; animation: float 6s ease-in-out infinite; }

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(8px, -12px) scale(1.05); }
  66% { transform: translate(-6px, 8px) scale(0.95); }
}

/* ====== 右侧登录区 ====== */
.login-right {
  width: 480px;
  display: flex; align-items: center; justify-content: center;
  background: #fff;
  padding: 40px;
}

.form-wrapper { width: 100%; max-width: 380px; }

.form-header { text-align: center; margin-bottom: 36px; }

.form-header h2 {
  font-size: 26px; font-weight: 700; color: #1a1a2e; margin-bottom: 8px;
}

.form-header p { font-size: 14px; color: #999; }

.login-form { margin-bottom: 24px; }

.custom-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e8e8e8 inset;
  transition: all 0.3s;
  padding: 2px 12px;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #667eea inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102,126,234,0.3) inset;
}

.login-btn {
  width: 100%; height: 46px; font-size: 16px; font-weight: 600;
  border-radius: 10px; border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102,126,234,0.35);
}

.form-footer { text-align: center; font-size: 14px; color: #999; }

.register-link {
  color: #667eea; font-weight: 600; margin-left: 4px; text-decoration: none;
  transition: color 0.2s;
}

.register-link:hover { color: #764ba2; }

.demo-hint { margin-top: 28px; text-align: center; }

.demo-hint :deep(.el-divider__text) {
  font-size: 12px; color: #ccc; background: #fff;
}

.demo-accounts {
  display: flex; flex-direction: column; gap: 4px; margin-top: 8px;
}

.demo-tag {
  font-size: 12px; color: #bbb; padding: 4px 12px;
  background: #f8f9ff; border-radius: 6px;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-left { display: none; }
  .login-right { width: 100%; padding: 24px; }
}
</style>
