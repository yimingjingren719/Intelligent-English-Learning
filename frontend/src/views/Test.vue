<template>
  <div class="test-page">
    <NavBar />
    <div class="page-container">
      <!-- 模式选择 -->
      <div v-if="!testStarted && !testSubmitted" class="card-wrapper fade-in">
        <div class="page-header"><h2>模拟测试</h2><p>选择练习模式或模拟考试</p></div>

        <el-tabs v-model="mode">
          <el-tab-pane label="专项练习" name="practice">
            <el-form label-width="100px" style="margin-top:16px">
              <el-form-item label="题型">
                <el-select v-model="practiceType">
                  <el-option label="选词填空 (3分/题)" value="BANKED_CLOZE" />
                  <el-option label="长篇匹配 (7分/题)" value="LONG_MATCH" />
                  <el-option label="仔细阅读 (14分/题)" value="CAREFUL_READING" />
                </el-select>
              </el-form-item>
              <el-form-item label="数量(篇)">
                <el-input-number v-model="practiceCount" :min="1" :max="10" />
                <span style="margin-left:8px;color:#999">
                  ≈ {{ practiceCount * (practiceType === 'BANKED_CLOZE' ? 10 : practiceType === 'LONG_MATCH' ? 10 : 5) }} 小题
                </span>
              </el-form-item>
              <el-button type="primary" size="large" @click="startPractice" :loading="loading">开始练习</el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="模拟试卷" name="exam">
            <div style="margin-top:16px;padding:20px;background:#f8f9ff;border-radius:8px">
              <p>📝 <b>试卷结构</b>：选词填空 ×1 + 长篇匹配 ×1 + 仔细阅读 ×2</p>
              <p>📊 <b>总分</b>：3×10 + 7×10 + 14×5×2 = <b>240分</b></p>
              <el-button type="primary" size="large" @click="startExam" :loading="loading" style="margin-top:12px">开始考试</el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 答题界面 -->
      <div v-if="testStarted && !testSubmitted" class="test-layout fade-in">
        <div class="question-sidebar">
          <div class="sidebar-header">
            <h4>{{ mode === 'exam' ? '试卷' : '答题卡' }}</h4>
            <span class="answered-count">{{ answeredCount }} / {{ realTotal }} 已答</span>
          </div>
          <div class="sidebar-list">
            <div v-for="(g, gIdx) in groups" :key="'g' + gIdx"
              class="sidebar-item"
              :class="{ active: currentGroupIdx === gIdx, answered: isGroupDone(gIdx) }"
              @click="currentGroupIdx = gIdx">
              <span class="sidebar-num">{{ gIdx + 1 }}</span>
              <span class="sidebar-type">{{ typeShort(g) }}</span>
            </div>
          </div>
          <div class="sidebar-footer">
            <el-tag size="small" type="warning">⏱ {{ formatTime(elapsedTime) }}</el-tag>
            <el-button type="danger" size="small" @click="confirmSubmit" style="margin-top:8px;width:100%">📝 交卷</el-button>
          </div>
        </div>

        <div class="question-main">
          <!-- 选词填空 -->
          <template v-if="currentGroup?.type === 'BANKED_CLOZE'">
            <div class="card-wrapper">
              <h3 style="margin-bottom:16px">📝 选词填空 (15选10，每题3分)</h3>
              <div class="word-bank">
                <span v-for="(w, i) in getOptions(currentGroup.question)" :key="i"
                  class="word-chip" :class="{ used: isWordUsed(i) }">
                  {{ ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O'][i] }}. {{ w.text }}
                </span>
              </div>
              <div class="passage-content" style="margin-top:16px;line-height:2.2">
                <template v-for="(part, pi) in clozeParts" :key="pi">
                  <span v-if="part.text" v-html="part.text"></span>
                  <el-select v-if="part.isBlank" v-model="clozeAnswers[currentGroupIdx][part.blankIdx]"
                    size="small" style="width:120px" placeholder="选词">
                    <el-option v-for="(w, wi) in getOptions(currentGroup.question)" :key="wi"
                      :label="['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O'][wi]" :value="['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O'][wi]" />
                  </el-select>
                </template>
              </div>
            </div>
          </template>

          <!-- 长篇匹配 -->
          <template v-if="currentGroup?.type === 'LONG_MATCH'">
            <div class="card-wrapper" style="margin-bottom:16px">
              <h3 style="margin-bottom:12px">🔗 长篇匹配 (10题，每题7分)</h3>
              <p style="color:#999;margin-bottom:16px">阅读文章后将右侧10个陈述匹配到对应段落</p>
              <div class="passage-content" v-html="currentGroup.question.content" style="line-height:2"></div>
            </div>
            <div class="card-wrapper">
              <h4 style="margin-bottom:12px">匹配选项</h4>
              <div v-for="(stmt, si) in getOptions(currentGroup.question)" :key="si" class="match-row">
                <span class="match-num">{{ stmt.label }}</span>
                <span class="match-text">{{ stmt.text }}</span>
                <el-select v-model="matchAnswers[currentGroupIdx][si]" size="small" style="width:100px" placeholder="段落">
                  <el-option v-for="ch in 'ABCDEFGHIJ'.split('')" :key="ch" :label="ch" :value="ch" />
                </el-select>
              </div>
            </div>
          </template>

          <!-- 仔细阅读 -->
          <template v-if="currentGroup?.type === 'CAREFUL_READING'">
            <div class="card-wrapper reading-passage" style="margin-bottom:16px">
              <el-alert title="📖 仔细阅读文章，然后回答以下5个问题 (每题14分)" type="info" :closable="false" style="margin-bottom:12px" />
              <div class="passage-content" v-html="currentGroup.passage.content"></div>
            </div>
            <div v-for="(q, qIdx) in currentGroup.questions" :key="q.id" class="card-wrapper question-card" style="margin-bottom:12px">
              <div class="question-header"><el-tag>第 {{ qIdx + 1 }} 题 (14分)</el-tag></div>
              <div class="question-content" v-html="q.content"></div>
              <el-radio-group v-model="readingAnswers[currentGroupIdx][qIdx]" class="options-group">
                <div v-for="opt in getOptions(q)" :key="opt.label" class="option-item" :class="{ selected: readingAnswers[currentGroupIdx][qIdx] === opt.label }">
                  <el-radio :value="opt.label">{{ opt.label }}. {{ opt.text }}</el-radio>
                </div>
              </el-radio-group>
            </div>
          </template>

          <div class="question-actions">
            <el-button v-if="currentGroupIdx > 0" @click="currentGroupIdx--">⬅ 上一大题</el-button>
            <span class="question-pos">{{ currentGroupIdx + 1 }} / {{ groups.length }}</span>
            <el-button v-if="currentGroupIdx < groups.length - 1" type="primary" @click="currentGroupIdx++">下一大题 ➡</el-button>
            <el-button v-if="currentGroupIdx === groups.length - 1" type="success" @click="confirmSubmit">📝 交卷</el-button>
          </div>
        </div>
      </div>

      <!-- 测试结果 -->
      <div v-if="testSubmitted && testResult" class="card-wrapper fade-in">
        <el-result :icon="testResult.score >= 100 ? 'success' : 'error'" :title="`总得分: ${testResult.score} 分`" :sub-title="`正确 ${testResult.correctCount} / ${testResult.totalQuestions} 题`">
          <template #extra>
            <el-button type="primary" @click="resetTest">再来一次</el-button>
            <el-button @click="router.push('/dashboard')">返回首页</el-button>
          </template>
        </el-result>
        <h3 style="margin:20px 0">详细结果</h3>
        <div v-for="(detail, idx) in testResult.details" :key="idx" class="answer-detail">
          <div class="detail-header">
            <el-tag :type="detail.isCorrect ? 'success' : 'danger'" size="small">{{ detail.isCorrect ? '✓' : '✗' }}</el-tag>
            <span>第 {{ idx + 1 }} 题 ({{ detail.score }}分)</span>
          </div>
          <div class="detail-content" v-html="detail.content"></div>
          <div class="detail-answers">
            <p>你的答案：<span :class="detail.isCorrect ? 'correct' : 'wrong'">{{ detail.userAnswer || '(未作答)' }}</span></p>
            <p>正确答案：<span class="correct">{{ detail.correctAnswer }}</span></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { generateTestAPI, submitTestAPI } from '@/api/test'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false); const testStarted = ref(false); const testSubmitted = ref(false)
const mode = ref('practice')
const practiceType = ref('BANKED_CLOZE'); const practiceCount = ref(1)
const elapsedTime = ref(0); let testStartTime = 0; let timerInterval = null; let autoSubmitTimer = null
const testResult = ref(null)

const groups = ref([]); const currentGroupIdx = ref(0)
const clozeAnswers = ref({}); const matchAnswers = ref({}); const readingAnswers = ref({})

// ======== 测试状态持久化（切换页面不丢失）========
const STORAGE_KEY = 'ongoing_test'

function saveTestState() {
  if (!testStarted.value || testSubmitted.value) return
  const state = {
    mode: mode.value, groups: JSON.parse(JSON.stringify(groups.value)),
    currentGroupIdx: currentGroupIdx.value,
    clozeAnswers: JSON.parse(JSON.stringify(clozeAnswers.value)),
    matchAnswers: JSON.parse(JSON.stringify(matchAnswers.value)),
    readingAnswers: JSON.parse(JSON.stringify(readingAnswers.value)),
    startTime: testStartTime || Date.now()
  }
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

function restoreTestState() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return
    const data = JSON.parse(raw)
    groups.value = data.groups || []
    currentGroupIdx.value = data.currentGroupIdx || 0
    clozeAnswers.value = data.clozeAnswers || {}
    matchAnswers.value = data.matchAnswers || {}
    readingAnswers.value = data.readingAnswers || {}
    mode.value = data.mode || 'practice'
    const savedStart = data.startTime || 0
    testStartTime = savedStart
    const now = Date.now()
    elapsedTime.value = savedStart ? Math.floor((now - savedStart) / 1000) : 0
    testStarted.value = true
    startTimer()
    startAutoSubmit()
  } catch (e) { sessionStorage.removeItem(STORAGE_KEY) }
}

