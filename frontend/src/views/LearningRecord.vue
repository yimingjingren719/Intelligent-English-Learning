<template>
  <div class="learning-record">
    <NavBar />
    <div class="page-container">
      <div class="page-header fade-in">
        <h2>学习日历</h2>
        <p>记录每一天的学习足迹</p>
      </div>

      <div class="card-wrapper fade-in" style="margin-bottom:16px">
        <div class="month-nav">
          <el-button @click="prevMonth" :icon="ArrowLeft" circle />
          <h3>{{ year }}年 {{ month }}月</h3>
          <el-button @click="nextMonth" :icon="ArrowRight" circle />
          <el-button @click="goToday" size="small" style="margin-left:12px">今天</el-button>
        </div>
      </div>

      <div class="stats-grid fade-in">
        <div class="stat-card"><div class="number">{{ monthStats.testDays }}</div><div class="label">本月学习天数</div></div>
        <div class="stat-card green"><div class="number">{{ monthStats.totalQuestions }}</div><div class="label">本月答题数</div></div>
        <div class="stat-card orange"><div class="number">{{ monthStats.testCount }}</div><div class="label">本月测试次数</div></div>
        <div class="stat-card blue"><div class="number">{{ formatHours(monthStats.totalTime) }}</div><div class="label">本月学习时长</div></div>
      </div>

      <div class="card-wrapper fade-in">
        <div class="calendar-weekdays">
          <span v-for="w in ['一','二','三','四','五','六','日']" :key="w">{{ w }}</span>
        </div>
        <div class="calendar-grid">
          <div v-for="i in firstDayOfWeek" :key="'e'+i" class="calendar-day empty"></div>
          <div
            v-for="day in calendarData" :key="day.date"
            class="calendar-day"
            :class="{ active: day.hasActivity, intense: day.questionCount > 20, today: isToday(day.date), selected: selectedDay === day.date }"
            @click="selectedDay = day.date"
          >
            <span class="day-num">{{ day.date }}</span>
            <div class="day-dots" v-if="day.hasActivity">
              <span v-if="day.testCount > 0" class="dot test"></span>
              <span v-if="day.questionCount > 0" class="dot question"></span>
              <span v-if="day.totalTime > 1800" class="dot time"></span>
            </div>
          </div>
        </div>
        <div class="calendar-legend">
          <span class="legend-item"><span class="dot test"></span> 有测试</span>
          <span class="legend-item"><span class="dot question"></span> 有答题</span>
          <span class="legend-item"><span class="dot time"></span> 超30分钟</span>
        </div>
      </div>

      <div v-if="selectedDay" class="card-wrapper fade-in">
        <h3>{{ year }}/{{ month }}/{{ selectedDay }} 详情</h3>
        <div v-if="selectedData">
          <el-row :gutter="16" style="margin-top:12px">
            <el-col :span="8"><div class="mini-stat"><span class="v">{{ selectedData.testCount }}</span><span class="l">测试次数</span></div></el-col>
            <el-col :span="8"><div class="mini-stat"><span class="v">{{ selectedData.questionCount }}</span><span class="l">答题数</span></div></el-col>
            <el-col :span="8"><div class="mini-stat"><span class="v">{{ formatHours(selectedData.totalTime) }}</span><span class="l">学习时长</span></div></el-col>
          </el-row>
        </div>
        <el-empty v-else description="当天无学习记录" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import request from '@/api/request'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const now = new Date()
const year = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)
const calendarData = ref([])
const selectedDay = ref(null)

const firstDayOfWeek = computed(() => {
  const d = new Date(year.value, month.value - 1, 1).getDay()
  return d === 0 ? 6 : d - 1
})

const monthStats = computed(() => {
  let testDays = 0, totalQuestions = 0, testCount = 0, totalTime = 0
  calendarData.value.forEach(d => {
    if (d.hasActivity) testDays++
    totalQuestions += d.questionCount || 0
    testCount += d.testCount || 0
    totalTime += d.totalTime || 0
  })
  return { testDays, totalQuestions, testCount, totalTime }
})

const selectedData = computed(() => calendarData.value.find(d => d.date === selectedDay.value))

function isToday(d) { return year.value === now.getFullYear() && month.value === now.getMonth() + 1 && d === now.getDate() }
function formatHours(s) { const h = Math.floor(s / 3600); const m = Math.floor((s % 3600) / 60); return h > 0 ? `${h}h${m}m` : `${m}m` }

function prevMonth() { if (month.value === 1) { year.value--; month.value = 12 } else month.value--; fetchCalendar() }
function nextMonth() { if (month.value === 12) { year.value++; month.value = 1 } else month.value++; fetchCalendar() }
function goToday() { year.value = now.getFullYear(); month.value = now.getMonth() + 1; fetchCalendar() }

async function fetchCalendar() {
  try {
    const res = await request.get('/learning/calendar', { params: { year: year.value, month: month.value } })
    calendarData.value = res.data; selectedDay.value = null
  } catch (e) { /* ignore */ }
}

onMounted(() => fetchCalendar())
</script>

<style scoped>
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 16px; }
.month-nav { display: flex; align-items: center; gap: 16px; justify-content: center; }
.month-nav h3 { font-size: 20px; color: #1a1a2e; min-width: 140px; text-align: center; }

.calendar-weekdays { display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; color: #999; font-size: 13px; font-weight: 600; margin-bottom: 8px; }
.calendar-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; }

.calendar-day { aspect-ratio: 1; border-radius: 8px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 2px; background: #f8f9fa; cursor: pointer; transition: all 0.2s; font-size: 13px; }
.calendar-day:hover { background: #eef0ff; }
.calendar-day.empty { background: transparent; cursor: default; }
.calendar-day.active { background: #e8f4fd; }
.calendar-day.intense { background: #bfdbfe; }
.calendar-day.today { border: 2px solid #667eea; font-weight: 700; }
.calendar-day.selected { border: 2px solid #667eea; background: #eef0ff; }

.day-num { color: #333; font-weight: 500; }
.day-dots { display: flex; gap: 3px; }
.dot { width: 6px; height: 6px; border-radius: 50%; }
.dot.test { background: #f56c6c; }
.dot.question { background: #667eea; }
.dot.time { background: #67c23a; }

.calendar-legend { display: flex; gap: 16px; justify-content: center; margin-top: 8px; }
.legend-item { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #999; }

.mini-stat { text-align: center; padding: 16px; background: #f8f9ff; border-radius: 10px; }
.mini-stat .v { display: block; font-size: 22px; font-weight: 700; color: #667eea; }
.mini-stat .l { display: block; font-size: 12px; color: #999; margin-top: 4px; }
</style>
