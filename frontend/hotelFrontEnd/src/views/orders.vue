<template>
  <div class="order-container">
    <h2 class="page-title">我的订单</h2>

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
              <span class="category-name">{{ order.categoryName }}</span>
              <span class="vip-badge" :class="`vip-level-${order.viplevel}`">
                {{ getVipLevelText(order.viplevel) }}
              </span>
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
            
            <div class="action-btns" v-if="order.status === 0">
              <el-button
                class="btn-confirm"
                size="small"
                round
                :loading="operatingId === order.orderId"
                @click="handleConfirm(order)"
              >
                确认
              </el-button>
              <el-button
                class="btn-cancel"
                size="small"
                round
                :loading="operatingId === order.orderId"
                @click="handleCancel(order)"
              >
                取消
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
import { accountId } from '@/store/auth'
import request from '@/api/request'

//（订单加载状态与订单列表）
const isLoading = ref(true)
const orderList = ref([])

//（订单状态映射字典）
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

//（会员等级映射字典）
const vipLevelMap = {
  1: '普通会员',
  2: '银卡会员',
  3: '金卡会员',
  4: '大师会员'
}

const getVipLevelText = (level) => {
  return vipLevelMap[level] || '未知等级'
}

//（格式化日期时间）
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

const operatingId = ref('')

//（修改订单状态并刷新列表）
const changeOrderStatus = async (order, status, successText) => {
  operatingId.value = order.orderId
  try {
    await request.post('/user/cancelOrder', { id: order.orderId, status })
    ElMessage.success(successText)
    await fetchOrders()
  } catch (err) {
    console.warn('修改订单状态失败：', err?.message || err)
  } finally {
    operatingId.value = ''
  }
}

//（确认待确认订单）
const handleConfirm = (order) => {
  ElMessageBox.confirm(
    `确认订单 ${order.orderId} 吗？`,
    '确认订单',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    changeOrderStatus(order, 1, '订单已确认')
  }).catch(() => {
  })
}

//（取消待确认订单）
const handleCancel = (order) => {
  ElMessageBox.confirm(
    `确认取消订单 ${order.orderId} 吗？`,
    '取消确认',
    {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      type: 'warning',
    }
  ).then(() => {
    changeOrderStatus(order, 4, '订单已取消')
  }).catch(() => {
  })
}

//（加载当前用户的历史订单）
const fetchOrders = async () => {
  if (!accountId.value) {
    isLoading.value = false
    return
  }

  isLoading.value = true
  try {
    const res = await request.get('/user/historyOrders', {
      params: { id: accountId.value }
    })
    orderList.value = res.data || []
  } catch (err) {
    console.warn('获取历史订单失败：', err?.message || err)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchOrders()
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

.category-name {
  font-size: 13px;
  font-weight: normal;
  color: #909399;
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

.action-btns .btn-confirm {
  --el-button-text-color: #fff;
  --el-button-bg-color: #f0a020;
  --el-button-border-color: #f0a020;
  --el-button-hover-text-color: #fff;
  --el-button-hover-bg-color: #f7b84b;
  --el-button-hover-border-color: #f7b84b;
  --el-button-active-text-color: #fff;
  --el-button-active-bg-color: #d98a10;
  --el-button-active-border-color: #d98a10;
}

.action-btns .btn-cancel {
  --el-button-text-color: #fff;
  --el-button-bg-color: #e74c3c;
  --el-button-border-color: #e74c3c;
  --el-button-hover-text-color: #fff;
  --el-button-hover-bg-color: #f0685a;
  --el-button-hover-border-color: #f0685a;
  --el-button-active-text-color: #fff;
  --el-button-active-bg-color: #cf3f2f;
  --el-button-active-border-color: #cf3f2f;
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