function clearTestState() {
  sessionStorage.removeItem(STORAGE_KEY)
}

function startAutoSubmit() {
  clearTimeout(autoSubmitTimer)
  autoSubmitTimer = setTimeout(() => {
    if (testStarted.value && !testSubmitted.value) {
      ElMessage.warning('测试已超过1小时，自动提交')
      doSubmit()
    }
  }, 3600000) // 1小时
}

// 保存状态到 sessionStorage（每次切换题目时调用）
watch(currentGroupIdx, () => saveTestState())
watch(clozeAnswers, () => saveTestState(), { deep: true })
watch(matchAnswers, () => saveTestState(), { deep: true })
watch(readingAnswers, () => saveTestState(), { deep: true })

onMounted(() => {
  restoreTestState()
})

const currentGroup = computed(() => groups.value[currentGroupIdx.value] || null)

// 选词填空文章分段
const clozeParts = computed(() => {
  const g = currentGroup.value; if (g?.type !== 'BANKED_CLOZE') return []
  const text = g.question.content || ''; const parts = []
  const regex = /<b>(\d+)\._____<\/b>/g; let lastIdx = 0; let match; let blankIdx = 0
  while ((match = regex.exec(text)) !== null) {
    if (match.index > lastIdx) parts.push({ text: text.slice(lastIdx, match.index) })
    parts.push({ isBlank: true, blankIdx: blankIdx++ })
    lastIdx = match.index + match[0].length
  }
  if (lastIdx < text.length) parts.push({ text: text.slice(lastIdx) })
  return parts
})

