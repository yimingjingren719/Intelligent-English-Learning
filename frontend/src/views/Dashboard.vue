<template>
  <div class="dashboard">
    <NavBar />
    <div class="page-container">
      <div class="page-header fade-in" style="display:flex;justify-content:space-between;align-items:center">
        <div>
          <h2>欢迎回来，{{ userStore.nickname }} 👋</h2>
          <p>以下是您的学习概览 <span v-if="lastUpdate" style="color:#999;font-size:12px">· 更新于 {{ lastUpdate }}</span></p>
        </div>
        <el-button @click="loadAll()" :loading="aiLoading" size="small">🔄 刷新数据</el-button>
      </div>

      <!-- 筛选 -->
      <div class="card-wrapper fade-in" style="margin-bottom:16px">
        <el-radio-group v-model="recentFilter" @change="onFilterChange">
          <el-radio-button value="0">全部数据</el-radio-button>
          <el-radio-button v-for="n in [5,10,15,20]" :key="n" :value="String(n)">最近{{ n }}题</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid fade-in">
        <div class="stat-card"><div class="number">{{ stats.accuracy }}%</div><div class="label">总正确率</div></div>
        <div class="stat-card green"><div class="number">{{ stats.totalAnswers || 0 }}</div><div class="label">总答题数</div></div>
        <div class="stat-card orange"><div class="number">{{ stats.unmasteredErrors || 0 }}</div><div class="label">待掌握错题</div></div>
        <div class="stat-card blue"><div class="number">{{ stats.averageScore || 0 }}</div><div class="label">平均得分</div></div>
      </div>

      <!-- 能力六维面板 + 题型柱状图 -->
      <el-row :gutter="16" class="fade-in">
        <el-col :xs="24" :md="12">
          <div class="card-wrapper">
            <h3>🎯 能力六维面板</h3>
            <div ref="radarChart" style="width:100%;height:380px"></div>
            <div class="radar-legend">
              <span v-for="lvl in ['S','A','B','C','D']" :key="lvl" class="level-tag" :class="'lvl-'+lvl.toLowerCase()">{{ lvl }}级</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :md="12">
          <div class="card-wrapper">
            <h3>📊 各题型正确率</h3>
            <div ref="barChart" style="width:100%;height:380px"></div>
          </div>
        </el-col>
      </el-row>

      <!-- 最近模拟试卷分数 -->
      <div class="card-wrapper fade-in" style="margin-top:16px">
        <h3>📈 最近模拟试卷分数</h3>
        <div ref="scoreChart" style="width:100%;height:260px" v-if="recentScores.length > 0"></div>
        <el-empty v-else description="暂无模拟试卷数据" />
      </div>

      <!-- AI 薄弱点分析 + 提升建议 -->
      <div class="card-wrapper fade-in" style="margin-top:16px">
        <h3>🔍 薄弱点分析与提升建议</h3>
        <div class="ai-response" v-if="aiAnalysis" v-html="aiAnalysis"></div>
        <el-button v-else type="primary" @click="loadAI" :loading="aiLoading">生成AI分析</el-button>
      </div>

      <!-- 快捷入口 -->
      <div class="card-wrapper fade-in" style="margin-top:16px">
        <h3>快捷入口</h3>
        <el-row :gutter="16" style="margin-top:12px">
          <el-col :xs="12" :sm="6" v-for="a in quickActions" :key="a.title">
            <div class="quick-action-card" @click="router.push(a.path)">
              <el-icon :size="28" :color="a.color"><component :is="a.icon" /></el-icon>
              <span class="action-title">{{ a.title }}</span>
              <span class="action-desc">{{ a.desc }}</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import NavBar from '@/components/NavBar.vue'
import request from '@/api/request'
import * as echarts from 'echarts'
import { EditPen, ChatDotRound, Document, TrendCharts } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const recentFilter = ref('0')
const aiAnalysis = ref('')
const aiLoading = ref(false)
const lastUpdate = ref('')
let autoTimer = null

const radarChart = ref(null); const barChart = ref(null); const scoreChart = ref(null)
let radarInstance, barInstance, scoreInstance

const stats = reactive({ accuracy: 0, totalAnswers: 0, unmasteredErrors: 0, averageScore: 0 })
let detailData = null; const recentScores = ref([])

const quickActions = [
  { title: '在线测试', desc: '模拟考试练习', path: '/test', icon: 'EditPen', color: '#667eea' },
  { title: '错题练习', desc: '专攻薄弱环节', path: '/error-practice', icon: 'Document', color: '#f5576c' },
  { title: 'AI答疑', desc: '智能问答助手', path: '/ai-qa', icon: 'ChatDotRound', color: '#11998e' },
  { title: '学习记录', desc: '查看学习历程', path: '/learning-record', icon: 'TrendCharts', color: '#4facfe' }
]

