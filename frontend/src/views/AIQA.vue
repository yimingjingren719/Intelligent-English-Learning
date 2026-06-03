<template>
  <div class="ai-qa">
    <NavBar />
    <div class="page-container">
      <div class="page-header fade-in">
        <h2>AI 智能答疑</h2>
        <p>有任何英语学习问题，随时问AI助手</p>
      </div>

      <!-- 聊天区域 -->
      <div class="card-wrapper fade-in">
        <div class="chat-container">
          <div class="chat-messages" ref="chatMessages">
            <div v-if="messages.length === 0" class="empty-chat">
              <el-icon :size="48" color="#ccc"><ChatDotRound /></el-icon>
              <p>开始和AI英语助手对话吧！</p>
              <div class="quick-questions">
                <span>试试问：</span>
                <el-tag
                  v-for="q in quickQuestions"
                  :key="q"
                  class="quick-tag"
                  @click="sendQuick(q)"
                >{{ q }}</el-tag>
              </div>
            </div>

            <div v-for="(msg, idx) in messages" :key="idx" class="message" :class="msg.role">
              <div class="message-avatar">
                <el-avatar v-if="msg.role === 'user'" :size="36" icon="UserFilled" />
                <el-avatar v-else :size="36" style="background: #667eea;">
                  <el-icon><ChatDotRound /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-text" v-if="msg.role === 'user'">{{ msg.content }}</div>
                <div class="message-text ai-response" v-else v-html="renderMarkdown(msg.content)"></div>
              </div>
            </div>

            <div v-if="loading" class="message assistant">
              <div class="message-avatar">
                <el-avatar :size="36" style="background: #667eea;">
                  <el-icon><ChatDotRound /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>

          <div class="chat-input-area">
            <el-select v-model="contextType" placeholder="选择上下文" style="width: 140px;" clearable>
              <el-option label="通用问答" value="" />
              <el-option label="语法" value="GRAMMAR" />
              <el-option label="词汇" value="VOCABULARY" />
              <el-option label="阅读" value="READING" />
              <el-option label="写作" value="WRITING" />
            </el-select>
            <el-input
              v-model="inputText"
              placeholder="输入您的英语学习问题..."
              @keyup.enter="sendMessage"
              :disabled="loading"
              class="chat-input"
            />
            <el-button type="primary" @click="sendMessage" :loading="loading" :disabled="!inputText.trim()">
              发送
            </el-button>
          </div>
        </div>
      </div>

      <!-- AI能力画像入口 -->
      <div class="card-wrapper fade-in" style="margin-top: 20px;">
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="feature-card" @click="viewProfile">
              <el-icon :size="28" color="#667eea"><User /></el-icon>
              <span>能力画像</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="feature-card" @click="viewWeakPoints">
              <el-icon :size="28" color="#f5576c"><WarningFilled /></el-icon>
              <span>薄弱点分析</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="feature-card" @click="viewStudyPlan">
              <el-icon :size="28" color="#11998e"><Document /></el-icon>
              <span>学习建议</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import { aiChat, getAIProfile, getWeakPoints, getStudyPlan } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'

const chatMessages = ref(null)
const inputText = ref('')
const contextType = ref('')
const loading = ref(false)
const messages = ref([])

const quickQuestions = [
  '英语时态怎么区分？',
  '如何提高英语阅读能力？',
  '虚拟语气怎么用？',
  '常见介词搭配有哪些？'
]

function renderMarkdown(text) {
  return marked(text || '')
}

function scrollToBottom() {
  nextTick(() => {
    if (chatMessages.value) {
      chatMessages.value.scrollTop = chatMessages.value.scrollHeight
    }
  })
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const res = await aiChat({
      question: text,
      contextType: contextType.value || undefined
    })
    const answer = res.data
    messages.value.push({ role: 'assistant', content: answer })
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，AI服务暂时不可用，请稍后再试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function sendQuick(question) {
  inputText.value = question
  sendMessage()
}

async function viewProfile() {
  try {
    const res = await getAIProfile()
    const data = res.data
    const text = data.aiAnalysis || `正确率: ${data.accuracy}%, 总答题: ${data.totalAnswers}题`
    messages.value.push({ role: 'assistant', content: `📊 **能力画像**\n\n${text}` })
    scrollToBottom()
  } catch (e) {
    ElMessage.error('获取能力画像失败')
  }
}

async function viewWeakPoints() {
  try {
    const res = await getWeakPoints()
    const data = res.data
    const text = data.aiAnalysis || `薄弱知识点: ${data.tags?.join('、') || '暂无'}`
    messages.value.push({ role: 'assistant', content: `🎯 **薄弱点分析**\n\n${text}` })
    scrollToBottom()
  } catch (e) {
    ElMessage.error('获取薄弱点分析失败')
  }
}

async function viewStudyPlan() {
  try {
    const res = await getStudyPlan()
    const data = res.data
    const text = data.studyPlan || '暂无法生成学习建议'
    messages.value.push({ role: 'assistant', content: `📝 **学习建议**\n\n${text}` })
    scrollToBottom()
  } catch (e) {
    ElMessage.error('获取学习建议失败')
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.empty-chat { text-align: center; padding: 60px 20px; color: #999; }

.quick-questions { margin-top: 16px; }
.quick-tag { margin: 4px; cursor: pointer; }
.quick-tag:hover { background: #667eea; color: #fff; }

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message.user { flex-direction: row-reverse; }
.message.user .message-content { display: flex; justify-content: flex-end; }
.message.user .message-text {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  padding: 10px 16px;
  border-radius: 12px 12px 4px 12px;
  max-width: 70%;
}

.message.assistant .message-text {
  background: #fff;
  padding: 12px 16px;
  border-radius: 12px 12px 12px 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  max-width: 85%;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ccc;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
  30% { opacity: 1; transform: scale(1); }
}

.chat-input-area {
  display: flex;
  gap: 8px;
  align-items: center;
}

.chat-input { flex: 1; }

.feature-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: 12px;
  background: #f8f9ff;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 15px;
  font-weight: 500;
  color: #1a1a2e;
}

.feature-card:hover {
  background: #eef0ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102,126,234,0.15);
}
</style>
