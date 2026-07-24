<template>
  <div class="ai-assistant-wrapper">
    <!-- Floating Action Button -->
    <button class="ai-floating-btn" :class="{ open: isOpen }" @click="toggleWindow">
      <div class="glow-ring"></div>
      <span class="btn-icon">🤖</span>
      <span class="btn-tooltip" v-if="!isOpen">AI 睡眠助手</span>
    </button>

    <!-- Glassmorphic Chat Window -->
    <transition name="fade-slide">
      <div class="ai-chat-window glass-card" v-if="isOpen">
        <!-- Header -->
        <div class="chat-header">
          <div class="header-meta">
            <span class="status-indicator online"></span>
            <span class="header-title">睡眠健康 AI 智能助手</span>
          </div>
          <button class="header-close" @click="isOpen = false">&times;</button>
        </div>

        <!-- Messages scrollable area -->
        <div class="chat-messages" ref="msgListRef">
          <div class="msg-bubble system">
            <span class="bubble-icon">🩺</span>
            <div class="bubble-content">
              你好！我是你的专属睡眠健康助手。我已同步调阅你的睡眠健康监测报告。你可以点击下方快捷问题，或者直接打字向我咨询关于睡眠状况、行为（CBT-I）干预或环境优化建议。
            </div>
          </div>

          <div 
            v-for="(msg, index) in chatList" 
            :key="index" 
            class="msg-bubble" 
            :class="msg.role"
          >
            <span class="bubble-icon">{{ msg.role === 'user' ? '👤' : '🤖' }}</span>
            <div class="bubble-content" v-html="formatMarkdown(msg.content)"></div>
          </div>

          <!-- Loading state -->
          <div class="msg-bubble assistant loading" v-if="submitting">
            <span class="bubble-icon">🤖</span>
            <div class="bubble-content">
              <span class="dot-typing"></span>
            </div>
          </div>
        </div>

        <!-- Quick Tags -->
        <div class="quick-tags-wrap" v-if="chatList.length === 0 && !submitting">
          <button 
            v-for="(tag, idx) in quickPrompts" 
            :key="idx" 
            class="quick-tag-btn"
            @click="sendQuickPrompt(tag)"
          >
            {{ tag }}
          </button>
        </div>

        <!-- Input Area -->
        <div class="chat-input-area">
          <input 
            type="text" 
            v-model="inputMsg" 
            @keyup.enter="sendMessage"
            placeholder="输入提问，例如：'分析我的睡眠报告'..." 
            class="chat-input"
            :disabled="submitting"
          />
          <button class="btn-send" @click="sendMessage" :disabled="submitting || !inputMsg.trim()">
            发送
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import request from '../api/request'

const isOpen = ref(false)
const submitting = ref(false)
const inputMsg = ref('')
const chatList = ref([])
const msgListRef = ref(null)

const quickPrompts = [
  '📊 分析我的睡眠健康报告',
  '🌡️ 如何改善我的入睡延迟？',
  '🛌 夜间打鼾与最低血氧有什么风险？',
  '☕ 日常咖啡因与屏幕暴露有什么建议？'
]

const toggleWindow = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    scrollBottom()
  }
}

const sendQuickPrompt = (tag) => {
  const query = tag.replace(/[\uE000-\uF8FF]|\uD83C[\uDC00-\uDFFF]|\uD83D[\uDC00-\uDFFF]|[\u2011-\u26FF]|\uD83E[\uDD00-\uDFFF]/g, '').trim()
  inputMsg.value = query
  sendMessage()
}

const sendMessage = async () => {
  if (!inputMsg.value || !inputMsg.value.trim() || submitting.value) return
  
  const userText = inputMsg.value.trim()
  chatList.value.push({ role: 'user', content: userText })
  inputMsg.value = ''
  submitting.value = true
  scrollBottom()

  try {
    const res = await request({
      url: '/ai/chat',
      method: 'post',
      data: { message: userText }
    })
    chatList.value.push({ role: 'assistant', content: res })
  } catch (err) {
    chatList.value.push({ role: 'assistant', content: '连接服务失败: ' + (err.message || '大模型暂时无法响应') })
  } finally {
    submitting.value = false
    scrollBottom()
  }
}

