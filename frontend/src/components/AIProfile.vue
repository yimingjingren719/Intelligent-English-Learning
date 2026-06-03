<template>
  <div class="ai-profile">
    <div class="profile-header">
      <h3>
        <el-icon color="#667eea"><TrendCharts /></el-icon>
        AI 能力画像
      </h3>
      <el-button size="small" type="primary" link @click="$emit('refresh')" :loading="loading">
        刷新
      </el-button>
    </div>

    <!-- 统计数据 -->
    <el-row :gutter="16" class="profile-stats">
      <el-col :span="6" v-for="stat in statsItems" :key="stat.label">
        <div class="profile-stat">
          <div class="profile-stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
          <div class="profile-stat-label">{{ stat.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- AI分析 -->
    <div v-if="aiAnalysis" class="ai-analysis">
      <div class="ai-response" v-html="renderedAnalysis"></div>
    </div>

    <!-- 进度环 -->
    <div class="progress-section" v-if="profile">
      <div class="progress-item">
        <span class="progress-label">正确率</span>
        <el-progress
          :percentage="Math.round(profile.accuracy || 0)"
          :stroke-width="12"
          :color="accuracyColor"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps({
  profile: { type: Object, default: null },
  loading: { type: Boolean, default: false }
})

defineEmits(['refresh'])

const statsItems = computed(() => {
  if (!props.profile) return []
  const { aiAnalysis, ...rest } = props.profile
  return [
    { label: '总答题', value: rest.totalAnswers || 0, color: '#667eea' },
    { label: '正确数', value: rest.correctAnswers || 0, color: '#67c23a' },
    { label: '测试次数', value: rest.totalTests || 0, color: '#e6a23c' },
    { label: '待掌握错题', value: rest.unmasteredErrors || 0, color: '#f56c6c' }
  ]
})

const aiAnalysis = computed(() => props.profile?.aiAnalysis || '')
const renderedAnalysis = computed(() => marked(aiAnalysis.value || ''))

const accuracyColor = computed(() => {
  const acc = props.profile?.accuracy || 0
  if (acc >= 80) return '#67c23a'
  if (acc >= 60) return '#e6a23c'
  return '#f56c6c'
})
</script>

<style scoped>
.ai-profile { padding: 4px; }

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.profile-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #1a1a2e;
}

.profile-stats { margin-bottom: 20px; }

.profile-stat {
  text-align: center;
  padding: 12px 8px;
  background: #f8f9ff;
  border-radius: 8px;
}

.profile-stat-value {
  font-size: 22px;
  font-weight: 700;
}

.profile-stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.ai-analysis { margin-top: 12px; }

.progress-section { margin-top: 20px; }

.progress-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.progress-label {
  font-size: 14px;
  color: #666;
  min-width: 60px;
}
</style>
