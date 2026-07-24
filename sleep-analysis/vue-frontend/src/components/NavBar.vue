<template>
  <aside class="sidebar" :class="{ 'sidebar--collapsed': isCollapsed }">
    <div class="sidebar-top">
      <div class="logo-wrapper">
        <div class="logo">
          <span class="logo-mark">⌚</span>
          <div class="logo-text">
            <span class="logo-tag">SMARTWATCH</span>
            <span class="logo-name">智能手表</span>
            <span class="logo-desc">睡眠健康分析预测系统</span>
          </div>
        </div>
        <button class="collapse-btn" @click="toggleCollapse" :title="isCollapsed ? '展开菜单' : '收起菜单'">
          {{ isCollapsed ? '▶' : '◀' }}
        </button>
      </div>

      <nav class="nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          active-class="nav-item--active"
        >
          <span class="nav-dot" :class="item.dotClass"></span>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>
    </div>

    <div class="sidebar-footer">
      <div class="user-profile" v-if="username">
        <div class="user-avatar" :style="{ background: avatarBgColor }">
          {{ username.substring(0, 1).toUpperCase() }}
        </div>
        <div class="user-info">
          <span class="username">{{ username }}</span>
          <a href="javascript:;" class="logout-btn" @click="handleLogout">退出登录</a>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '')
const isCollapsed = ref(localStorage.getItem('sidebar_collapsed') === 'true')

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
  localStorage.setItem('sidebar_collapsed', isCollapsed.value ? 'true' : 'false')
  
  // 期间进行多次 Resize 事件分发，使得 ECharts 能够跟随侧边栏滑出/折叠进行流畅宽度自适应
  const interval = setInterval(() => {
    window.dispatchEvent(new Event('resize'))
  }, 50)
  setTimeout(() => {
    clearInterval(interval)
    window.dispatchEvent(new Event('resize'))
  }, 450)
}

const menuItems = computed(() => {
  return [
    { path: '/screen1', label: '睡眠健康总览', dotClass: 'dot-amber' },
    { path: '/screen2', label: '个人睡眠画像', dotClass: 'dot-sky' },
    { path: '/screen3', label: '生活因子关联', dotClass: 'dot-teal' },
    { path: '/screen4', label: '智能诊断预测', dotClass: 'dot-rose' },
    { path: '/screen5', label: '数据极速检索', dotClass: 'dot-lavender' }
  ]
})

const avatarBgColor = computed(() => {
  const nameVal = username.value || ''
  const colors = ['#c9974e', '#5aab9a', '#6ba8d9', '#a48cdb']
  let code = 0
  for (let i = 0; i < nameVal.length; i++) {
    code += nameVal.charCodeAt(i)
  }
  return colors[code % colors.length]
})

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
}

onMounted(async () => {
  if (!username.value && localStorage.getItem('token')) {
    try {
      const res = await request.get('/auth/info')
      if (res && res.username) {
        username.value = res.username
        localStorage.setItem('username', res.username)
      }
    } catch (e) {
      console.error('自动拉取用户名失败:', e)
    }
  }
})
</script>

<style scoped>
.sidebar {
  width: 200px;
  min-width: 200px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 32px 20px 28px;
  justify-content: space-between;
  transition: width 0.4s cubic-bezier(0.16, 1, 0.3, 1), min-width 0.4s cubic-bezier(0.16, 1, 0.3, 1), padding 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
}

.sidebar--collapsed {
  width: 68px;
  min-width: 68px;
  padding: 32px 8px 28px;
}

