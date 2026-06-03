<template>
  <el-menu
    :default-active="activeIndex"
    mode="horizontal"
    :ellipsis="false"
    @select="handleSelect"
    class="navbar"
  >
    <div class="navbar-brand">
      <el-icon :size="28"><Reading /></el-icon>
      <span class="brand-text">智能评测与个性化英语学习平台</span>
    </div>

    <div class="navbar-menu">
      <el-menu-item index="/dashboard">学习仪表盘</el-menu-item>
      <el-menu-item index="/test">在线测试</el-menu-item>
      <el-menu-item index="/error-practice">错题练习</el-menu-item>
      <el-menu-item index="/vocabulary">生词本</el-menu-item>
      <el-menu-item index="/ai-qa">AI答疑</el-menu-item>
      <el-menu-item index="/learning-record">学习记录</el-menu-item>

      <el-sub-menu v-if="isAdmin" index="admin">
        <template #title>管理后台</template>
        <el-menu-item index="/admin">管理首页</el-menu-item>
        <el-menu-item index="/admin/questions">题库管理</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
      </el-sub-menu>
    </div>

    <div class="navbar-user">
      <el-dropdown @command="handleUserCommand">
        <span class="user-info">
          <el-avatar :size="32" :icon="UserFilled" />
          <span class="username">{{ nickname }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-menu>

  <!-- 个人信息编辑弹窗 -->
  <el-dialog v-model="profileVisible" title="个人信息" width="420px">
    <el-form :model="profileForm" label-width="80px">
      <el-form-item label="用户名">
        <el-input v-model="profileForm.username" disabled />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="profileForm.gender">
          <el-radio value="男">男</el-radio>
          <el-radio value="女">女</el-radio>
          <el-radio value="">保密</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="年龄">
        <el-input-number v-model="profileForm.age" :min="1" :max="120" placeholder="年龄" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="个性签名">
        <el-input v-model="profileForm.signature" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="写一句话介绍自己" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="profileVisible = false">取消</el-button>
      <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeIndex = computed(() => route.path)
const isAdmin = computed(() => userStore.isAdmin)
const nickname = computed(() => userStore.nickname)

const profileVisible = ref(false)
const saving = ref(false)
const profileForm = reactive({ username: '', nickname: '', gender: '', age: null, email: '', signature: '' })

function handleSelect(index) {
  router.push(index)
}

function handleUserCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    profileForm.username = userStore.userInfo?.username || ''
    profileForm.nickname = userStore.userInfo?.nickname || ''
    profileForm.gender = userStore.userInfo?.gender || ''
    profileForm.age = userStore.userInfo?.age || null
    profileForm.email = userStore.userInfo?.email || ''
    profileForm.signature = userStore.userInfo?.signature || ''
    profileVisible.value = true
  }
}

async function saveProfile() {
  saving.value = true
  try {
    await userStore.updateInfo({
      nickname: profileForm.nickname,
      gender: profileForm.gender,
      age: profileForm.age,
      email: profileForm.email,
      signature: profileForm.signature
    })
    await userStore.fetchUserInfo()  // 重新拉取确保数据同步
    ElMessage.success('个人信息已更新')
    profileVisible.value = false
  } catch (e) {
    ElMessage.error('更新失败：' + (e.message || '未知错误'))
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 30px;
  color: #667eea;
}

.brand-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.navbar-menu {
  flex: 1;
  display: flex;
  border-bottom: none !important;
}

.navbar-menu .el-menu-item,
.navbar-menu .el-sub-menu {
  border-bottom: none !important;
}

.navbar-user {
  margin-left: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #333;
}
</style>
