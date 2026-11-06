<template>
  <div class="info-container">
    <h2 class="page-title">丽兹卡尔顿管理员信息管理</h2>

    <div class="info-list">

      <div class="info-card">
        <div class="card-label">管理员姓名</div>
        <div class="card-content">
          <span v-if="!isEditing('adminname')" class="card-value">{{ userInfo.adminname }}</span>
          <el-input v-else v-model="tempValue" size="small" class="edit-input" maxlength="20" />

          <div class="card-actions">
            <template v-if="!isEditing('adminname')">
              <el-button link type="primary" @click="startEdit('adminname', userInfo.adminname)">更改</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" @click="saveEdit('adminname')">确认</el-button>
              <el-button link type="info" @click="cancelEdit">取消</el-button>
            </template>
          </div>
        </div>
      </div>


      <div class="info-card">
        <div class="card-label">联系电话</div>
        <div class="card-content">
          <span v-if="!isEditing('telephone')" class="card-value">{{ userInfo.telephone }}</span>
          <el-input v-else v-model="tempValue" size="small" class="edit-input" maxlength="13" />

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


    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { accountId } from '@/store/auth'

//（管理员信息数据）
const userInfo = ref({
  id: '',
  adminname: '',
  telephone: ''
})

//（获取并填充管理员信息）
const fetchAdminInfo = async () => {
  if (!accountId.value) return
  try {
    const res = await request.get('/admin/info', {
      params: { id: accountId.value }
    })
    const data = res.data || {}
    userInfo.value = {
      id: data.id || '',
      adminname: data.adminname || '',
      telephone: data.telephone || ''
    }
  } catch (err) {
    console.warn('获取管理员信息失败：', err?.message || err)
  }
}

onMounted(fetchAdminInfo)



//（编辑状态与临时输入值）
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

//（保存修改并校验内容非空）
const saveEdit = (field) => {
  if (!tempValue.value.trim()) {
    ElMessage.warning('内容不能为空')
    return
  }
  userInfo.value[field] = tempValue.value
  ElMessage.success('修改成功')
  editingField.value = null
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