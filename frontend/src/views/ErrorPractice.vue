<template>
  <div class="error-practice">
    <NavBar />
    <div class="page-container">
      <div class="page-header fade-in">
        <h2>错题练习</h2>
        <p>专攻您的薄弱环节，巩固知识点</p>
      </div>

      <!-- 错题列表 -->
      <div v-if="!practicing">
        <div class="card-wrapper fade-in">
          <div class="section-header">
            <h3>我的错题本</h3>
            <el-button type="primary" @click="startPractice" :disabled="!errorList.length">
              开始练习 ({{ errorList.length }}题)
            </el-button>
          </div>

          <el-table :data="errorList" style="width: 100%" v-loading="loading" empty-text="暂无错题，继续保持！">
            <el-table-column prop="questionId" label="题目ID" width="80" />
            <el-table-column label="错误次数" width="100">
              <template #default="{ row }">
                <el-tag type="danger">{{ row.errorCount }}次</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最近错误答案" width="150">
              <template #default="{ row }">
                <span class="wrong-answer">{{ row.lastWrongAnswer }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="lastPracticeTime" label="最后练习时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="viewDetail(row.id)">查看详情</el-button>
                <el-button size="small" type="success" link @click="markMastered(row.id)">已掌握</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="total > 0"
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchErrors"
            style="margin-top: 16px; justify-content: center;"
          />
        </div>
      </div>

      <!-- 练习模式 -->
      <div v-if="practicing">
        <div class="test-progress card-wrapper">
          <div class="progress-info">
            <span>第 {{ practiceIndex + 1 }} / {{ practiceQuestions.length }} 题</span>
          </div>
          <el-progress :percentage="practicePercent" :stroke-width="8" color="#f5576c" />
        </div>

        <div class="card-wrapper question-card" v-if="currentPQ">
          <div class="question-content" v-html="currentPQ.content"></div>

          <div v-if="(currentPQ.questionType === 'SINGLE_CHOICE' || currentPQ.questionType === 'READING') && pOptions.length > 0">
            <el-radio-group v-model="practiceAnswer" class="options-group">
              <div v-for="opt in pOptions" :key="opt.label" class="option-item">
                <el-radio :value="opt.label">{{ opt.label }}. {{ opt.text }}</el-radio>
              </div>
            </el-radio-group>
          </div>

          <div v-if="currentPQ.questionType === 'TRUE_FALSE'">
            <el-radio-group v-model="practiceAnswer" class="options-group">
              <div class="option-item"><el-radio value="TRUE">正确 (True)</el-radio></div>
              <div class="option-item"><el-radio value="FALSE">错误 (False)</el-radio></div>
            </el-radio-group>
          </div>

          <div class="question-actions">
            <el-button @click="checkAnswer" :disabled="!practiceAnswer">检查答案</el-button>
            <el-button type="primary" @click="nextPractice" :disabled="!checked">
              {{ practiceIndex < practiceQuestions.length - 1 ? '下一题' : '完成练习' }}
            </el-button>
          </div>

          <div v-if="checked" class="check-result" :class="isCorrect ? 'correct-bg' : 'wrong-bg'">
            <p><strong>{{ isCorrect ? '✅ 回答正确！' : '❌ 回答错误' }}</strong></p>
            <p>正确答案：<strong>{{ currentPQ.correctAnswer }}</strong></p>
            <p v-if="currentPQ.analysis">解析：{{ currentPQ.analysis }}</p>
          </div>
        </div>
      </div>

      <!-- 错题详情弹窗 -->
      <el-dialog v-model="dialogVisible" title="错题详情 — 原题回顾" width="750px">
        <div v-if="detailQuestion" v-loading="detailLoading">
          <!-- 阅读理解文章 -->
          <div v-if="detailPassage" class="card-wrapper reading-passage" style="margin-bottom:12px">
            <el-alert title="📖 阅读文章" type="info" :closable="false" style="margin-bottom:8px" />
            <div class="passage-content" v-html="detailPassage.content"></div>
          </div>
          <!-- 原大题（选词填空/长篇匹配的完整内容） -->
          <div class="question-content" v-html="detailQuestion.content" style="margin-bottom:12px"></div>
          <p><strong>正确答案：</strong>{{ formatAnswer(detailQuestion) }}</p>
          <p v-if="detailQuestion.analysis"><strong>解析：</strong>{{ detailQuestion.analysis }}</p>
          <!-- 关联小题 -->
          <div v-if="detailRelated && detailRelated.length" style="margin-top:12px">
            <h4>同篇其它题目：</h4>
            <div v-for="(rq, ri) in detailRelated" :key="ri" style="padding:8px;margin:4px 0;background:#f8f9ff;border-radius:6px">
              <span v-html="rq.content"></span>
              <p><strong>答案：</strong>{{ formatAnswer(rq) }}</p>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import { getErrorPage, getErrorDetail, markErrorMastered, getErrorPractice } from '@/api/error'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const errorList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 练习模式
const practicing = ref(false)
const practiceQuestions = ref([])
const practiceIndex = ref(0)
const practiceAnswer = ref('')
const checked = ref(false)
const isCorrect = ref(false)

// 详情弹窗
const dialogVisible = ref(false)
const detailQuestion = ref(null)
const detailPassage = ref(null)
const detailRelated = ref([])
const detailLoading = ref(false)

const currentPQ = computed(() => practiceQuestions.value[practiceIndex.value] || null)
const practicePercent = computed(() =>
  practiceQuestions.value.length > 0 ? Math.round((practiceIndex.value + 1) / practiceQuestions.value.length * 100) : 0
)
const pOptions = computed(() => {
  if (!currentPQ.value?.options) return []
  try {
    return typeof currentPQ.value.options === 'string'
      ? JSON.parse(currentPQ.value.options) : currentPQ.value.options
  } catch { return [] }
})

onMounted(() => fetchErrors())

async function fetchErrors() {
  loading.value = true
  try {
    const res = await getErrorPage({ page: currentPage.value, size: pageSize.value })
    errorList.value = res.data.records
    total.value = res.data.total
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

async function startPractice() {
  try {
    const res = await getErrorPractice({ count: errorList.value.length })
    practiceQuestions.value = res.data.questions
    practicing.value = true
    practiceIndex.value = 0
    practiceAnswer.value = ''
    checked.value = false
  } catch (e) { /* ignore */ }
}

function checkAnswer() {
  checked.value = true
  isCorrect.value = practiceAnswer.value.trim().toUpperCase() ===
    (currentPQ.value.correctAnswer || '').trim().toUpperCase()
}

function nextPractice() {
  if (practiceIndex.value < practiceQuestions.value.length - 1) {
    practiceIndex.value++
    practiceAnswer.value = ''
    checked.value = false
  } else {
    practicing.value = false
    ElMessage.success('练习完成！')
    fetchErrors()
  }
}

function getOptions(q) {
  if (!q?.options) return []
  try { return typeof q.options === 'string' ? JSON.parse(q.options) : q.options } catch { return [] }
}

function formatAnswer(q) {
  if (!q) return ''
  const answer = q.correctAnswer || ''
  // 选词填空/长篇匹配：逗号分隔的答案序列
  if (q.questionType === 'BANKED_CLOZE') {
    const parts = answer.split(',')
    const opts = getOptions(q)
    return parts.map(p => {
      const opt = opts.find(o => o.label === p.trim())
      return opt ? `${p.trim()}. ${opt.text}` : p.trim()
    }).join(', ')
  }
  if (q.questionType === 'LONG_MATCH') {
    const parts = answer.split(',')
    const opts = getOptions(q)
    return parts.map((p, i) => {
      const stmt = opts[i]
      return `${p.trim()} — ${stmt?.text || ''}`
    }).join('; ')
  }
  // 单选/判断/仔细阅读
  const opts = getOptions(q)
  const match = opts.find(o => o.label === answer)
  return match ? `${answer}. ${match.text}` : answer
}

async function viewDetail(errorId) {
  detailLoading.value = true
  try {
    const res = await getErrorDetail(errorId)
    detailQuestion.value = res.data.question
    detailPassage.value = res.data.passage || null
    detailRelated.value = res.data.relatedQuestions || []
    dialogVisible.value = true
  } catch (e) { /* ignore */ }
  finally { detailLoading.value = false }
}

async function markMastered(errorId) {
  try {
    await ElMessageBox.confirm('确定标记为已掌握吗？', '确认')
    await markErrorMastered(errorId)
    ElMessage.success('已标记为掌握')
    fetchErrors()
  } catch (e) { /* ignore */ }
}
</script>

<style scoped>
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.wrong-answer { color: #f56c6c; }
.test-progress { margin-bottom: 20px; }
.progress-info { margin-bottom: 8px; color: #666; }
.question-content { font-size: 16px; line-height: 1.8; margin-bottom: 20px; padding: 16px; background: #f8f9ff; border-radius: 8px; }
.options-group { display: flex; flex-direction: column; gap: 12px; }
.option-item { padding: 12px 16px; border: 1px solid #e8e8e8; border-radius: 8px; }
.question-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }
.check-result { margin-top: 16px; padding: 16px; border-radius: 8px; }
.check-result p { margin: 4px 0; }
.correct-bg { background: #f0f9eb; border: 1px solid #e1f3d8; }
.wrong-bg { background: #fef0f0; border: 1px solid #fde2e2; }
</style>
