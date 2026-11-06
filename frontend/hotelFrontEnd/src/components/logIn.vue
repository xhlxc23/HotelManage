<template>
  <div class="login-container">
    <div class="login-left">
      <div class="left-mask"></div>
      <div class="left-slogan">
        <h1>THE RITZ-CARLTON</h1>
        <p>WE ARE LADIES AND GENTLEMEN SERVING LADIES AND GENTLEMEN.</p>
      </div>
    </div>

    <div class="login-right">
      <div class="login-form-wrapper">
        <h2 class="login-title">欢迎登录</h2>
        <p class="login-subtitle">丽思卡尔顿酒店欢迎您！</p>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          label-position="top"
          size="large"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.telephone"
              placeholder="请输入手机号"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <div class="login-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码?</el-link>
          </div>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-entry">
          <span>还没有账号？</span>
          <el-link type="primary" :underline="false" @click="router.push('/register')">
            立即注册
          </el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { setAuth, login } from '@/store/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()

//（登录表单、记住我与提交状态）
const loginFormRef = ref(null)
const loginForm = reactive({ telephone: '', password: '' })
const rememberMe = ref(false)
const loading = ref(false)

//（手机号与密码的校验规则）
const rules = {
  telephone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的 11 位手机号',
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6 ~ 20 位', trigger: 'blur' }
  ]
}

//（提交登录并在成功后写入登录态）
const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(loginForm.telephone.trim(), loginForm.password)

      const data = res.data || {}
      const user = data.userTnfo || {}
      setAuth(user.id, data.roleType === 2, user)
      ElMessage.success('登录成功')
      router.push('/')
    } catch (err) {
      console.warn('登录失败：', err?.message || err)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: #f5f6f8;
}

.login-left {
  position: relative;
  flex: 6;
  background-image: url('/back.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  display: flex;
  align-items: flex-end;
  padding: 60px;
}

.left-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.1) 0%,
    rgba(0, 0, 0, 0.5) 100%
  );
  z-index: 1;
}

.left-slogan {
  position: relative;
  z-index: 2;
  color: #fff;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.4);
}

.left-slogan h1 {
  margin: 0 0 12px;
  font-size: 36px;
  font-weight: 400;
  letter-spacing: 4px;
  font-family: Georgia, serif;
}

.left-slogan p {
  margin: 0;
  font-size: 12px;
  letter-spacing: 2px;
  opacity: 0.8;
  max-width: 500px;
  line-height: 1.6;
}

.login-right {
  flex: 4;
  background: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 40px 120px;
  border-radius: 24px 0 0 24px;
  box-shadow: -12px 0 32px rgba(0, 0, 0, 0.08);
  z-index: 2;
}

.login-form-wrapper {
  width: 100%;
  max-width: 340px;
}

.login-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 500;
  color: #061a2b;
  letter-spacing: 2px;
  text-align: center;
  font-family: "Songti SC", "STSong", "SimSun", serif;
}

.login-subtitle {
  margin: 0 0 40px;
  font-size: 13px;
  color: #888;
  letter-spacing: 1px;
  text-align: center;
}

.login-form :deep(.el-input__wrapper) {
  background-color: #f7f8fa;
  box-shadow: none;
  border-radius: 10px;
  padding: 4px 16px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #061a2b;
  box-shadow: 0 0 0 3px rgba(6, 26, 43, 0.1);
}

.login-form :deep(.el-input__inner) {
  font-size: 15px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  margin-top: -10px;
}

.login-options :deep(.el-checkbox__label) {
  font-size: 13px;
  color: #666;
}

.login-options :deep(.el-link) {
  font-size: 13px;
  color: #666;
}

.login-options :deep(.el-link:hover) {
  color: #061a2b;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  letter-spacing: 6px;
  border-radius: 10px;
  background-color: #061a2b;
  border-color: #061a2b;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.login-btn:hover {
  background-color: #0d2d48;
  border-color: #0d2d48;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(6, 26, 43, 0.25);
}

.login-btn:active {
  transform: translateY(0);
}

.register-entry {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  margin-top: 20px;
  font-size: 13px;
  letter-spacing: 1px;
  color: #888;
}

.register-entry :deep(.el-link) {
  font-size: 13px;
  font-weight: 500;
  color: #061a2b;
}

.register-entry :deep(.el-link:hover) {
  color: #0d2d48;
}

@media (max-width: 900px) {
  .login-container {
    flex-direction: column;
  }
  .login-left {
    display: none;
  }
  .login-right {
    flex: 1;
    width: 100%;
    border-radius: 0;
    box-shadow: none;
    padding: 24px 24px 80px;
  }
  .login-title {
    font-size: 24px;
  }
}
</style>
