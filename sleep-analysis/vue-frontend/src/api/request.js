import axios from 'axios'

const request = axios.create({
  baseURL: '/api',   // 走 vite proxy 转发给 Spring Boot，见 vite.config.js
  timeout: 15000
})

// 请求拦截器：携带 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一剥离 data，并且拦截 401 状态码进行自动重定向
request.interceptors.response.use(
  (response) => {
    const body = response.data
    // 捕获 401 业务状态码
    if (body && body.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      window.location.hash = '/login'
      return Promise.reject(new Error(body.message || '登录过期，请重新登录'))
    }
    if (body && body.code !== 200) {
      console.error('[API业务错误]', body.message)
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body ? body.data : null
  },
  (error) => {
    console.error('[API请求异常]', error)
    // 捕获 401 HTTP 状态码
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      window.location.hash = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
