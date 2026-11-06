<template>
  <div class="order-container">
    <h2 class="page-title">订单管理</h2>

    <div class="filter-section">
      <div class="filter-card">
        <div class="filter-fields">
          <el-date-picker
            v-model="filterStayDate"
            type="date"
            placeholder="留宿日期"
            size="default"
            class="custom-date-picker"
            value-format="YYYY-MM-DD"
          />
          <span class="date-separator">留宿当日</span>

          <el-input
            v-model="filterRoomName"
            placeholder="房间名称"
            size="default"
            clearable
            class="custom-text-input"
          />

          <el-input
            v-model="filterPhone"
            placeholder="电话号"
            size="default"
            clearable
            class="custom-text-input"
          />

          <el-select
            v-model="filterStatus"
            placeholder="订单状态"
            size="default"
            clearable
            class="custom-select"
          >
            <el-option
              v-for="(info, status) in statusMap"
              :key="status"
              :label="info.text"
              :value="Number(status)"
            />
          </el-select>
        </div>

        <el-button type="primary" class="filter-btn" @click="handleFilter" :loading="isLoading">
          筛选
        </el-button>
      </div>
    </div>

    <div class="order-list" v-loading="isLoading">
      
      <div v-for="order in orderList" :key="order.orderId" class="order-card">
        
        <div class="card-header">
          <div class="order-no">
            <span class="label">订单号：</span>
            <span class="value">{{ order.orderId }}</span>
          </div>
          <el-tag :type="getStatusInfo(order.status).type" effect="light" round size="small">
            {{ getStatusInfo(order.status).text }}
          </el-tag>
        </div>

        <div class="card-body">
          <div class="body-left">
            <div class="room-info">
              <el-icon class="icon"><House /></el-icon>
              <span class="room-name">{{ order.roomName }} 号房</span>
              <span class="room-id">（{{ order.categoryName }}）</span>
              <span class="vip-badge" :class="`vip-level-${order.viplevel}`">
                {{ getVipLevelText(order.viplevel) }}
              </span>
            </div>

            <div class="phone-info" v-if="order.userName || order.userTelephone">
              <span class="phone-label">客人：</span>
              <span class="guest-name">{{ order.userName || '-' }}</span>
              <template v-if="order.userTelephone">
                <span class="info-divider">·</span>
                <span class="phone-label">电话：</span>
                <span class="phone-value">{{ order.userTelephone }}</span>
              </template>
            </div>
            
            <div class="time-info">
              <div class="time-block">
                <span class="time-label">入住</span>
                <span class="time-value">{{ formatDate(order.checkInTime) }}</span>
              </div>
              <div class="time-arrow">→</div>
              <div class="time-block">
                <span class="time-label">退房</span>
                <span class="time-value">{{ formatDate(order.checkOutTime) }}</span>
              </div>
            </div>
          </div>

          <div class="body-right">
            <div class="price-info">
              <span class="price-label">结算价</span>
              <span class="price-value"><small>¥</small>{{ order.totalPrice }}</span>
            </div>
            
            <div class="action-btns" v-if="[0, 1, 2].includes(order.status)">
              <el-button
                v-if="[0].includes(order.status)"
                type="primary"
                size="small"
                round
                @click="handleApprove(order)"
              >
                确认
              </el-button>
              <el-button 
                v-if="[1].includes(order.status)" 
                type="primary"
                size="small" 
                round
                @click="handleConfirm(order)"
              >
                入住
              </el-button>
              <el-button 
                v-if="[0, 1].includes(order.status)" 
                type="danger"
                size="small" 
                round
                @click="handleCancel(order)"
              >
                拒绝
              </el-button>
              <el-button 
                v-if="[2].includes(order.status)" 
                type="primary" 
                size="small" 
                round 
                @click="handleCheckout(order)"
              >
                退房
              </el-button>
            </div>
          </div>
        </div>

        <div class="card-extra">
          <div class="extra-grid">
            <div class="extra-item">
              <span class="extra-label">下单时间：</span>
              <span>{{ formatDate(order.orderTime) }}</span>
            </div>
            
            <div class="extra-item" v-if="order.actualStartTime">
              <span class="extra-label">实际入住：</span>
              <span class="highlight">{{ formatDate(order.actualStartTime) }}</span>
            </div>
            <div class="extra-item" v-if="order.actualEndTime">
              <span class="extra-label">实际退房：</span>
              <span class="highlight">{{ formatDate(order.actualEndTime) }}</span>
            </div>
            <div class="extra-item" v-if="order.cancelTime">
              <span class="extra-label">取消时间：</span>
              <span class="danger">{{ formatDate(order.cancelTime) }}</span>
            </div>
          </div>
          
          <div class="remark-box" v-if="order.remark">
            <span class="remark-label">备注：</span>
            <span class="remark-text">{{ order.remark }}</span>
          </div>
        </div>

      </div>

      <el-empty v-if="orderList.length === 0 && !isLoading" description="暂无订单记录" />

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { House } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import request from '@/api/request'