// Grade: S≥80 A≥60 B≥40 C≥20 D<20
function getGrade(v) { if (v >= 80) return 'S'; if (v >= 60) return 'A'; if (v >= 40) return 'B'; if (v >= 20) return 'C'; return 'D' }
// Study freq grade: S≥10题/天 A≥7 B≥5 C≥3 D<3
function getFreqGrade(v) { if (v >= 10) return 'S'; if (v >= 7) return 'A'; if (v >= 5) return 'B'; if (v >= 3) return 'C'; return 'D' }
const typeLabelsMap = { BANKED_CLOZE: '选词填空', LONG_MATCH: '长篇匹配', CAREFUL_READING: '仔细阅读' }


onMounted(() => { loadAll(); startAutoRefresh() })
onUnmounted(() => { clearInterval(autoTimer); document.removeEventListener('visibilitychange', onVisibilityChange) })

// 双重保障：watch + @change 确保筛选变化时更新
watch(recentFilter, () => { loadAll() })

function onFilterChange(val) {
  loadAll()
}

function startAutoRefresh() {
  autoTimer = setInterval(() => loadAll(), 60000)
  document.addEventListener('visibilitychange', onVisibilityChange)
}

function onVisibilityChange() {
  if (!document.hidden) loadAll()
}

async function loadAll() {
  aiAnalysis.value = ''
  const params = recentFilter.value !== '0' ? { recentCount: parseInt(recentFilter.value) } : {}
  try {
    const statRes = await request.get('/learning/statistics', { params })
    Object.assign(stats, statRes.data)
    lastUpdate.value = new Date().toLocaleTimeString()
  } catch (e) { /* ignore */ }
  try {
    const detailRes = await request.get('/learning/statistics/detail', { params })
    detailData = detailRes.data
    recentScores.value = detailData.recentScores || []
    nextTick(() => { renderRadar(); renderBar(); if (recentScores.value.length > 0) renderScoreChart() })
    generateAIAnalysis()
  } catch (e) { /* ignore */ }
}

function renderRadar() {
  if (!detailData) return; if (!radarChart.value) return
  if (radarInstance) radarInstance.dispose()
  radarInstance = echarts.init(radarChart.value)

  const ts = detailData.typeStats || {}
  const maxScore = Math.max(0, ...(detailData.recentScores || []).map(s => s.score || 0))
  const examVal = maxScore > 0 ? Math.min(100, maxScore / 240 * 100) : 0
  const freqVal = Math.min(100, (detailData.dailyAvgExercises || 0) / 10 * 100)

  const dimNames = ['选词填空', '长篇匹配', '仔细阅读', '模拟分数', '学习频率', '错题掌握']
  const values = [
    Math.round((ts.BANKED_CLOZE?.accuracy || 0) * 10) / 10,
    Math.round((ts.LONG_MATCH?.accuracy || 0) * 10) / 10,
    Math.round((ts.CAREFUL_READING?.accuracy || 0) * 10) / 10,
    Math.round(examVal * 10) / 10,
    Math.round(freqVal * 10) / 10,
    Math.round((detailData.errorMasteryRate || 0) * 10) / 10
  ]
  const grades = [
    getGrade(values[0]), getGrade(values[1]), getGrade(values[2]),
    getGrade(values[3]), getFreqGrade(detailData.dailyAvgExercises || 0), getGrade(values[5])
  ]

  radarInstance.setOption({
    tooltip: { trigger: 'item', formatter: (p) => p.name + ': ' + p.value + '%' },
    radar: {
      center: ['50%', '48%'], radius: '65%',
      indicator: dimNames.map((n, i) => ({
        name: n + '\n[' + grades[i] + ']',
        max: 100
      })),
      axisName: { fontSize: 10, color: '#666' }
    },
    series: [{
      type: 'radar',
      data: [{
        value: values, name: '能力画像',
        areaStyle: { color: 'rgba(102,126,234,0.25)' },
        lineStyle: { color: '#667eea', width: 2 },
        itemStyle: { color: '#667eea' }
      }]
    }]
  })
}

