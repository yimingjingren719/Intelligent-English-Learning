<template>
  <div class="test-result">
    <NavBar />
    <div class="page-container">
      <div class="card-wrapper fade-in" v-if="detail">
        <el-result
          :icon="detail.score >= 60 ? 'success' : 'error'"
          :title="`得分: ${detail.score} 分`"
          :sub-title="`正确 ${detail.correctCount} / ${detail.totalQuestions} 题`"
        >
          <template #extra>
            <el-button type="primary" @click="router.push('/test')">再来一次</el-button>
            <el-button @click="router.push('/dashboard')">返回首页</el-button>
          </template>
        </el-result>
      </div>
      <el-empty v-else description="加载中..." />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { getTestDetailAPI } from '@/api/test'

const route = useRoute()
const router = useRouter()
const detail = ref(null)

onMounted(async () => {
  try {
    const res = await getTestDetailAPI(route.params.sessionId)
    detail.value = res.data
  } catch (e) { /* ignore */ }
})
</script>