//（订单列表与筛选状态）
const isLoading = ref(true)
const orderList = ref([])

const filterStayDate = ref('')
const filterRoomName = ref('')
const filterPhone = ref('')
const filterStatus = ref(null)

//（订单状态映射）
const statusMap = {
  0: { text: '待确认', type: 'warning' },
  1: { text: '已确认未入住', type: 'primary' },
  2: { text: '已入住未退房', type: 'success' },
  3: { text: '已退房完成', type: 'info' },
  4: { text: '已取消', type: 'info' },
  5: { text: '已拒绝', type: 'danger' },
  6: { text: '已过期', type: 'info' }
}

const getStatusInfo = (status) => {
  return statusMap[status] || { text: '未知状态', type: 'info' }
}

//（会员等级映射）
const vipLevelMap = {
  1: '普通会员',
  2: '银卡会员',
  3: '金卡会员',
  4: '大师会员'
}

const getVipLevelText = (level) => {
  return vipLevelMap[level] || '未知等级'
}

//（时间格式化工具）
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

//（修改订单状态并刷新列表）
const updateOrderStatus = async (order, status, successText) => {
  try {
    await request.post('/admin/setorderstatus', { orderId: order.orderId, status })
    ElMessage.success(successText)
    await handleFilter(true)
  } catch (err) {
    console.warn('修改订单状态失败：', err?.message || err)
  }
}

const handleApprove = (order) => {
  ElMessageBox.confirm(
    `确认订单 ${order.orderId} 吗？`,
    '确认订单',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => updateOrderStatus(order, 1, '订单已确认')).catch(() => {})
}

const handleConfirm = (order) => {
  ElMessageBox.confirm(
    `确认为订单 ${order.orderId} 办理入住吗？`,
    '入住确认',
    {
      confirmButtonText: '确认入住',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => updateOrderStatus(order, 2, '已办理入住')).catch(() => {})
}

const handleCheckout = (order) => {
  ElMessageBox.confirm(
    `确认为订单 ${order.orderId} 办理退房吗？`,
    '退房确认',
    {
      confirmButtonText: '确认退房',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => updateOrderStatus(order, 3, '退房成功')).catch(() => {})
}

const handleCancel = (order) => {
  ElMessageBox.confirm(
    `确认拒绝订单 ${order.orderId} 吗？`,
    '拒绝确认',
    {
      confirmButtonText: '确认拒绝',
      cancelButtonText: '再想想',
      type: 'warning',
    }
  ).then(() => updateOrderStatus(order, 5, '订单已拒绝')).catch(() => {})
}

//（订单查询与筛选）
const handleFilter = async (silent = false) => {
  const phone = filterPhone.value ? filterPhone.value.trim() : ''
  if (phone && !/^\d{11}$/.test(phone)) {
    ElMessage.warning('请输入 11 位手机号')
    return
  }

  const params = {}
  if (filterStayDate.value) params.time = `${filterStayDate.value}T00:00:00`
  if (filterRoomName.value && filterRoomName.value.trim()) params.roomName = filterRoomName.value.trim()
  if (phone) params.telephone = phone
  if (filterStatus.value !== null && filterStatus.value !== '' && filterStatus.value !== undefined) {
    params.status = filterStatus.value
  }

  isLoading.value = true
  try {
    const res = await request.get('/admin/allOrders', { params })
    orderList.value = res.data || []
    if (!silent) {
      if (orderList.value.length > 0) {
        ElMessage.success(`共 ${orderList.value.length} 条订单`)
      } else {
        ElMessage.info('未找到符合条件的订单')
      }
    }
  } catch (err) {
    console.warn('查询订单失败：', err?.message || err)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  handleFilter(true)
})
</script>

<style scoped>
.order-container {
  width: 100%;
  max-width: 860px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  font-family: "Songti SC", "STSong", "SimSun", serif;
  color: #061a2b;
  font-size: 22px;
  letter-spacing: 2px;
  margin-bottom: 24px;
  font-weight: 500;
}

.filter-section {
  margin-bottom: 28px;
}

.filter-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 16px 24px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(200, 220, 240, 0.4);
  box-shadow:
    0 10px 30px rgba(6, 26, 43, 0.06),
    0 2px 8px rgba(6, 26, 43, 0.03);
}

.filter-fields {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.date-separator {
  color: #5d7691;
  font-size: 13px;
}

:deep(.custom-date-picker .el-input__wrapper) {
  border-radius: 999px;
  background-color: rgba(255, 255, 255, 0.7);
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.9);
  padding: 0 16px;
  transition: all 0.3s;
}
:deep(.custom-date-picker .el-input__wrapper.is-focus) {
  border-color: #061a2b;
  background-color: #ffffff;
  box-shadow: 0 0 0 1px #061a2b inset;
}

:deep(.custom-text-input .el-input__wrapper) {
  border-radius: 999px;
  background-color: rgba(255, 255, 255, 0.7);
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.9);
  padding: 0 16px;
  transition: all 0.3s;
}
:deep(.custom-text-input .el-input__wrapper.is-focus) {
  border-color: #061a2b;
  background-color: #ffffff;
  box-shadow: 0 0 0 1px #061a2b inset;
}
:deep(.custom-text-input) {
  width: 130px;
}

