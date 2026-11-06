<template>
  <div class="info-container">
    <h2 class="page-title">丽兹卡尔顿欢迎您的到来！</h2>

    <div class="info-list">

      <div class="info-card">
        <div class="card-label">用户姓名</div>
        <div class="card-content">
          <span v-if="!isEditing('username')" class="card-value">{{ userInfo.username }}</span>
          <el-input v-else v-model="tempValue" size="small" class="edit-input" maxlength="20" />

          <div class="card-actions">
            <template v-if="!isEditing('username')">
              <el-button link type="primary" @click="startEdit('username', userInfo.username)">更改</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" @click="saveEdit('username')">确认</el-button>
              <el-button link type="info" @click="cancelEdit">取消</el-button>
            </template>
          </div>
        </div>
      </div>

      <div class="info-card">
        <div class="card-label">VIP 等级</div>
        <div class="card-content">
          <span class="card-value">{{ vipLevelText }}</span>
          <div class="card-actions"></div>
        </div>
      </div>

      <div class="info-card">
        <div class="card-label">联系电话</div>
        <div class="card-content">
          <span v-if="!isEditing('telephone')" class="card-value">{{ userInfo.telephone }}</span>
          <el-input v-else v-model="tempValue" size="small" class="edit-input" maxlength="11" />

          <div class="card-actions">
            <template v-if="!isEditing('telephone')">
              <el-button link type="primary" @click="startEdit('telephone', userInfo.telephone)">更改</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" @click="saveEdit('telephone')">确认</el-button>
              <el-button link type="info" @click="cancelEdit">取消</el-button>
            </template>
          </div>
        </div>
      </div>

      <div class="info-card">
        <div class="card-label">当前积分</div>
        <div class="card-content">
          <span class="card-value">{{ userInfo.exp }}</span>
          <div class="card-actions"></div>
        </div>
      </div>

      <div class="info-card">
        <div class="card-label">登录密码</div>
        <div class="card-content">
          <span v-if="!isEditing('password')" class="card-value">{{ passwordText }}</span>
          <el-input
            v-else
            v-model="tempValue"
            size="small"
            class="edit-input"
            maxlength="20"
            placeholder="请输入新密码"
          />

          <div class="card-actions">
            <template v-if="!isEditing('password')">
              <el-button link type="primary" @click="startEdit('password', '')">更改</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" @click="saveEdit('password')">确认</el-button>
              <el-button link type="info" @click="cancelEdit">取消</el-button>
            </template>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { accountId } from '@/store/auth'
import request from '@/api/request'

//（当前登录用户信息）
const userInfo = ref({
  username: '',
  viplevel: 1,
  telephone: '',
  exp: 0
})

//（拉取用户信息）
const fetchUserInfo = async () => {
  if (!accountId.value) return
  try {
    const res = await request.get('/user/userInfo', {
      params: { id: accountId.value }
    })
    if (res.data) userInfo.value = { ...userInfo.value, ...res.data }
  } catch (err) {
    console.warn('获取用户信息失败：', err?.message || err)
  }
}

onMounted(fetchUserInfo)

//（密码统一显示星号）
const passwordText = '********'

//（根据等级计算显示文字）
const vipLevelText = computed(() => {
  const map = { 1: '普通会员', 2: '银卡会员', 3: '金卡会员', 4: '大师会员' }
  return map[userInfo.value.viplevel] || '未知等级'
})

//（字段编辑状态）
const editingField = ref(null)
const tempValue = ref('')

const isEditing = (field) => editingField.value === field

const startEdit = (field, currentValue) => {
  editingField.value = field
  tempValue.value = currentValue
}

const cancelEdit = () => {
  editingField.value = null
  tempValue.value = ''
}

//（校验字段格式）
const validateField = (field, value) => {
  const text = value.trim()
  if (!text) return ''
  if (field === 'username' && text.length > 20) return '用户名长度为 1 ~ 20 个字符'
  if (field === 'telephone' && !/^1[3-9]\d{9}$/.test(text)) {
    return '请输入正确的 11 位手机号'
  }
  if (field === 'password' && (text.length < 6 || text.length > 20)) {
    return '密码长度为 6 ~ 20 位'
  }
  return ''
}

//（保存修改并刷新信息）
const saveEdit = async (field) => {
  const value = tempValue.value.trim()

  if (!value) {
    ElMessage.info('未填写内容，已保留原值')
    cancelEdit()
    return
  }

  const error = validateField(field, value)
  if (error) {
    ElMessage.warning(error)
    return
  }

  const payload = {
    id: accountId.value,
    [field]: value
  }

  try {
    await request.post('/user/updateUserInfo', payload)
    ElMessage.success('修改成功')
    editingField.value = null
    await fetchUserInfo()
  } catch (err) {
    console.warn('修改用户信息失败：', err?.message || err)
  }
}
</script>

<style scoped>
.info-container {
  width: 100%;
  max-width: 700px;
}

.page-title {
  font-family: "Songti SC", "STSong", "SimSun", serif;
  color: #8b8b8b;
  font-size: 22px;
  letter-spacing: 2px;
  margin-bottom: 24px;
  font-weight: 500;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.info-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 10px;
}

.card-label {
  align-self: flex-start;
  width: 110px;
  padding: 8px 0;
  text-align: center;
  font-size: 13px;
  letter-spacing: 1px;
  color: #5d7691;
  border-radius: 999px;
  background: linear-gradient(135deg, #f6fbff 0%, #e8f2fc 100%);
  box-shadow:
    0 10px 22px rgba(6, 26, 43, 0.09),
    0 3px 7px rgba(6, 26, 43, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transition: transform 0.35s cubic-bezier(0.22, 0.61, 0.36, 1),
              box-shadow 0.35s ease;
}

.card-content {
  align-self: stretch;
  margin-left: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 11px 26px;
  border-radius: 999px;
  background: linear-gradient(135deg, #eef6fe 0%, #dde9f8 100%);
  box-shadow:
    0 18px 36px rgba(6, 26, 43, 0.12),
    0 6px 14px rgba(6, 26, 43, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.85);
  transition: transform 0.35s cubic-bezier(0.22, 0.61, 0.36, 1),
              box-shadow 0.35s ease;
}

.info-card:hover .card-label {
  transform: translateY(-3px);
  box-shadow:
    0 16px 30px rgba(6, 26, 43, 0.13),
    0 5px 10px rgba(6, 26, 43, 0.07),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.info-card:hover .card-content {
  transform: translateY(-3px);
  box-shadow:
    0 26px 48px rgba(6, 26, 43, 0.17),
    0 9px 18px rgba(6, 26, 43, 0.09),
    inset 0 1px 0 rgba(255, 255, 255, 0.85);
}

.card-value {
  font-size: 15px;
  font-weight: 600;
  color: #061a2b;
}

.edit-input {
  width: 200px;
}
:deep(.edit-input .el-input__wrapper) {
  border-radius: 8px;
  background-color: #ffffff;
  box-shadow: none;
  border: 1px solid #c4d1e0;
}
:deep(.edit-input .el-input__wrapper.is-focus) {
  border-color: #061a2b;
}

.card-actions {
  display: flex;
  align-items: center;
  min-width: 90px;
  justify-content: flex-end;
}

:deep(.el-button.is-link) {
  font-size: 13px;
  font-weight: 500;
}

@media (max-width: 600px) {
  .info-list {
    gap: 20px;
  }
  .card-label {
    width: 96px;
    padding: 7px 0;
    font-size: 12px;
  }
  .card-content {
    margin-left: 40px;
    padding: 12px 18px;
  }
}
</style>
