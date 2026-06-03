<template>
  <div class="vocabulary-page">
    <NavBar />
    <div class="page-container">
      <div class="page-header fade-in">
        <h2>单词学习</h2>
        <p>词库 + 生词本 + 测验模式</p>
      </div>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- ========== Tab 1: 我的生词本 ========== -->
        <el-tab-pane label="我的生词本" name="mine">
          <div class="card-wrapper">
            <div class="section-header">
              <span>共 {{ totalWords }} 个单词 | 已掌握 {{ masteredCount }}</span>
              <el-button type="primary" @click="dialogVisible = true">+ 手动添加</el-button>
            </div>
            <div class="word-grid">
              <div v-for="word in words" :key="word.id" class="word-card" :class="{ mastered: word.mastered === 1 }">
                <div class="word-text">{{ word.word }}</div>
                <div class="word-trans">{{ word.translation }}</div>
                <div class="word-meta">
                  <el-tag size="small" :type="word.mastered ? 'success' : 'info'">
                    {{ word.mastered ? '已掌握' : '复习' + word.reviewCount + '次' }}
                  </el-tag>
                </div>
                <div class="word-actions">
                  <el-button v-if="!word.mastered" size="small" type="success" @click="handleMaster(word.id)">✓掌握</el-button>
                  <el-button size="small" type="danger" @click="handleDelete(word.id)">删除</el-button>
                </div>
              </div>
            </div>
            <el-empty v-if="words.length === 0" description="生词本为空，去词库添加单词吧" />
            <el-pagination v-if="total > 0" v-model:current-page="currentPage" :page-size="pageSize" :total="total"
              layout="prev, pager, next" @current-change="fetchMyWords" style="margin-top:16px;justify-content:center" />
          </div>
        </el-tab-pane>

        <!-- ========== Tab 2: 词库 ========== -->
        <el-tab-pane label="词库" name="system">
          <div class="card-wrapper">
            <div class="section-header">
              <span>共 {{ sysTotal }} 个六级核心词汇</span>
              <el-input v-model="searchKeyword" placeholder="搜索单词..." clearable @clear="fetchSystemWords" @keyup.enter="fetchSystemWords" style="width:200px">
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
            </div>
            <div class="word-grid">
              <div v-for="word in sysWords" :key="word.id" class="word-card system-word">
                <div class="word-text">{{ word.word }}</div>
                <div class="word-trans">{{ word.translation }}</div>
                <el-button size="small" type="primary" @click="learnWord(word.id)">+ 加入学习</el-button>
              </div>
            </div>
            <el-pagination v-if="sysTotal > 0" v-model:current-page="sysPage" :page-size="sysSize" :total="sysTotal"
              layout="prev, pager, next" @current-change="fetchSystemWords" style="margin-top:16px;justify-content:center" />
          </div>
        </el-tab-pane>

        <!-- ========== Tab 3: 单词测验 ========== -->
        <el-tab-pane label="单词测验" name="quiz">
          <div class="card-wrapper" v-if="!quizFinished">
            <div class="quiz-progress">
              <span>第 {{ quizIndex + 1 }} / {{ quizTotal }} 题</span>
              <span>✅ {{ quizCorrect }} 正确</span>
            </div>
            <div class="quiz-card" v-if="quizCurrent">
              <h2 class="quiz-word">{{ quizCurrent.word }}</h2>
              <p class="quiz-hint">请选择正确的中文释义</p>
              <div class="quiz-options">
                <div v-for="(opt, idx) in quizOptions" :key="idx" class="quiz-option"
                  :class="{ correct: quizAnswered && idx === quizCorrectIdx, wrong: quizAnswered && idx === quizSelectedIdx && idx !== quizCorrectIdx }"
                  @click="answerQuiz(idx)">
                  <span class="quiz-opt-label">{{ ['A','B','C','D'][idx] }}</span>
                  {{ opt }}
                </div>
              </div>
              <el-button v-if="quizAnswered" type="primary" size="large" @click="nextQuiz" style="margin-top:16px">
                {{ quizIndex < quizTotal - 1 ? '下一题' : '查看结果' }}
              </el-button>
            </div>
          </div>
          <div class="card-wrapper" v-else>
            <el-result icon="success" :title="`测验完成！`" :sub-title="`正确 ${quizCorrect} / ${quizTotal}`">
              <template #extra>
                <el-button type="primary" @click="startQuiz">再来一轮</el-button>
                <el-button @click="activeTab = 'system'">去词库背单词</el-button>
              </template>
            </el-result>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 添加生词弹窗 -->
    <el-dialog v-model="dialogVisible" title="添加生词" width="400px">
      <el-form :model="wordForm" @keyup.enter="saveWord">
        <el-form-item label="单词"><el-input v-model="wordForm.word" placeholder="输入英文单词" /></el-form-item>
        <el-form-item label="释义"><el-input v-model="wordForm.translation" placeholder="输入中文释义" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveWord">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import { getVocabularyPage, addVocabularyAPI, deleteVocabularyAPI, markMasteredAPI } from '@/api/vocabulary'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const activeTab = ref('mine')

// --- 我的生词本 ---
const words = ref([])
const currentPage = ref(1); const pageSize = ref(24)
const total = ref(0); const totalWords = ref(0); const masteredCount = ref(0)
const dialogVisible = ref(false)
const wordForm = reactive({ word: '', translation: '' })

async function fetchMyWords() {
  try {
    const res = await getVocabularyPage({ page: currentPage.value, size: pageSize.value })
    words.value = res.data.records; total.value = res.data.total; totalWords.value = res.data.total
  } catch (e) { /* ignore */ }
}