function isWordUsed(idx) {
  const answers = clozeAnswers.value[currentGroupIdx.value] || []
  const letter = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O'][idx]
  return answers.includes(letter)
}

const realTotal = computed(() => {
  let n = 0; groups.value.forEach((g, i) => {
    if (g.type === 'BANKED_CLOZE') n += 10
    else if (g.type === 'LONG_MATCH') n += 10
    else if (g.type === 'CAREFUL_READING') n += g.questions.length
  }); return n
})

const answeredCount = computed(() => {
  let n = 0; groups.value.forEach((g, i) => {
    if (g.type === 'BANKED_CLOZE') n += (clozeAnswers.value[i] || []).filter(Boolean).length
    else if (g.type === 'LONG_MATCH') n += (matchAnswers.value[i] || []).filter(Boolean).length
    else if (g.type === 'CAREFUL_READING') n += (readingAnswers.value[i] || []).filter(Boolean).length
  }); return n
})

function isGroupDone(gIdx) {
  const g = groups.value[gIdx]
  if (g.type === 'BANKED_CLOZE') return (clozeAnswers.value[gIdx] || []).filter(Boolean).length === 10
  if (g.type === 'LONG_MATCH') return (matchAnswers.value[gIdx] || []).filter(Boolean).length === 10
  if (g.type === 'CAREFUL_READING') return (readingAnswers.value[gIdx] || []).every(Boolean)
  return false
}

