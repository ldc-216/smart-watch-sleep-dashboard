import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/screen1' },
  { 
    path: '/login', 
    name: 'login', 
    component: () => import('../views/Login.vue'), 
    meta: { title: '平台登录', isPublic: true } 
  },
  { 
    path: '/screen1', 
    name: 'screen1', 
    component: () => import('../views/Screen1Overview.vue'), 
    meta: { title: '总体睡眠健康大屏' } 
  },
  { 
    path: '/screen2', 
    name: 'screen2', 
    component: () => import('../views/Screen2Profile.vue'), 
    meta: { title: '个人睡眠画像与指导' } 
  },
  { 
    path: '/screen3', 
    name: 'screen3', 
    component: () => import('../views/Screen3Correlation.vue'), 
    meta: { title: '生活与环境因素关联' } 
  },
  { 
    path: '/screen4', 
    name: 'screen4', 
    component: () => import('../views/Screen4Prediction.vue'), 
    meta: { title: '智能预测与诊断预警' } 
  },
  { 
    path: '/screen5', 
    name: 'screen5', 
    component: () => import('../views/Screen5Search.vue'), 
    meta: { title: '极速检索与高危日志' } 
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由全局前置守卫：鉴权拦截
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  
  if (to.meta.title) {
    document.title = to.meta.title
  }

  // 如果访问的不是公开页面，且本地没有 Token，则强制重定向到 /login
  if (!to.meta.isPublic && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