.sidebar-top {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-mark {
  font-size: 19px;
  color: var(--accent-amber);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(201, 151, 78, 0.08);
  border: 1px solid rgba(201, 151, 78, 0.18);
  line-height: 1;
  box-shadow: 0 0 10px rgba(201, 151, 78, 0.08);
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.logo:hover .logo-mark {
  transform: rotate(15deg) scale(1.05);
  background: rgba(201, 151, 78, 0.16);
  border-color: rgba(201, 151, 78, 0.4);
  box-shadow: 0 0 15px rgba(201, 151, 78, 0.25);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.collapse-btn {
  background: transparent;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  font-size: 11px;
  padding: 4px 6px;
  border-radius: 4px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.collapse-btn:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.05);
}

.sidebar--collapsed .collapse-btn {
  position: absolute;
  right: -9px;
  top: 40px;
  background: var(--bg-card);
  border: 1px solid var(--border-card);
  border-radius: 50%;
  width: 20px;
  height: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
  z-index: 10;
  color: var(--text-primary);
}

.logo-text {
  display: flex;
  flex-direction: column;
  transition: opacity 0.25s, width 0.25s;
  opacity: 1;
  width: auto;
}

.sidebar--collapsed .logo-text {
  opacity: 0;
  width: 0;
  height: 0;
  overflow: hidden;
  pointer-events: none;
}

.logo-tag {
  font-size: 8.5px;
  font-weight: 700;
  color: var(--accent-amber);
  letter-spacing: 0.15em;
  text-transform: uppercase;
  margin-bottom: 2px;
  opacity: 0.9;
  text-shadow: 0 0 8px rgba(201, 151, 78, 0.25);
  font-family: var(--font-mono);
}

.logo-name {
  font-family: var(--font-title);
  font-size: 14.5px;
  font-weight: 700;
  color: #eeede6;
  letter-spacing: 0.05em;
  line-height: 1.25;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.15);
}

.logo-desc {
  font-family: var(--font-title);
  font-size: 10px;
  color: rgba(255, 255, 255, 0.38);
  font-weight: 500;
  letter-spacing: 0.04em;
  margin-top: 2px;
  line-height: 1.35;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 14px;
  color: var(--text-tertiary);
  border-radius: var(--radius-sm);
  font-size: 13.5px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  letter-spacing: -0.01em;
  border-left: 2px solid transparent;
}

.nav-item:hover {
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.02);
}

.nav-item--active {
  color: var(--text-primary);
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.04) 0%, rgba(255, 255, 255, 0.01) 100%);
  font-weight: 500;
  border-left-color: var(--accent-amber);
  padding-left: 12px;
}

.nav-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
  transition: all 0.25s ease;
}

.nav-item:not(.nav-item--active) .nav-dot {
  opacity: 0.35;
}

.nav-item--active .nav-dot {
  opacity: 1;
  box-shadow: 0 0 8px currentColor;
}

.dot-amber { background: var(--accent-amber); color: var(--accent-amber); }
.dot-sky { background: var(--accent-sky); color: var(--accent-sky); }
.dot-teal { background: var(--accent-teal); color: var(--accent-teal); }
.dot-rose { background: var(--accent-rose); color: var(--accent-rose); }
.dot-lavender { background: var(--accent-lavender); color: var(--accent-lavender); }

.nav-label {
  white-space: nowrap;
  transition: opacity 0.2s, width 0.2s;
  opacity: 1;
}

.sidebar--collapsed .nav-label {
  opacity: 0;
  width: 0;
  overflow: hidden;
  pointer-events: none;
}

.sidebar--collapsed .nav-item {
  justify-content: center;
  padding: 11px 0;
  gap: 0;
  border-left-width: 0;
}

.sidebar--collapsed .nav-item--active {
  padding-left: 0;
  border-left-width: 0;
}

.sidebar-footer {
  padding-top: 20px;
  border-top: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* User Profile footer status */
.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.01);
  border: 1px solid rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  padding: 8px 10px;
  transition: all 0.3s;
}

.user-profile:hover {
  background: rgba(255, 255, 255, 0.03);
  border-color: rgba(255, 255, 255, 0.05);
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: #fff;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.2);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  transition: opacity 0.2s, width 0.2s;
  opacity: 1;
}

.sidebar--collapsed .user-info {
  opacity: 0;
  width: 0;
  height: 0;
  overflow: hidden;
  pointer-events: none;
}

.sidebar--collapsed .user-profile {
  justify-content: center;
  padding: 8px 0;
}

.username {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.02em;
}

.logout-btn {
  font-size: 9.5px;
  color: var(--text-tertiary);
  text-decoration: none;
  transition: color 0.2s;
}

.logout-btn:hover {
  color: var(--accent-rose);
}
</style>
