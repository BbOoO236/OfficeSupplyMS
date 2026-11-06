import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '/api',
  timeout: 15000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    if (store.getters.token) {
      config.headers['Authorization'] = 'Bearer ' + store.getters.token
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果返回的状态码不是200，则判断为错误
    if (res.code !== 200) {
      // 401: 未登录或token过期，跳转到登录页面
      if (res.code === 401) {
        store.dispatch('logout').then(() => {
          router.push('/login')
        })
      }
      // 403: 权限不足、角色错误、用户禁用等，不弹系统提示，由页面自己处理
      // 404: 资源不存在，不弹系统提示
      // 其他错误弹系统提示
      if (res.code !== 401 && res.code !== 403 && res.code !== 404) {
        Message({
          message: res.message || 'Error',
          type: 'error',
          duration: 5 * 1000
        })
      }

      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  error => {
    // 网络错误等，不弹系统提示
    return Promise.reject(error)
  }
)

export default service