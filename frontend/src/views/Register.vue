<template>
  <div class="register-page">
    <!-- 左侧品牌区（同登录页） -->
    <div class="register-left">
      <div class="left-content">
        <div class="brand-icon">
          <div class="icon-circle">
            <el-icon :size="48"><Reading /></el-icon>
          </div>
        </div>
        <h1 class="brand-title">智能评测与个性化英语学习平台</h1>
        <p class="brand-subtitle">加入我们，开启你的 AI 驱动英语学习之旅</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><EditPen /></el-icon><span>智能模拟测试</span>
          </div>
          <div class="feature-item">
            <el-icon><TrendCharts /></el-icon><span>能力六维画像</span>
          </div>
          <div class="feature-item">
            <el-icon><ChatDotRound /></el-icon><span>AI 实时答疑</span>
          </div>
          <div class="feature-item">
            <el-icon><Document /></el-icon><span>六级词汇库</span>
          </div>
        </div>
      </div>
      <div class="left-decoration">
        <div class="circle c1"></div>
        <div class="circle c2"></div>
        <div class="circle c3"></div>
      </div>
    </div>

    <!-- 右侧注册区 -->
    <div class="register-right">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>创建账号</h2>
          <p>注册后即可使用全部功能</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleRegister" class="register-form">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名（3-20个字符）" :prefix-icon="User" size="large" class="custom-input" />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" placeholder="昵称" :prefix-icon="UserFilled" size="large" class="custom-input" />
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="form.email" placeholder="邮箱" :prefix-icon="Message" size="large" class="custom-input" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码（6-30个字符）" :prefix-icon="Lock" size="large" show-password class="custom-input" />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" :prefix-icon="Lock" size="large" show-password class="custom-input" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" @click="handleRegister" class="register-btn">
              {{ loading ? '注册中...' : '注 册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
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
import { User, UserFilled, Message, Lock, EditPen, TrendCharts, ChatDotRound, Document } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '', nickname: '', email: '', password: '', confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度为6-30个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.register({
      username: form.username, nickname: form.nickname,
      email: form.email, password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) { /* handled by interceptor */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.register-page { display: flex; min-height: 100vh; overflow: hidden; }

.register-left {
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 70%, #533483 100%);
  display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden; padding: 60px;
}

.left-content { position: relative; z-index: 2; max-width: 480px; }
.brand-icon { margin-bottom: 32px; }

.icon-circle {
  width: 88px; height: 88px; border-radius: 24px;
  background: linear-gradient(135deg, rgba(102,126,234,0.3), rgba(118,75,162,0.3));
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.15);
  display: flex; align-items: center; justify-content: center; color: #fff;
}

.brand-title {
  font-size: 30px; font-weight: 800; color: #fff; line-height: 1.3; margin-bottom: 16px;
  background: linear-gradient(135deg, #a8b8ff, #c4b5fd);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}

.brand-subtitle { font-size: 15px; color: rgba(255,255,255,0.6); line-height: 1.6; margin-bottom: 40px; }

.features { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }

.feature-item {
  display: flex; align-items: center; gap: 10px; padding: 12px 16px;
  background: rgba(255,255,255,0.06); border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.08);
  color: rgba(255,255,255,0.8); font-size: 14px; transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255,255,255,0.12); border-color: rgba(255,255,255,0.2);
  transform: translateY(-2px);
}

.left-decoration { position: absolute; inset: 0; }
.circle { position: absolute; border-radius: 50%; background: radial-gradient(circle, rgba(102,126,234,0.2), transparent); }
.c1 { width: 400px; height: 400px; top: -120px; right: -100px; animation: float 8s ease-in-out infinite; }
.c2 { width: 300px; height: 300px; bottom: -80px; left: -60px; animation: float 10s ease-in-out infinite reverse; }
.c3 { width: 200px; height: 200px; top: 50%; left: 50%; animation: float 6s ease-in-out infinite; }

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(8px, -12px) scale(1.05); }
  66% { transform: translate(-6px, 8px) scale(0.95); }
}

.register-right { width: 520px; display: flex; align-items: center; justify-content: center; background: #fff; padding: 40px; }
.form-wrapper { width: 100%; max-width: 400px; }

.form-header { text-align: center; margin-bottom: 28px; }
.form-header h2 { font-size: 26px; font-weight: 700; color: #1a1a2e; margin-bottom: 8px; }
.form-header p { font-size: 14px; color: #999; }

.register-form { margin-bottom: 20px; }

.custom-input :deep(.el-input__wrapper) {
  border-radius: 10px; box-shadow: 0 0 0 1px #e8e8e8 inset; transition: all 0.3s; padding: 2px 12px;
}

.custom-input :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #667eea inset; }
.custom-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px rgba(102,126,234,0.3) inset; }

.register-btn {
  width: 100%; height: 46px; font-size: 16px; font-weight: 600; border-radius: 10px; border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: all 0.3s;
}

.register-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(102,126,234,0.35); }

.form-footer { text-align: center; font-size: 14px; color: #999; }
.login-link { color: #667eea; font-weight: 600; margin-left: 4px; text-decoration: none; }
.login-link:hover { color: #764ba2; }

@media (max-width: 768px) {
  .register-left { display: none; }
  .register-right { width: 100%; padding: 24px; }
}
</style>
