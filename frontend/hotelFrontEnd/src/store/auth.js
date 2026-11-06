import { ref } from 'vue'
import request from '@/api/request'

//（登录态数据，持久化到 localStorage）
const savedUser = localStorage.getItem('userInfo')

export const accountId = ref(localStorage.getItem('accountId') || '')
export const isManage = ref(localStorage.getItem('isManage') === 'true')
export const userInfo = ref(savedUser ? JSON.parse(savedUser) : null)

//（用户登录）
export const login = (telephone, password) =>
  request.post('/user/login', { telephone, password })

//（管理员登录）
export const adminLogin = (telephone, password) =>
  request.post('/admin/login', { telephone, password })

//（用户注册，成功后自动登录）
export const register = (telephone, password, username) =>
  request.post('/user/register', { telephone, password, username })

//（写入登录态并同步到本地存储）
export const setAuth = (id, manage, info = null) => {
  accountId.value = id
  isManage.value = manage
  userInfo.value = info
  localStorage.setItem('accountId', id)
  localStorage.setItem('isManage', manage)
  if (info) {
    localStorage.setItem('userInfo', JSON.stringify(info))
  }
}

//（清除登录态与本地存储）
export const clearAuth = () => {
  accountId.value = ''
  isManage.value = false
  userInfo.value = null
  localStorage.removeItem('accountId')
  localStorage.removeItem('isManage')
  localStorage.removeItem('userInfo')
}
