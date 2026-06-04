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
            <span>第 {{ practiceIndex + 1 }} / {{ practiceQuestions.length }} 大题</span>
            <el-tag size="small" style="margin-left:8px">{{ typeLabel(currentItem?.type) }}</el-tag>
          </div>
          <el-progress :percentage="practicePercent" :stroke-width="8" color="#f5576c" />
        </div>

        <!-- 选词填空 -->
        <template v-if="currentItem?.type === 'BANKED_CLOZE'">
          <div class="card-wrapper">
            <h3 style="margin-bottom:16px">📝 选词填空 (15选10)</h3>
            <div class="word-bank">
              <span v-for="(w, i) in getOptions(currentItem.question)" :key="i"
                class="word-chip" :class="{ used: isClozeWordUsed(i) }">
                {{ ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O'][i] }}. {{ w.text }}
              </span>
            </div>
            <div class="passage-content" style="line-height:2.2">
              <template v-for="(part, pi) in clozeParts" :key="pi">
                <span v-if="part.text" v-html="part.text"></span>
                <el-select v-if="part.isBlank" v-model="clozeAnswers[part.blankIdx]"
                  size="small" style="width:120px" placeholder="选词">
                  <el-option v-for="(w, wi) in getOptions(currentItem.question)" :key="wi"
                    :label="['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O'][wi]"
                    :value="['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O'][wi]" />
                </el-select>
              </template>
            </div>
          </div>
        </template>

        <!-- 长篇匹配 -->
        <template v-if="currentItem?.type === 'LONG_MATCH'">
          <div class="card-wrapper" style="margin-bottom:16px">
            <h3 style="margin-bottom:12px">🔗 长篇匹配 (10题)</h3>
            <p style="color:#999;margin-bottom:16px">阅读文章后将右侧10个陈述匹配到对应段落</p>
            <div class="passage-content" v-html="currentItem.question.content" style="line-height:2"></div>
          </div>
          <div class="card-wrapper">
            <h4 style="margin-bottom:12px">匹配选项</h4>
            <div v-for="(stmt, si) in getOptions(currentItem.question)" :key="si" class="match-row">
              <span class="match-num">{{ stmt.label }}</span>
              <span class="match-text">{{ stmt.text }}</span>
              <el-select v-model="matchAnswers[si]" size="small" style="width:100px" placeholder="段落">
                <el-option v-for="ch in 'ABCDEFGHIJ'.split('')" :key="ch" :label="ch" :value="ch" />
              </el-select>
            </div>
          </div>
        </template>

        <!-- 仔细阅读 -->
        <template v-if="currentItem?.type === 'CAREFUL_READING'">
          <div class="card-wrapper reading-passage" style="margin-bottom:16px">
            <el-alert title="📖 仔细阅读文章，然后回答问题 (每题14分)" type="info" :closable="false" style="margin-bottom:12px" />
            <div class="passage-content" v-html="currentItem.passage?.content"></div>
          </div>
          <div v-for="(q, qIdx) in currentItem.questions" :key="q.id" class="card-wrapper question-card" style="margin-bottom:12px">
            <div class="question-header"><el-tag>第 {{ qIdx + 1 }} 题</el-tag></div>
            <div class="question-content" v-html="q.content"></div>
            <el-radio-group v-model="readingAnswers[qIdx]" class="options-group">
              <div v-for="opt in getOptions(q)" :key="opt.label" class="option-item"
                :class="{ selected: readingAnswers[qIdx] === opt.label }">
                <el-radio :value="opt.label">{{ opt.label }}. {{ opt.text }}</el-radio>
              </div>
            </el-radio-group>
          </div>
        </template>

        <!-- 操作按钮 -->
        <div class="question-actions">
          <el-button @click="checkPracticeAnswer" :disabled="!canCheckAnswer">检查答案</el-button>
          <el-button type="primary" @click="nextPractice" :disabled="!checked">
            {{ practiceIndex < practiceQuestions.length - 1 ? '下一大题' : '完成练习' }}
          </el-button>
        </div>

        <!-- 检查结果 -->
        <div v-if="checked" class="card-wrapper" style="margin-top:16px">
          <div class="check-result" :class="isCorrect ? 'correct-bg' : 'wrong-bg'">
            <p><strong>{{ isCorrect ? '✅ 全部正确！' : '❌ 存在错误' }}</strong></p>
            <div v-if="currentItem?.type === 'BANKED_CLOZE'">
              <p>正确答案：<strong>{{ currentItem.question.correctAnswer }}</strong></p>
              <div v-for="(ans, ai) in correctAnswerParts" :key="ai" style="margin:2px 0">
                第{{ ai + 1 }}题：
                <span :style="{ color: clozeAnswers[ai] === ans ? '#67c23a' : '#f56c6c' }">
                  {{ clozeAnswers[ai] || '(未作答)' }} → {{ ans }}
                </span>
              </div>
            </div>
            <div v-if="currentItem?.type === 'LONG_MATCH'">
              <p>正确答案：<strong>{{ currentItem.question.correctAnswer }}</strong></p>
              <div v-for="(ans, ai) in correctAnswerParts" :key="ai" style="margin:2px 0">
                陈述{{ ai + 1 }}：
                <span :style="{ color: matchAnswers[ai] === ans ? '#67c23a' : '#f56c6c' }">
                  {{ matchAnswers[ai] || '(未作答)' }} → {{ ans }}
                </span>
              </div>
            </div>
            <div v-if="currentItem?.type === 'CAREFUL_READING'">
              <div v-for="(q, qi) in currentItem.questions" :key="qi" style="margin:2px 0">
                第{{ qi + 1 }}题：
                <span :style="{ color: readingAnswers[qi] === q.correctAnswer ? '#67c23a' : '#f56c6c' }">
                  {{ readingAnswers[qi] || '(未作答)' }} → {{ q.correctAnswer }}
                </span>
              </div>
            </div>
            <p v-if="currentItem?.question?.analysis" style="margin-top:8px">
              解析：{{ currentItem.question.analysis }}
            </p>
          </div>
        </div>
      </div>

      <!-- 错题详情弹窗 -->
      <el-dialog v-model="dialogVisible" title="错题详情 — 原题回顾" width="750px">
        <div v-if="detailQuestion" v-loading="detailLoading">
          <div v-if="detailPassage" class="card-wrapper reading-passage" style="margin-bottom:12px">
            <el-alert title="📖 阅读文章" type="info" :closable="false" style="margin-bottom:8px" />
            <div class="passage-content" v-html="detailPassage.content"></div>
          </div>
          <div class="question-content" v-html="detailQuestion.content" style="margin-bottom:12px"></div>
          <p><strong>正确答案：</strong>{{ formatAnswer(detailQuestion) }}</p>
          <p v-if="detailQuestion.analysis"><strong>解析：</strong>{{ detailQuestion.analysis }}</p>
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
import { ref, computed, onMounted, watch } from 'vue'
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
const checked = ref(false)
const isCorrect = ref(false)

// 各题型的答案
const clozeAnswers = ref([])
const matchAnswers = ref([])
const readingAnswers = ref([])

// 详情弹窗
const dialogVisible = ref(false)
const detailQuestion = ref(null)
const detailPassage = ref(null)
const detailRelated = ref([])
const detailLoading = ref(false)

const currentItem = computed(() => practiceQuestions.value[practiceIndex.value] || null)

const practicePercent = computed(() =>
  practiceQuestions.value.length > 0
    ? Math.round((practiceIndex.value + 1) / practiceQuestions.value.length * 100)
    : 0
)

// 将正确答案按逗号分割
const correctAnswerParts = computed(() => {
  const item = currentItem.value
  if (!item) return []
  const answer = item.type === 'CAREFUL_READING'
    ? '' : (item.question?.correctAnswer || '')
  return answer ? answer.split(',').map(s => s.trim()) : []
})

// 选词填空文章分段
const clozeParts = computed(() => {
  const item = currentItem.value
  if (item?.type !== 'BANKED_CLOZE') return []
  const text = item.question?.content || ''
  const parts = []
  const regex = /<b>(\d+)\._____<\/b>/g
  let lastIdx = 0; let match; let blankIdx = 0
  while ((match = regex.exec(text)) !== null) {
    if (match.index > lastIdx) parts.push({ text: text.slice(lastIdx, match.index) })
    parts.push({ isBlank: true, blankIdx: blankIdx++ })
    lastIdx = match.index + match[0].length
  }
  if (lastIdx < text.length) parts.push({ text: text.slice(lastIdx) })
  return parts
})

function isClozeWordUsed(idx) {
  const letter = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O'][idx]
  return clozeAnswers.value.includes(letter)
}

// 是否可以检查答案
const canCheckAnswer = computed(() => {
  const item = currentItem.value
  if (!item) return false
  if (item.type === 'BANKED_CLOZE') return clozeAnswers.value.filter(Boolean).length === 10
  if (item.type === 'LONG_MATCH') return matchAnswers.value.filter(Boolean).length === 10
  if (item.type === 'CAREFUL_READING')
    return readingAnswers.value.length > 0 && readingAnswers.value.every(Boolean)
  return false
})

// 监听题型切换，重置答案数组
watch(currentItem, (item) => {
  if (!item) return
  checked.value = false
  if (item.type === 'BANKED_CLOZE') {
    clozeAnswers.value = new Array(10).fill('')
  } else if (item.type === 'LONG_MATCH') {
    matchAnswers.value = new Array(10).fill('')
  } else if (item.type === 'CAREFUL_READING') {
    readingAnswers.value = new Array(item.questions?.length || 0).fill('')
  }
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
    // Backend now returns structured items: [{type, question, passage?, questions?}, ...]
    practiceQuestions.value = res.data.questions || []
    practicing.value = true
    practiceIndex.value = 0
    checked.value = false
    // Initialize answers for first item
    initAnswersForCurrent()
  } catch (e) {
    ElMessage.error('加载练习题目失败')
  }
}

function initAnswersForCurrent() {
  const item = currentItem.value
  if (!item) return
  if (item.type === 'BANKED_CLOZE') {
    clozeAnswers.value = new Array(10).fill('')
  } else if (item.type === 'LONG_MATCH') {
    matchAnswers.value = new Array(10).fill('')
  } else if (item.type === 'CAREFUL_READING') {
    readingAnswers.value = new Array(item.questions?.length || 0).fill('')
  }
}

function checkPracticeAnswer() {
  const item = currentItem.value
  if (!item) return
  checked.value = true

  if (item.type === 'BANKED_CLOZE') {
    const correct = correctAnswerParts.value
    isCorrect.value = correct.length > 0 && correct.every((ans, i) =>
      ans.toUpperCase() === (clozeAnswers.value[i] || '').toUpperCase()
    )
  } else if (item.type === 'LONG_MATCH') {
    const correct = correctAnswerParts.value
    isCorrect.value = correct.length > 0 && correct.every((ans, i) =>
      ans.toUpperCase() === (matchAnswers.value[i] || '').toUpperCase()
    )
  } else if (item.type === 'CAREFUL_READING') {
    const questions = item.questions || []
    isCorrect.value = questions.length > 0 && questions.every((q, i) =>
      (q.correctAnswer || '').toUpperCase() === (readingAnswers.value[i] || '').toUpperCase()
    )
  } else {
    isCorrect.value = false
  }
}

function nextPractice() {
  if (practiceIndex.value < practiceQuestions.value.length - 1) {
    practiceIndex.value++
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

function typeLabel(type) {
  const map = { BANKED_CLOZE: '选词填空', LONG_MATCH: '长篇匹配', CAREFUL_READING: '仔细阅读' }
  return map[type] || type || ''
}

function formatAnswer(q) {
  if (!q) return ''
  const answer = q.correctAnswer || ''
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
.progress-info { margin-bottom: 8px; color: #666; display: flex; align-items: center; }

/* 选词填空 */
.word-bank { display: flex; flex-wrap: wrap; gap: 6px; padding: 12px; background: #f8f9ff; border-radius: 8px; margin-bottom: 16px; }
.word-chip { padding: 4px 10px; background: #eef0ff; border-radius: 6px; font-size: 13px; font-weight: 500; transition: all 0.2s; }
.word-chip.used { opacity: 0.3; text-decoration: line-through; background: #f5f5f5; }

/* 长篇匹配 */
.match-row { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.match-num { font-weight: 700; color: #667eea; min-width: 24px; }
.match-text { flex: 1; font-size: 14px; }

/* 仔细阅读 */
.question-header { display: flex; gap: 8px; margin-bottom: 12px; }
.question-content { font-size: 16px; line-height: 1.8; margin-bottom: 20px; padding: 16px; background: #f8f9ff; border-radius: 8px; }
.passage-content { font-size: 15px; }
.passage-content :deep(p) { margin-bottom: 8px; line-height: 1.8; }
.passage-content :deep(b) { color: #e6a23c; }

.options-group { display: flex; flex-direction: column; gap: 8px; }
.option-item { padding: 12px 16px; border: 2px solid #e8e8e8; border-radius: 8px; transition: all 0.2s; }
.option-item:hover { border-color: #667eea; }
.option-item.selected { border-color: #667eea; background: #eef0ff; }

.question-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }

.check-result { margin-top: 16px; padding: 16px; border-radius: 8px; }
.check-result p { margin: 4px 0; }
.correct-bg { background: #f0f9eb; border: 1px solid #e1f3d8; }
.wrong-bg { background: #fef0f0; border: 1px solid #fde2e2; }
</style>
