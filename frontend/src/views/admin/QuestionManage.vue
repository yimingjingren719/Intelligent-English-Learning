<template>
  <div class="question-manage">
    <NavBar />
    <div class="page-container">
      <div class="card-wrapper fade-in">
        <div class="page-header">
          <h2>题库管理</h2>
          <el-button type="primary" @click="openAddDialog">添加题目</el-button>
        </div>

        <!-- 筛选 -->
        <div class="filter-bar">
          <div class="filter-item">
            <label>类型：</label>
            <el-select v-model="filterQuestionType" placeholder="全部类型" style="width:140px">
              <el-option label="选词填空" value="BANKED_CLOZE" />
              <el-option label="长篇匹配" value="LONG_MATCH" />
              <el-option label="仔细阅读" value="CAREFUL_READING" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>难度：</label>
            <el-select v-model="filterDifficulty" placeholder="全部难度" style="width:120px">
              <el-option v-for="n in 10" :key="n" :label="String(n)" :value="n" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>关键词：</label>
            <el-input v-model="filterKeyword" placeholder="搜索题目内容" clearable style="width:200px" />
          </div>
          <div class="filter-item">
            <el-button type="primary" @click="fetchQuestions">搜索</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>
          <div class="filter-item" style="color:#999;font-size:12px">
            当前：类型={{ filterQuestionType || '全部' }}  难度={{ filterDifficulty || '全部' }}
          </div>
        </div>

        <!-- 表格 -->
        <el-table :data="questions" v-loading="loading" border stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag>{{ typeLabel(row.questionType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="题目内容" min-width="250" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-html="row.content?.substring(0, 80)"></span>
            </template>
          </el-table-column>
          <el-table-column prop="correctAnswer" label="答案" width="100" />
          <el-table-column prop="difficulty" label="难度" width="80" />
          <el-table-column prop="tags" label="标签" width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="total > 0"
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchQuestions"
          style="margin-top: 16px; justify-content: center;"
        />
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑题目' : '添加题目'"
      width="700px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="题目类型" prop="questionType">
          <el-select v-model="form.questionType" :disabled="isEdit">
            <el-option label="选词填空" value="BANKED_CLOZE" />
            <el-option label="长篇匹配" value="LONG_MATCH" />
            <el-option label="仔细阅读" value="CAREFUL_READING" />
          </el-select>
        </el-form-item>

        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="支持HTML格式" />
        </el-form-item>

        <el-form-item label="选项(JSON)" prop="options">
          <el-input v-model="form.options" type="textarea" :rows="4"
            :placeholder="optionsPlaceholder" />
        </el-form-item>

        <el-form-item label="正确答案" prop="correctAnswer">
          <el-input v-model="form.correctAnswer" :placeholder="answerPlaceholder" />
        </el-form-item>

        <el-form-item label="答案解析" prop="analysis">
          <el-input v-model="form.analysis" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item label="难度" prop="difficulty">
          <el-slider v-model="form.difficulty" :min="1" :max="10" show-input />
        </el-form-item>

        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="逗号分隔，如：语法,时态" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import { getQuestionsPage, addQuestion, updateQuestion, deleteQuestion } from '@/api/question'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const questions = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filterQuestionType = ref('')
const filterDifficulty = ref(null)
const filterKeyword = ref('')

// 弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  questionType: 'SINGLE_CHOICE',
  content: '',
  options: '',
  correctAnswer: '',
  analysis: '',
  difficulty: 5,
  tags: ''
})

const rules = {
  questionType: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  correctAnswer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

onMounted(() => fetchQuestions())

async function fetchQuestions() {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (filterQuestionType.value) params.questionType = filterQuestionType.value
    if (filterDifficulty.value) params.difficulty = filterDifficulty.value
    if (filterKeyword.value) params.keyword = filterKeyword.value
    const res = await getQuestionsPage(params)
    questions.value = res.data.records
    total.value = res.data.total
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

function onFilterChange(name, val) {
  console.log('[Filter]', name, '→', val, '| ref values:', filterQuestionType.value, filterDifficulty.value)
}

function resetFilters() {
  filterQuestionType.value = ''
  filterDifficulty.value = null
  filterKeyword.value = ''
  fetchQuestions()
}

function typeLabel(type) {
  const map = { BANKED_CLOZE: '选词填空', LONG_MATCH: '长篇匹配', CAREFUL_READING: '仔细阅读', SINGLE_CHOICE: '单选题', TRUE_FALSE: '判断题', FILL_BLANK: '填空题', READING: '阅读理解' }
  return map[type] || type
}

const optionsPlaceholder = computed(() => {
  const m = {
    BANKED_CLOZE: '[{"label":"A","text":"单词1"},...,{"label":"O","text":"单词15"}] 15选10的候选词',
    LONG_MATCH: '[{"label":"1","text":"陈述1内容"},...,{"label":"10","text":"陈述10内容"}] 10个匹配陈述',
    CAREFUL_READING: '[{"label":"A","text":"选项A"},{"label":"B","text":"选项B"},{"label":"C","text":"选项C"},{"label":"D","text":"选项D"}] 四选一选项'
  }
  return m[form.questionType] || 'JSON格式选项'
})

const answerPlaceholder = computed(() => {
  const m = {
    BANKED_CLOZE: '15选10的答案序列，如：A,B,C,D,E,F,G,H,I,J（逗号分隔10个字母）',
    LONG_MATCH: '10个段落匹配，如：F,B,H,A,D,J,C,I,E,G（逗号分隔10个字母）',
    CAREFUL_READING: '用于标记文章填PASSAGE，小题填A/B/C/D'
  }
  return m[form.questionType] || '填写正确答案'
})

function openAddDialog() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    questionType: row.questionType,
    content: row.content,
    options: row.options ? (typeof row.options === 'string' ? row.options : JSON.stringify(row.options)) : '',
    correctAnswer: row.correctAnswer,
    analysis: row.analysis || '',
    difficulty: row.difficulty,
    tags: row.tags || ''
  })
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    questionType: 'BANKED_CLOZE', content: '', options: '', correctAnswer: '',
    analysis: '', difficulty: 5, tags: ''
  })
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      await updateQuestion(editId.value, data)
      ElMessage.success('更新成功')
    } else {
      await addQuestion(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchQuestions()
  } catch (e) { /* ignore */ }
  finally { submitting.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这道题目吗？', '确认删除', { type: 'warning' })
    await deleteQuestion(id)
    ElMessage.success('删除成功')
    fetchQuestions()
  } catch (e) { /* ignore */ }
}
</script>

<style scoped>
.filter-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 16px; margin-bottom: 16px; }
.filter-item { display: flex; align-items: center; gap: 6px; }
.filter-item label { font-size: 14px; color: #606266; white-space: nowrap; }
</style>