:deep(.custom-select .el-select__wrapper) {
  border-radius: 999px;
  background-color: rgba(255, 255, 255, 0.7);
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.9);
  min-height: 32px;
  padding: 0 12px;
  transition: all 0.3s;
}
:deep(.custom-select .el-select__wrapper.is-focused) {
  border-color: #061a2b;
  background-color: #ffffff;
  box-shadow: 0 0 0 1px #061a2b inset;
}
:deep(.custom-select) {
  width: 150px;
}

.filter-btn {
  border-radius: 999px;
  background-color: #061a2b;
  border-color: #061a2b;
  padding: 8px 32px;
  letter-spacing: 2px;
  box-shadow: 0 4px 12px rgba(6, 26, 43, 0.2);
  transition: all 0.3s ease;
  flex-shrink: 0;
}
.filter-btn:hover {
  background-color: #143b5c;
  border-color: #143b5c;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(6, 26, 43, 0.3);
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}

.order-card {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
}

.order-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px dashed #ebeef5;
}

.order-no {
  font-size: 13px;
  color: #5d7691;
}
.order-no .value {
  font-family: monospace;
  color: #303133;
  font-weight: 600;
}

.card-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.body-left {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.room-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.room-info .icon {
  color: #409eff;
}
.room-name {
  font-family: "Songti SC", "STSong", "SimSun", serif;
  letter-spacing: 1px;
}
.room-id {
  font-size: 13px;
  font-weight: normal;
  color: #909399;
}

.phone-info {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #606266;
}
.phone-label {
  color: #909399;
}
.guest-name {
  color: #303133;
  font-weight: 500;
}
.info-divider {
  color: #c0c4cc;
}
.phone-value {
  font-family: monospace;
  letter-spacing: 0.5px;
}

.vip-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: normal;
}
.vip-level-1 { background: #f4f4f5; color: #909399; }
.vip-level-2 { background: #e1f3d8; color: #67c23a; }
.vip-level-3 { background: #fdf6ec; color: #e6a23c; }
.vip-level-4 { background: #fef0f0; color: #f56c6c; font-weight: 600; }

.time-info {
  display: flex;
  align-items: center;
  gap: 16px;
}
.time-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.time-label {
  font-size: 12px;
  color: #909399;
}
.time-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.time-arrow {
  color: #c0c4cc;
  font-size: 16px;
}

.body-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.price-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}
.price-label {
  font-size: 12px;
  color: #909399;
}
.price-value {
  font-size: 22px;
  font-weight: 700;
  color: #f56c6c;
  font-family: "Din", "Helvetica Neue", Helvetica, Arial, sans-serif;
}
.price-value small {
  font-size: 14px;
  margin-right: 2px;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.card-extra {
  padding-top: 12px;
  border-top: 1px solid #f2f6fc;
  background-color: #fafbfc;
  border-radius: 8px;
  padding: 12px 16px;
  margin-top: 4px;
}

.extra-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
}

.extra-item {
  font-size: 13px;
  color: #606266;
}
.extra-label {
  color: #909399;
}
.extra-item .highlight {
  color: #409eff;
  font-weight: 500;
}
.extra-item .danger {
  color: #f56c6c;
}

.remark-box {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
  font-size: 13px;
}
.remark-label {
  color: #909399;
}
.remark-text {
  color: #606266;
}

@media (max-width: 600px) {
  .filter-card {
    flex-direction: column;
    border-radius: 20px;
    padding: 20px;
    align-items: stretch;
  }

  .filter-fields {
    flex-direction: column;
    align-items: stretch;
  }

  .date-separator {
    text-align: center;
    margin: -4px 0;
  }

  :deep(.custom-text-input),
  :deep(.custom-select) {
    width: 100%;
  }

  .order-card {
    padding: 16px;
  }
  
  .card-body {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .body-right {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    border-top: 1px dashed #ebeef5;
    padding-top: 12px;
  }
  
  .price-info {
    align-items: flex-start;
  }

  .extra-grid {
    flex-direction: column;
    gap: 8px;
  }
}
</style>

    