function typeShort(g) {
  const m = { BANKED_CLOZE: '选词', LONG_MATCH: '匹配', CAREFUL_READING: '阅读' }; return m[g.type] || '?'
}

function getOptions(q) {
  if (!q?.options) return []
  try { return typeof q.options === 'string' ? JSON.parse(q.options) : q.options } catch { return [] }
}

function formatTime(s) { const m = Math.floor(s / 60), sec = s % 60; return `${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}` }

function groupQuestions(questions, qType) {
  const result = []
  if (qType === 'BANKED_CLOZE' || qType === 'LONG_MATCH') {
    questions.forEach(q => result.push({ type: qType, question: q }))
  } else if (qType === 'CAREFUL_READING') {
    let i = 0
    while (i < questions.length) {
      const q = questions[i]
      if (q.questionType === 'CAREFUL_READING' && q.correctAnswer === 'PASSAGE') {
        const passage = q; const qs = []; i++
        while (i < questions.length && questions[i].questionType === 'CAREFUL_READING' && questions[i].correctAnswer !== 'PASSAGE') { qs.push(questions[i]); i++ }
        if (qs.length > 0) result.push({ type: 'CAREFUL_READING', passage, questions: qs })
      } else { i++ }
    }
  }
  return result
}

async function startPractice() { await loadTest(practiceType.value, practiceCount.value) }
async function startExam() { mode.value = 'exam'; await loadTest('EXAM', 1) }

async function loadTest(qType, count) {
  loading.value = true
  try {
    if (qType === 'EXAM') {
      // 试卷模式：各取1篇
      const [bc, lm, cr] = await Promise.all([
        generateTestAPI({ questionType: 'BANKED_CLOZE', count: 1 }),
        generateTestAPI({ questionType: 'LONG_MATCH', count: 1 }),
        generateTestAPI({ questionType: 'CAREFUL_READING', count: 12 })
      ])
      const all = [...bc.data.questions, ...lm.data.questions, ...cr.data.questions]
      groups.value = [
        ...groupQuestions(bc.data.questions, 'BANKED_CLOZE'),
        ...groupQuestions(lm.data.questions, 'LONG_MATCH'),
        ...groupQuestions(cr.data.questions, 'CAREFUL_READING')
      ]
    } else {
      const res = await generateTestAPI({ questionType: qType, count: qType === 'CAREFUL_READING' ? count * 6 : count })
      groups.value = groupQuestions(res.data.questions, qType)
    }
    // 初始化答案
    const ca = {}; const ma = {}; const ra = {}
    groups.value.forEach((g, i) => {
      if (g.type === 'BANKED_CLOZE') ca[i] = new Array(10).fill('')
      else if (g.type === 'LONG_MATCH') ma[i] = new Array(10).fill('')
      else if (g.type === 'CAREFUL_READING') ra[i] = new Array(g.questions.length).fill('')
    })
    clozeAnswers.value = ca; matchAnswers.value = ma; readingAnswers.value = ra
    currentGroupIdx.value = 0; testStarted.value = true
    testStartTime = Date.now()
    startTimer(); startAutoSubmit(); saveTestState()
  } catch (e) { ElMessage.error('加载题目失败') }
  finally { loading.value = false }
}

function startTimer() { timerInterval = setInterval(() => { elapsedTime.value = Math.floor((Date.now() - testStartTime) / 1000) }, 1000) }

function confirmSubmit() {
  const un = realTotal.value - answeredCount.value
  ElMessageBox.confirm(un > 0 ? `还有 ${un} 题未作答，确定提交？` : '确定交卷？', '确认', { confirmButtonText: '提交', cancelButtonText: '继续检查', type: 'warning' })
    .then(() => submitTest()).catch(() => {})
}

