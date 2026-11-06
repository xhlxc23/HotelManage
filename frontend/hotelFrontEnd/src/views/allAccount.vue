<template>
  <div class="account-container">
    <h2 class="page-title">账号管理</h2>

    <div class="search-card">
      <el-input
        v-model="searchForm.name"
        placeholder="账号姓名"
        clearable
        class="glass-input"
      />
      <el-input
        v-model="searchForm.telephone"
        placeholder="联系电话"
        clearable
        class="glass-input"
      />
      <el-button
        type="primary"
        class="primary-btn"
        :loading="loading"
        @click="handleSearch"
      >
        搜索
      </el-button>
    </div>

    <div v-loading="loading" class="account-list">
      <div
        v-for="account in accountList"
        :key="account.id"
        class="account-card"
      >
        <div class="account-head">
          <el-icon class="head-icon"><User /></el-icon>
          <span class="account-id">ID：{{ account.id }}</span>
        </div>

        <div
          v-for="field in ACCOUNT_FIELDS"
          :key="field.key"
          class="field-row"
        >
          <span class="field-label">{{ field.label }}</span>

          <span v-if="!isEditing(account, field.key)" class="field-value">
            {{ field.key === 'password' ? '••••••' : account[field.key] }}
          </span>
          <el-input
            v-else
            v-model="tempValue"
            size="small"
            :maxlength="field.maxlength"
            :placeholder="field.key === 'password' ? '请输入新密码' : ''"
            class="field-input"
          />

          <div class="field-actions">
            <template v-if="!isEditing(account, field.key)">
              <el-button
                class="edit-btn"
                size="small"
                round
                @click="startEdit(account, field.key)"
              >
                更改
              </el-button>
            </template>
            <template v-else>
              <el-button
                type="primary"
                class="save-btn"
                size="small"
                round
                :loading="submitting"
                @click="handleConfirmChange(account, field.key)"
              >
                确认
              </el-button>
              <el-button
                class="cancel-btn"
                size="small"
                round
                @click="cancelEdit"
              >
                取消
              </el-button>
            </template>
          </div>
        </div>
      </div>

      <el-empty
        v-if="!loading && !accountList.length"
        :description="
          hasSearched ? '没有找到匹配的账号' : '请输入账号姓名或联系电话进行搜索'
        "
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import request from '@/api/request'

//（可编辑字段列表，id 不可更改）
const ACCOUNT_FIELDS = [
  { key: 'name', label: '姓名', apiKey: 'username', maxlength: 20 },
  { key: 'telephone', label: '电话', apiKey: 'telephone', maxlength: 11 },
  { key: 'password', label: '密码', apiKey: 'password', maxlength: 20 }
]

//（页面状态与搜索表单数据）
const loading = ref(false)
const hasSearched = ref(false)
const accountList = ref([])

const searchForm = reactive({
  name: '',
  telephone: ''
})

//（接口返回数据映射为页面账号结构）
const toAccount = (item) => {
  const user = item?.userTnfo || {}
  return {
    id: user.id,
    name: user.username,
    telephone: user.telephone
  }
}

//（搜索账号：按姓名或联系电话精确查询）
const handleSearch = async () => {
  const userName = searchForm.name.trim()
  const telephone = searchForm.telephone.trim()

  if (!userName && !telephone) {
    ElMessage.warning('请至少填写账号姓名或联系电话')
    return
  }

  loading.value = true
  try {
    const res = await request.post('/admin/searchusers', {
      ...(telephone ? { telephone } : { userName })
    })
    accountList.value = (res.data || []).map(toAccount)
    hasSearched.value = true
    ElMessage.success(`搜索完成，共 ${accountList.value.length} 条`)
  } catch (err) {
    accountList.value = []
    hasSearched.value = true
    console.warn('搜索账号失败：', err?.message || err)
  } finally {
    loading.value = false
  }
}

//（编辑状态：同一时间只能修改一个字段）
const editingKey = ref(null)
const tempValue = ref('')
const submitting = ref(false)

const keyOf = (account, field) => `${account.id}:${field}`
const isEditing = (account, field) => editingKey.value === keyOf(account, field)
const apiKeyOf = (field) =>
  ACCOUNT_FIELDS.find((f) => f.key === field)?.apiKey || field

const startEdit = (account, field) => {
  editingKey.value = keyOf(account, field)
  tempValue.value = field === 'password' ? '' : account[field]
}

const cancelEdit = () => {
  editingKey.value = null
  tempValue.value = ''
}

