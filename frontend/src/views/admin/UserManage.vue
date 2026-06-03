<template>
  <div class="user-manage">
    <NavBar />
    <div class="page-container">
      <div class="card-wrapper fade-in">
        <div class="page-header">
          <h2>用户管理</h2>
        </div>

        <el-table :data="users" v-loading="loading" border stripe empty-text="暂无用户数据">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column prop="nickname" label="昵称" width="150" />
          <el-table-column prop="email" label="邮箱" min-width="200" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 'ADMIN' ? 'danger' : ''">{{ row.role === 'ADMIN' ? '管理员' : '学生' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'success' : 'danger'">
                {{ row.status === 0 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="180" />
        </el-table>

        <el-pagination
          v-if="total > 0"
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchUsers"
          style="margin-top: 16px; justify-content: center;"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import request from '@/api/request'

const loading = ref(false)
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => fetchUsers())

async function fetchUsers() {
  loading.value = true
  try {
    const res = await request.get('/user/list', { params: { page: currentPage.value, size: pageSize.value } })
    users.value = res.data.records
    total.value = res.data.total
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.user-manage { min-height: 100vh; background: #f0f2f5; }
</style>
