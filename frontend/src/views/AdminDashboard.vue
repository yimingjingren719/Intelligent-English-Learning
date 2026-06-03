<template>
  <div class="admin-dashboard">
    <NavBar />
    <div class="page-container">
      <div class="page-header fade-in">
        <h2>管理后台</h2>
        <p>题库管理与系统概览</p>
      </div>

      <div class="card-wrapper fade-in">
        <el-row :gutter="20">
          <el-col :sm="12" v-for="item in adminMenus" :key="item.path">
            <div class="admin-card" @click="router.push(item.path)">
              <el-icon :size="40" :color="item.color">
                <component :is="item.icon" />
              </el-icon>
              <div class="admin-card-info">
                <h4>{{ item.title }}</h4>
                <p>{{ item.desc }}</p>
              </div>
              <el-icon><ArrowRight /></el-icon>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()

const adminMenus = [
  { title: '题库管理', desc: '增删改查题目，管理题库内容', path: '/admin/questions', icon: 'Document', color: '#667eea' },
  { title: '用户管理', desc: '查看和管理注册用户', path: '/admin/users', icon: 'User', color: '#11998e' }
]
</script>

<style scoped>
.admin-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px 24px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #eee;
  margin-bottom: 16px;
}

.admin-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.08);
  border-color: #667eea;
}

.admin-card-info h4 { font-size: 18px; color: #1a1a2e; margin-bottom: 6px; }
.admin-card-info p { font-size: 13px; color: #999; }
</style>