function renderBar() {
  if (!detailData) return; if (!barChart.value) return
  if (barInstance) barInstance.dispose()
  barInstance = echarts.init(barChart.value)
  const ts = detailData.typeStats || {}
  const types = ['BANKED_CLOZE', 'LONG_MATCH', 'CAREFUL_READING']
  const names = ['选词填空\n(每篇均分)', '长篇匹配\n(每篇均分)', '仔细阅读\n(总正确率)']
  const accuracies = types.map(t => ts[t]?.accuracy || 0)
  const counts = [ts.BANKED_CLOZE?.exerciseCount || 0, ts.LONG_MATCH?.exerciseCount || 0, ts.CAREFUL_READING?.total || 0]
  const grades = accuracies.map(getGrade)

  barInstance.setOption({
    tooltip: { trigger: 'axis', formatter: (ps) => ps.map(p => p.seriesName + ': ' + p.value + (p.seriesIndex === 0 ? '%' : '题')).join('<br>') },
    xAxis: { type: 'category', data: names },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [
      { name: '正确率', type: 'bar', data: accuracies.map((v, i) => ({ value: v, itemStyle: { color: ['#667eea','#e6a23c','#67c23a'][i] } })),
        label: { show: true, formatter: (p) => p.value + '% [' + grades[p.dataIndex] + ']' }, barWidth: '50%' },
      { name: '完成数', type: 'line', yAxisIndex: 0, data: counts, itemStyle: { color: '#f5576c' },
        label: { show: true, formatter: '{c}篇/题' } }
    ],
    legend: { bottom: 0 }
  })
}

function renderScoreChart() {
  if (!scoreChart.value) return; if (scoreInstance) scoreInstance.dispose()
  scoreInstance = echarts.init(scoreChart.value)
  const scores = recentScores.value.slice().reverse()
  scoreInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: scores.map(s => s.time) },
    yAxis: { type: 'value', name: '分数' },
    series: [{ type: 'line', data: scores.map(s => s.score), smooth: true, itemStyle: { color: '#667eea' }, areaStyle: { color: 'rgba(102,126,234,0.15)' }, label: { show: true } }]
  })
}

function generateAIAnalysis() {
  if (!detailData) return
  const ts = detailData.typeStats || {}
  const bcA = ts.BANKED_CLOZE?.accuracy || 0
  const lm = ts.LONG_MATCH?.accuracy || 0
  const cr = ts.CAREFUL_READING?.accuracy || 0

  let weak = []; if (bcA < 60) weak.push(`选词填空正确率仅${bcA}%`)
  if (lm < 60) weak.push(`长篇匹配正确率仅${lm}%`)
  if (cr < 60) weak.push(`仔细阅读正确率仅${cr}%`)

  const weakText = weak.length ? '主要薄弱点：' + weak.join('；') + '。' : '各题型表现较为均衡。'
  const best = Math.max(bcA, lm, cr)
  const bestType = bcA >= lm && bcA >= cr ? '选词填空' : lm >= cr ? '长篇匹配' : '仔细阅读'
  const worstType = bcA <= lm && bcA <= cr ? '选词填空' : lm <= cr ? '长篇匹配' : '仔细阅读'
  const suggest = `建议重点加强<b>${worstType}</b>练习，每周至少完成2篇${worstType}专项训练。同时保持<b>${bestType}</b>的优势。`

  aiAnalysis.value = marked(`
### 📊 薄弱点分析
${weakText}根据最近${recentFilter.value === '0' ? '全部' : recentFilter.value}次练习数据，你的**${worstType}**存在明显短板，需要针对性训练。

### 💡 提升建议
1. **集中攻克弱项**：${suggest}
2. **模拟考试**：每周至少完成1次完整模拟试卷，适应考试节奏
3. **错题回顾**：定期复习错题本中的高频错误，确保同类错误不再重复
4. **生词积累**：利用生词本和六级词库，每天学习15-20个新单词
5. **限时训练**：模拟真实考试时间限制，提高答题速度
  `)
}

import { marked } from 'marked'
</script>

<style scoped>
.dashboard { min-height: 100vh; background: #f0f2f5; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 16px; }
.radar-legend { display: flex; gap: 12px; justify-content: center; margin-top: 8px; }
.level-tag { padding: 2px 12px; border-radius: 10px; font-size: 12px; font-weight: 600; }
.lvl-s { background: #f0f9eb; color: #67c23a; } .lvl-a { background: #eef0ff; color: #667eea; }
.lvl-b { background: #fef8e8; color: #e6a23c; } .lvl-c { background: #fef0f0; color: #f56c6c; }
.lvl-d { background: #f5f5f5; color: #999; }
.quick-action-card { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 20px 12px; border-radius: 12px; background: #f8f9ff; cursor: pointer; transition: all 0.3s; border: 1px solid transparent; }
.quick-action-card:hover { background: #eef0ff; border-color: #667eea; transform: translateY(-2px); }
.action-title { font-size: 15px; font-weight: 600; color: #1a1a2e; }
.action-desc { font-size: 12px; color: #999; }
.ai-response { line-height: 1.9; }
.ai-response :deep(h3) { margin: 12px 0 8px; color: #1a1a2e; }
.ai-response :deep(li) { margin: 4px 0 4px 20px; }
</style>