async function submitTest() {
  loading.value = true
  try {
    const flat = []
    groups.value.forEach((g, i) => {
      if (g.type === 'BANKED_CLOZE') {
        (clozeAnswers.value[i] || []).forEach((ans, idx) => {
          flat.push({ questionId: g.question.id * 100 + idx, userAnswer: ans || '', timeTaken: 0, score: 3 })
        })
      } else if (g.type === 'LONG_MATCH') {
        (matchAnswers.value[i] || []).forEach((ans, idx) => {
          flat.push({ questionId: g.question.id * 100 + idx, userAnswer: ans || '', timeTaken: 0, score: 7 })
        })
      } else if (g.type === 'CAREFUL_READING') {
        g.questions.forEach((q, qIdx) => {
          flat.push({ questionId: q.id, userAnswer: readingAnswers.value[i][qIdx] || '', timeTaken: 0, score: 14 })
        })
      }
    })
    const res = await submitTestAPI({ answers: flat })
    testResult.value = res.data; testSubmitted.value = true; testStarted.value = false
    clearInterval(timerInterval); clearTimeout(autoSubmitTimer); clearTestState()
    ElMessage.success('测试完成！')
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

function resetTest() {
  testStarted.value = false; testSubmitted.value = false; testResult.value = null
  groups.value = []; currentGroupIdx.value = 0; elapsedTime.value = 0
  testStartTime = 0; clearTestState(); clearTimeout(autoSubmitTimer)
}

onUnmounted(() => clearInterval(timerInterval))
</script>

<style scoped>
.test-layout { display: flex; gap: 16px; align-items: flex-start; }

.question-sidebar { width: 160px; min-width: 160px; background: #fff; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); position: sticky; top: 80px; max-height: calc(100vh - 100px); display: flex; flex-direction: column; }
.sidebar-header { padding: 12px 14px; border-bottom: 1px solid #f0f0f0; display: flex; justify-content: space-between; align-items: center; }
.sidebar-header h4 { font-size: 14px; }
.answered-count { font-size: 11px; color: #667eea; }
.sidebar-list { flex: 1; overflow-y: auto; padding: 8px; display: flex; flex-wrap: wrap; gap: 6px; align-content: flex-start; }
.sidebar-item { width: 44px; height: 44px; border-radius: 8px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; background: #f5f5f5; border: 2px solid transparent; font-size: 11px; }
.sidebar-item .sidebar-num { font-size: 13px; font-weight: 600; color: #999; }
.sidebar-item .sidebar-type { font-size: 7px; color: #bbb; }
.sidebar-item:hover { border-color: #667eea; background: #eef0ff; }
.sidebar-item.active { border-color: #667eea; background: #eef0ff; }
.sidebar-item.active .sidebar-num { color: #667eea; }
.sidebar-item.answered { background: #f0f9eb; border-color: #e1f3d8; }
.sidebar-footer { padding: 8px 12px; border-top: 1px solid #f0f0f0; text-align: center; }
.question-main { flex: 1; min-width: 0; }

.word-bank { display: flex; flex-wrap: wrap; gap: 6px; padding: 12px; background: #f8f9ff; border-radius: 8px; margin-bottom: 16px; }
.word-chip { padding: 4px 10px; background: #eef0ff; border-radius: 6px; font-size: 13px; font-weight: 500; transition: all 0.2s; }
.word-chip.used { opacity: 0.3; text-decoration: line-through; background: #f5f5f5; }

.match-row { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.match-num { font-weight: 700; color: #667eea; min-width: 24px; }
.match-text { flex: 1; font-size: 14px; }

.question-header { display: flex; gap: 8px; margin-bottom: 12px; }
.question-content { font-size: 15px; line-height: 1.8; margin-bottom: 16px; }
.passage-content { font-size: 15px; }
.passage-content :deep(p) { margin-bottom: 8px; line-height: 1.8; }
.passage-content :deep(b) { color: #e6a23c; }

.options-group { display: flex; flex-direction: column; gap: 8px; }
.option-item { padding: 12px 16px; border: 2px solid #e8e8e8; border-radius: 8px; transition: all 0.2s; }
.option-item:hover { border-color: #667eea; }
.option-item.selected { border-color: #667eea; background: #eef0ff; }

.question-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #f0f0f0; }
.question-pos { color: #999; }

.answer-detail { padding: 14px; margin-bottom: 10px; border: 1px solid #e8e8e8; border-radius: 8px; }
.detail-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.correct { color: #67c23a; font-weight: 600; }
.wrong { color: #f56c6c; font-weight: 600; }
</style>
