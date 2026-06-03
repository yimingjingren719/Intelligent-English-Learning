<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import request from '@/api/request'

const userStore = useUserStore()
const router = useRouter()
let heartbeatTimer = null

function startHeartbeat() {
  if (heartbeatTimer) return
  heartbeatTimer = setInterval(() => {
    if (userStore.isLoggedIn) {
      request.post('/learning/heartbeat').catch(() => {})
    }
  }, 60000) // 每60秒上报一次
}

function stopHeartbeat() {
  clearInterval(heartbeatTimer)
  heartbeatTimer = null
}

onMounted(() => {
  userStore.restoreLogin()
  startHeartbeat()
})

onUnmounted(() => {
  stopHeartbeat()
})
</script>
