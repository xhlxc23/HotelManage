import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth } from '@/store/auth'

//（统一请求实例，/api 由 vite 代理转发到本地后端）
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})
//（响应拦截：业务成功直接返回，业务失败统一提示）
request.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res && String(res.code) === '200') {
      return res
    }

    const msg = res?.msg || '请求失败'
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  },
  //（请求异常处理，401 时清除登录态并跳转登录页）
  (error) => {
    const status = error?.response?.status

    if (status === 401) {
      clearAuth()
      ElMessage.error('登录已失效，请重新登录')
      import('@/router').then(({ default: router }) => router.push('/logIn'))
    } else {
      const msg = error?.response?.data?.msg || error?.message || '网络异常'
      ElMessage.error(msg)
    }

    return Promise.reject(error)
  }
)

export default request
