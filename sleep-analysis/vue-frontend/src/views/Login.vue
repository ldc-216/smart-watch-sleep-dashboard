<template>
  <div class="login-container">
    <!-- Background Dots Grid -->
    <div class="dots-bg"></div>
    
    <!-- Decorative Glowing Lights -->
    <div class="glow-light glow-amber"></div>
    <div class="glow-light glow-teal"></div>

    <div class="login-card-wrap">
      <div class="login-card" :class="{ 'is-flipped': isRegister }">
        
        <!-- 登录面板 (Front) -->
        <div class="card-face card-front">
          <div class="logo">
            <span class="logo-mark">◑</span>
            <div class="logo-text">
              <span class="logo-name">睡眠健康</span>
              <span class="logo-desc">大数据分析平台</span>
            </div>
          </div>

          <h2 class="card-title">平台登录</h2>

          <form @submit.prevent="handleLogin" class="auth-form">
            <div class="form-group">
              <label class="form-label" for="login-username">用户名</label>
              <input 
                id="login-username"
                name="username"
                v-model="loginForm.username" 
                type="text" 
                class="form-input" 
                placeholder="请输入用户名" 
                required 
              />
            </div>
            
            <div class="form-group">
              <label class="form-label" for="login-password">密码</label>
              <input 
                id="login-password"
                name="password"
                v-model="loginForm.password" 
                type="password" 
                class="form-input" 
                placeholder="请输入密码" 
                required 
              />
            </div>

            <div class="error-msg" v-if="errorMsg">{{ errorMsg }}</div>

            <button type="submit" class="btn-submit" :disabled="submitting">
              {{ submitting ? '验证中...' : '立即登录' }}
            </button>
          </form>

          <div class="auth-switch">
            还没有账号？ <a href="javascript:;" @click="toggleMode(true)">立即注册</a>
          </div>
        </div>

        <!-- 注册面板 (Back) -->
        <div class="card-face card-back">
          <div class="logo">
            <span class="logo-mark">◑</span>
            <div class="logo-text">
              <span class="logo-name">睡眠健康</span>
              <span class="logo-desc">大数据分析平台</span>
            </div>
          </div>

          <h2 class="card-title">新用户注册</h2>

          <form @submit.prevent="handleRegister" class="auth-form">
            <div class="form-group">
              <label class="form-label" for="register-username">用户名</label>
              <input 
                id="register-username"
                name="username"
                v-model="registerForm.username" 
                type="text" 
                class="form-input" 
                placeholder="数字与字母组合" 
                required 
              />
            </div>
            
            <div class="form-group">
              <label class="form-label" for="register-nickname">昵称</label>
              <input 
                id="register-nickname"
                name="nickname"
                v-model="registerForm.nickname" 
                type="text" 
                class="form-input" 
                placeholder="个性化昵称" 
              />
            </div>

            <div class="form-group">
              <label class="form-label" for="register-password">密码</label>
              <input 
                id="register-password"
                name="password"
                v-model="registerForm.password" 
                type="password" 
                class="form-input" 
                placeholder="不少于 6 位" 
                required 
              />
            </div>

            <div class="error-msg" v-if="errorMsg">{{ errorMsg }}</div>

            <button type="submit" class="btn-submit btn-register" :disabled="submitting">
              {{ submitting ? '注册中...' : '注册账户' }}
            </button>
          </form>

          <div class="auth-switch">
            已有账号？ <a href="javascript:;" @click="toggleMode(false)">返回登录</a>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'

const router = useRouter()
const isRegister = ref(false)
const submitting = ref(false)
const errorMsg = ref('')

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  nickname: '',
  password: ''
})

function toggleMode(toRegister) {
  isRegister.value = toRegister
  errorMsg.value = ''
}

async function handleLogin() {
  submitting.value = true
  errorMsg.value = ''
  try {
    const res = await request.post('/auth/login', loginForm)
    localStorage.setItem('token', res.token)
    localStorage.setItem('username', res.username)
    router.push('/')
  } catch (err) {
    errorMsg.value = err.message || '登录失败，请检查网络'
  } finally {
    submitting.value = false
  }
}