//（确认修改：只提交当前修改的字段）
const handleConfirmChange = async (account, field) => {
  const value = tempValue.value.trim()
  if (!value) {
    ElMessage.warning('内容不能为空')
    return
  }

  submitting.value = true
  try {
    const res = await request.post('/user/updateUserInfo', {
      id: account.id,
      [apiKeyOf(field)]: value
    })

    const user = res.data?.userTnfo || {}
    if (user.username != null) account.name = user.username
    if (user.telephone != null) account.telephone = user.telephone

    ElMessage.success('修改成功')
    cancelEdit()
  } catch (err) {
    console.warn('修改账号信息失败：', err?.message || err)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.account-container {
  width: 100%;
  max-width: 760px;
}

.page-title {
  font-family: "Songti SC", "STSong", "SimSun", serif;
  font-size: 22px;
  font-weight: 600;
  color: #061a2b;
  letter-spacing: 2px;
  margin: 0 0 22px 0;
}

.search-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  margin-bottom: 22px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(16px) saturate(160%);
  -webkit-backdrop-filter: blur(16px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 10px 30px rgba(6, 26, 43, 0.06);
}

.glass-input {
  flex: 1;
  min-width: 0;
}

:deep(.glass-input .el-input__wrapper) {
  border-radius: 999px;
  background-color: rgba(255, 255, 255, 0.7);
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.9);
  padding: 0 16px;
  transition: all 0.3s ease;
}
:deep(.glass-input .el-input__wrapper.is-focus) {
  border-color: #061a2b;
  background-color: #ffffff;
  box-shadow: 0 0 0 1px #061a2b inset;
}

.primary-btn {
  flex-shrink: 0;
  border-radius: 999px;
  background-color: #061a2b;
  border-color: #061a2b;
  padding: 8px 28px;
  letter-spacing: 2px;
  box-shadow: 0 4px 12px rgba(6, 26, 43, 0.2);
  transition: all 0.3s ease;
}
.primary-btn:hover {
  background-color: #143b5c;
  border-color: #143b5c;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(6, 26, 43, 0.3);
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 80px;
}

.account-card {
  padding: 16px 20px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.62);
  backdrop-filter: blur(20px) saturate(160%);
  -webkit-backdrop-filter: blur(20px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 10px 30px rgba(6, 26, 43, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.account-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 36px rgba(6, 26, 43, 0.14);
}

.account-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 10px;
  margin-bottom: 6px;
  border-bottom: 1px solid rgba(6, 26, 43, 0.08);
}

.head-icon {
  color: #061a2b;
}

.account-id {
  font-family: "Din", monospace;
  font-size: 12px;
  letter-spacing: 0.5px;
  color: #909399;
}

.field-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 9px 0;
  border-bottom: 1px dashed rgba(6, 26, 43, 0.07);
}
.field-row:last-child {
  border-bottom: none;
}

.field-label {
  width: 56px;
  flex-shrink: 0;
  font-size: 13px;
  letter-spacing: 1px;
  color: #5d7691;
}

.field-value {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  font-weight: 600;
  color: #061a2b;
  word-break: break-all;
}

.field-input {
  flex: 1;
  max-width: 240px;
}
:deep(.field-input .el-input__wrapper) {
  border-radius: 8px;
  background-color: #ffffff;
  box-shadow: none;
  border: 1px solid #c4d1e0;
}
:deep(.field-input .el-input__wrapper.is-focus) {
  border-color: #061a2b;
}

.field-actions {
  flex-shrink: 0;
  display: flex;
  gap: 6px;
  justify-content: flex-end;
  min-width: 66px;
}

.edit-btn,
.cancel-btn {
  border-radius: 999px;
  color: #5d7691;
  border-color: rgba(6, 26, 43, 0.15);
  background: rgba(255, 255, 255, 0.7);
  transition: all 0.25s ease;
}
.edit-btn:hover,
.cancel-btn:hover {
  color: #061a2b;
  border-color: #061a2b;
  background: #ffffff;
}

.save-btn {
  border-radius: 999px;
  background-color: #061a2b;
  border-color: #061a2b;
  transition: all 0.25s ease;
}
.save-btn:hover {
  background-color: #143b5c;
  border-color: #143b5c;
}

@media (max-width: 600px) {
  .search-card {
    flex-direction: column;
    align-items: stretch;
    border-radius: 20px;
    padding: 16px;
  }

  .glass-input,
  .primary-btn {
    width: 100%;
  }

  .account-card {
    padding: 14px 16px;
  }

  .field-label {
    width: 48px;
  }

  .field-input {
    max-width: none;
  }
}
</style>