const scrollBottom = () => {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    }
  })
}

// Quick markdown formatter for bold, italic, code and lists
const formatMarkdown = (text) => {
  if (!text) return ''
  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br/>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code style="background:rgba(255,255,255,0.06);padding:2px 4px;border-radius:4px;font-family:var(--font-mono)">$1</code>')
  return html
}
</script>

<style scoped>
.ai-assistant-wrapper {
  position: relative;
}

/* Floating Action Button */
.ai-floating-btn {
  position: fixed;
  bottom: 50px;
  right: 40px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(20, 20, 30, 0.9) 0%, rgba(15, 15, 22, 0.95) 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  cursor: pointer;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.ai-floating-btn:hover {
  transform: scale(1.08) rotate(8deg);
  border-color: var(--accent-teal);
  box-shadow: 0 8px 32px rgba(90, 171, 154, 0.35);
}

.ai-floating-btn.open {
  transform: scale(0.95) rotate(-15deg);
  border-color: var(--accent-amber);
  box-shadow: 0 8px 32px rgba(201, 151, 78, 0.3);
}

.btn-icon {
  font-size: 24px;
  position: relative;
  z-index: 10;
}

.glow-ring {
  position: absolute;
  inset: -2px;
  border-radius: 50%;
  border: 1px solid rgba(90, 171, 154, 0.35);
  animation: pulse-glow 2s infinite alternate;
  pointer-events: none;
}
.ai-floating-btn.open .glow-ring {
  border-color: rgba(201, 151, 78, 0.35);
}

.btn-tooltip {
  position: absolute;
  right: 70px;
  background: rgba(15, 15, 20, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.06);
  padding: 4px 10px;
  border-radius: 6px;
  color: var(--text-secondary);
  font-size: 10.5px;
  font-weight: 600;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}
.ai-floating-btn:hover .btn-tooltip {
  opacity: 1;
}

/* Chat Window */
.ai-chat-window {
  position: fixed;
  bottom: 120px;
  right: 40px;
  width: 600px;
  height: 840px;
  max-height: calc(100vh - 180px);
  border-radius: 14px;
  z-index: 9998;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(20, 20, 30, 0.75) 0%, rgba(12, 12, 18, 0.6) 100%);
  border: 1.5px solid rgba(255, 255, 255, 0.09);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(24px);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.header-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-indicator {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}
.status-indicator.online {
  background: var(--accent-teal);
  box-shadow: 0 0 6px var(--accent-teal);
}

.header-title {
  font-size: 15.5px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.header-close {
  background: none;
  border: none;
  color: var(--text-tertiary);
  font-size: 20px;
  cursor: pointer;
  transition: color 0.15s ease;
}
.header-close:hover {
  color: var(--accent-rose);
}

/* Chat Messages */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  scroll-behavior: smooth;
}

/* Scrollbar styles */
.chat-messages::-webkit-scrollbar {
  width: 4px;
}
.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}
.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
}
.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.15);
}

.msg-bubble {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  max-width: 90%;
  animation: fade-in-up 0.25s ease-out;
}

.msg-bubble.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.bubble-icon {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  flex-shrink: 0;
}
.msg-bubble.user .bubble-icon {
  background: rgba(201, 151, 78, 0.12);
  border-color: rgba(201, 151, 78, 0.2);
}
.msg-bubble.assistant .bubble-icon {
  background: rgba(90, 171, 154, 0.12);
  border-color: rgba(90, 171, 154, 0.2);
}

.bubble-content {
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13.5px;
  line-height: 1.55;
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.018);
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.msg-bubble.user .bubble-content {
  background: rgba(201, 151, 78, 0.12);
  border-color: rgba(201, 151, 78, 0.25);
  color: var(--accent-amber);
  border-top-right-radius: 2px;
}