async function handleRegister() {
  if (registerForm.password.length < 6) {
    errorMsg.value = '密码长度不能小于 6 位'
    return
  }
  submitting.value = true
  errorMsg.value = ''
  try {
    await request.post('/auth/register', registerForm)
    alert('注册成功，请登录！')
    loginForm.username = registerForm.username
    loginForm.password = ''
    toggleMode(false)
  } catch (err) {
    errorMsg.value = err.message || '注册失败，用户名可能已存在'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #08080f;
  position: fixed;
  top: 0;
  left: 0;
  overflow: hidden;
  z-index: 9999;
}

/* Dots Background */
.dots-bg {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px);
  background-size: 16px 16px;
  pointer-events: none;
  z-index: 1;
}

/* Glowing background spheres */
.glow-light {
  position: absolute;
  width: 350px;
  height: 350px;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.35;
  pointer-events: none;
  z-index: 1;
}

.glow-amber {
  background: var(--accent-amber);
  top: 15%;
  left: 20%;
}

.glow-teal {
  background: var(--accent-teal);
  bottom: 15%;
  right: 20%;
}

/* Card 3D Flip wrap */
.login-card-wrap {
  width: 380px;
  height: 480px;
  perspective: 1200px;
  z-index: 2;
}

.login-card {
  width: 100%;
  height: 100%;
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.15);
}

.login-card.is-flipped {
  transform: rotateY(180deg);
}

/* Card Face base */
.card-face {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  background: rgba(20, 20, 30, 0.55);
  backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 20px;
  padding: 36px 32px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.6);
}

.card-front {
  transform: rotateY(0deg);
  z-index: 2;
}

.card-back {
  transform: rotateY(180deg);
  z-index: 1;
}

/* Logo and brand */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
}

.logo-mark {
  font-size: 20px;
  color: var(--accent-teal);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: var(--radius-sm);
  background: rgba(90, 171, 154, 0.08);
  line-height: 1;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-name {
  font-family: var(--font-title);
  font-size: 12.5px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.04em;
  line-height: 1.2;
}

.logo-desc {
  font-family: var(--font-title);
  font-size: 11.5px;
  color: var(--text-secondary);
  letter-spacing: 0.04em;
  margin-top: 1px;
  line-height: 1.2;
}

.card-title {
  font-family: var(--font-title);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
  letter-spacing: 0.02em;
}

/* Form Styles */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 10.5px;
  color: var(--text-tertiary);
  font-weight: 500;
  letter-spacing: 0.01em;
}

.form-input {
  background: rgba(255, 255, 255, 0.02);
  color: var(--text-primary);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  font-family: var(--font-sans);
  outline: none;
  transition: all 0.3s ease;
}

.form-input:focus {
  border-color: var(--accent-amber);
  background: rgba(255, 255, 255, 0.04);
  box-shadow: 0 0 10px rgba(201, 151, 78, 0.1);
}

.card-back .form-input:focus {
  border-color: var(--accent-teal);
  box-shadow: 0 0 10px rgba(90, 171, 154, 0.1);
}

.error-msg {
  font-size: 11px;
  color: var(--accent-rose);
  margin-top: -4px;
  letter-spacing: 0.01em;
}

.btn-submit {
  background: linear-gradient(135deg, var(--accent-amber) 0%, #a47633 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 12px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
  box-shadow: 0 8px 20px rgba(201, 151, 78, 0.15);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 25px rgba(201, 151, 78, 0.25);
}

.btn-submit:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.btn-register {
  background: linear-gradient(135deg, var(--accent-teal) 0%, #3e8073 100%);
  box-shadow: 0 8px 20px rgba(90, 171, 154, 0.15);
}

.btn-register:hover:not(:disabled) {
  box-shadow: 0 10px 25px rgba(90, 171, 154, 0.25);
}

.auth-switch {
  font-size: 11.5px;
  color: var(--text-tertiary);
  text-align: center;
  margin-top: 20px;
}

.auth-switch a {
  color: var(--accent-teal);
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
  transition: color 0.2s;
}

.card-back .auth-switch a {
  color: var(--accent-amber);
}

.auth-switch a:hover {
  text-decoration: underline;
}
</style>
