<template>
  <div class="dashboard-shell">
    <!-- Dynamic Cosmic Nebula Glow Spheres -->
    <div class="nebula-sphere nebula-1"></div>
    <div class="nebula-sphere nebula-2"></div>
    <div class="nebula-sphere nebula-3"></div>

    <!-- Conditional Navigation Menu (Hides on Login Page) -->
    <NavBar v-if="showNavBar" />
    
    <div class="dashboard-content">
      <header class="top-header" v-if="showNavBar">
        <div class="header-clock">{{ now }}</div>
      </header>
      
      <main class="dashboard-main" :style="{ padding: showNavBar ? '8px 44px 48px' : '24px 0' }">
        <router-view />
      </main>
    </div>

    <!-- Dynamic AI Assistant (Hides on Login Page) -->
    <AiAssistant v-if="showNavBar" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from './components/NavBar.vue'
import AiAssistant from './components/AiAssistant.vue'

const router = useRouter()
const showNavBar = computed(() => {
  return router && router.currentRoute && router.currentRoute.value
    ? router.currentRoute.value.path !== '/login'
    : true
})

const now = ref('')
let timer = null
const tick = () => {
  const d = new Date()
  const day = String(d.getDate()).padStart(2, '0')
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const year = d.getFullYear()
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  now.value = `${year}/${month}/${day}  ${h}:${m}:${s}`
}
onMounted(() => { tick(); timer = setInterval(tick, 1000) })
onUnmounted(() => clearInterval(timer))
</script>