async function saveWord() {
  if (!wordForm.word) return
  try { await addVocabularyAPI(wordForm); ElMessage.success('已添加'); dialogVisible.value = false; wordForm.word = ''; wordForm.translation = ''; fetchMyWords() }
  catch (e) { ElMessage.error(e.message || '添加失败') }
}

async function handleDelete(id) {
  try { await ElMessageBox.confirm('确定删除？', '确认'); await deleteVocabularyAPI(id); ElMessage.success('已删除'); fetchMyWords() }
  catch (e) { /* cancel */ }
}

async function handleMaster(id) {
  try { await markMasteredAPI(id); masteredCount.value++; ElMessage.success('已标记掌握'); fetchMyWords() }
  catch (e) { /* ignore */ }
}

// --- 词库 ---
const sysWords = ref([]); const sysPage = ref(1); const sysSize = ref(50)
const sysTotal = ref(0); const searchKeyword = ref('')

async function fetchSystemWords() {
  try {
    const params = { page: sysPage.value, size: sysSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await request.get('/vocabulary/system', { params })
    sysWords.value = res.data.records; sysTotal.value = res.data.total
  } catch (e) { /* ignore */ }
}

async function learnWord(id) {
  try { await request.post(`/vocabulary/learn/${id}`); ElMessage.success('已加入学习列表'); fetchMyWords() }
  catch (e) { ElMessage.warning(e.response?.data?.message || '已在生词本中') }
}

// --- 单词测验 ---
const quizIndex = ref(0); const quizTotal = ref(20)
const quizCorrect = ref(0); const quizAnswered = ref(false)
const quizFinished = ref(false)
const quizCurrent = ref(null); const quizOptions = ref([])
const quizCorrectIdx = ref(-1); const quizSelectedIdx = ref(-1)
let quizWords = []

async function startQuiz() {
  // 优先从我的生词本中取未掌握的单词
  let myRes = await getVocabularyPage({ page: 1, size: 200 })
  let myWords = myRes.data.records.filter(w => w.mastered === 0)
  // 生词本不够20个，从系统词库补充
  if (myWords.length < 20) {
    const remain = 20 - myWords.length
    const sysRes = await request.get('/vocabulary/system', { params: { page: 1, size: remain } })
    myWords = [...myWords, ...sysRes.data.records]
  }
  const all = myWords.slice(0, 20)
  // 随机选20个
  quizWords = all.sort(() => Math.random() - 0.5).slice(0, Math.min(20, all.length))
  // 为每个题目生成4个选项（含正确答案）
  quizWords = quizWords.map(w => {
    const wrongs = all.filter(x => x.id !== w.id).sort(() => Math.random() - 0.5).slice(0, 3).map(x => x.translation)
    const options = [...wrongs, w.translation].sort(() => Math.random() - 0.5)
    return { ...w, options, correctIdx: options.indexOf(w.translation) }
  })
  quizIndex.value = 0; quizCorrect.value = 0; quizFinished.value = false
  showQuizQuestion()
}

function showQuizQuestion() {
  quizAnswered.value = false; quizSelectedIdx.value = -1
  const q = quizWords[quizIndex.value]
  quizCurrent.value = q; quizOptions.value = q.options; quizCorrectIdx.value = q.correctIdx
}

function answerQuiz(idx) {
  if (quizAnswered.value) return
  quizAnswered.value = true; quizSelectedIdx.value = idx
  if (idx === quizCorrectIdx.value) quizCorrect.value++
}

function nextQuiz() {
  if (quizIndex.value < quizWords.length - 1) { quizIndex.value++; showQuizQuestion() }
  else { quizFinished.value = true }
}

function onTabChange(tab) {
  if (tab === 'mine') fetchMyWords()
  else if (tab === 'system') fetchSystemWords()
  else if (tab === 'quiz') startQuiz()
}

onMounted(() => fetchMyWords())
</script>

<style scoped>
.vocabulary-page { min-height: 100vh; background: #f0f2f5; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.word-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(170px, 1fr)); gap: 10px; }

.word-card {
  background: #fff; border: 1px solid #e8e8e8; border-radius: 10px; padding: 14px;
  transition: all 0.2s; display: flex; flex-direction: column; gap: 6px;
}
.word-card:hover { border-color: #667eea; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.word-card.mastered { opacity: 0.5; background: #f5f5f5; }
.system-word { border-left: 3px solid #667eea; }

.word-text { font-size: 19px; font-weight: 700; color: #1a1a2e; }
.word-trans { font-size: 13px; color: #666; }
.word-meta { display: flex; gap: 4px; }
.word-actions { display: flex; gap: 4px; margin-top: 4px; }

/* Quiz */
.quiz-progress { display: flex; justify-content: space-between; color: #666; margin-bottom: 24px; font-size: 15px; }
.quiz-card { text-align: center; max-width: 500px; margin: 0 auto; }
.quiz-word { font-size: 36px; color: #1a1a2e; margin-bottom: 8px; }
.quiz-hint { color: #999; margin-bottom: 24px; }
.quiz-options { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

.quiz-option {
  padding: 16px; border: 2px solid #e8e8e8; border-radius: 10px; cursor: pointer;
  transition: all 0.2s; font-size: 15px; text-align: left; display: flex; align-items: center; gap: 8px;
}
.quiz-option:hover { border-color: #667eea; background: #f8f9ff; }
.quiz-option.correct { border-color: #67c23a; background: #f0f9eb; }
.quiz-option.wrong { border-color: #f56c6c; background: #fef0f0; }
.quiz-opt-label { font-weight: 700; color: #667eea; min-width: 24px; }
</style>