.msg-bubble.assistant .bubble-content {
  background: rgba(90, 171, 154, 0.05);
  border-color: rgba(90, 171, 154, 0.15);
  color: var(--text-primary);
  border-top-left-radius: 2px;
}

.msg-bubble.system .bubble-content {
  background: rgba(255, 255, 255, 0.01);
  border-color: rgba(255, 255, 255, 0.04);
  color: var(--text-secondary);
}

/* Typing indicator */
.dot-typing {
  position: relative;
  left: -9999px;
  width: 4px;
  height: 4px;
  border-radius: 2.5px;
  background-color: var(--text-tertiary);
  color: var(--text-tertiary);
  box-shadow: 9984px 0 0 0 var(--text-tertiary), 9992px 0 0 0 var(--text-tertiary), 10000px 0 0 0 var(--text-tertiary);
  animation: dot-typing 1.5s infinite linear;
  margin: 6px 12px 6px 4px;
}

@keyframes dot-typing {
  0% {
    box-shadow: 9984px 0 0 0 var(--text-tertiary), 9992px 0 0 0 var(--text-tertiary), 10000px 0 0 0 var(--text-tertiary);
  }
  16.667% {
    box-shadow: 9984px -4px 0 0 var(--text-tertiary), 9992px 0 0 0 var(--text-tertiary), 10000px 0 0 0 var(--text-tertiary);
  }
  33.333% {
    box-shadow: 9984px 0 0 0 var(--text-tertiary), 9992px -4px 0 0 var(--text-tertiary), 10000px 0 0 0 var(--text-tertiary);
  }
  50% {
    box-shadow: 9984px 0 0 0 var(--text-tertiary), 9992px 0 0 0 var(--text-tertiary), 10000px -4px 0 0 var(--text-tertiary);
  }
  66.667% {
    box-shadow: 9984px 0 0 0 var(--text-tertiary), 9992px 0 0 0 var(--text-tertiary), 10000px 0 0 0 var(--text-tertiary);
  }
  100% {
    box-shadow: 9984px 0 0 0 var(--text-tertiary), 9992px 0 0 0 var(--text-tertiary), 10000px 0 0 0 var(--text-tertiary);
  }
}

/* Quick Tags */
.quick-tags-wrap {
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: rgba(0, 0, 0, 0.1);
  border-top: 1px solid rgba(255, 255, 255, 0.04);
}

.quick-tag-btn {
  width: 100%;
  padding: 8px 14px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.025);
  border: 1px solid rgba(255, 255, 255, 0.06);
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}
.quick-tag-btn:hover {
  background: rgba(90, 171, 154, 0.08);
  border-color: rgba(90, 171, 154, 0.25);
  color: var(--accent-teal);
  padding-left: 16px;
}

/* Input Area */
.chat-input-area {
  display: flex;
  gap: 10px;
  padding: 14px 16px;
  background: rgba(0, 0, 0, 0.25);
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.chat-input {
  flex: 1;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  padding: 10px 14px;
  color: var(--text-primary);
  font-size: 13px;
  outline: none;
  transition: all 0.2s ease;
}
.chat-input:focus {
  border-color: var(--accent-teal);
  background: rgba(90, 171, 154, 0.05);
}

.btn-send {
  padding: 0 20px;
  border-radius: 6px;
  border: none;
  background: linear-gradient(135deg, var(--accent-teal) 0%, rgba(90, 171, 154, 0.8) 100%);
  color: #0f0f15;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-send:hover:not(:disabled) {
  filter: brightness(1.1);
  box-shadow: 0 0 10px rgba(90, 171, 154, 0.35);
}
.btn-send:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

/* Transitions */
.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.fade-slide-enter-from, .fade-slide-leave-to {
  transform: translateY(20px) scale(0.95);
  opacity: 0;
}

@keyframes pulse-glow {
  0% {
    transform: scale(0.96);
    opacity: 0.35;
  }
  100% {
    transform: scale(1.06);
    opacity: 0.7;
  }
}

@keyframes fade-in-up {
  from {
    transform: translateY(8px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
