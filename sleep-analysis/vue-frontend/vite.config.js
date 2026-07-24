import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 开发环境下把 /api 转发给 Spring Boot (localhost:8888)，避免跨域问题
      '/api': {
        target: 'http://localhost:8888',
        changeOrigin: true
      }
    }
  }
})
