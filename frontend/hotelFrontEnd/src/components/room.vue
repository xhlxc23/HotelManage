<template>
  <div class="room-detail-container" v-loading="isLoading">
    <div class="bg-layer" aria-hidden="true"></div>

    <div v-if="roomData" class="room-detail-content">

      <div class="nav-header">
        <el-button link @click="goBack" class="back-btn">
          <el-icon>
            <ArrowLeft />
          </el-icon> 返回
        </el-button>
      </div>

      <div class="gallery-wrapper">
        <el-carousel trigger="click" class="custom-carousel" :autoplay="true" indicator-position="outside">
          <el-carousel-item v-for="(img, index) in roomData.pictures" :key="index">
            <img :src="img" alt="room-img" class="gallery-img" @error="onImgError" />
          </el-carousel-item>
        </el-carousel>
        <!-- VR 全景入口按钮 -->
        <el-button v-if="roomData.categoryId" class="vr-entry-btn" @click="goVr">
          <el-icon class="vr-entry-icon">
            <VideoCamera />
          </el-icon>
          VR 全景看房
        </el-button>
      </div>

      <div class="main-info-card">
        <div class="info-header">
          <div class="title-area">
            <h1 class="room-title">{{ roomData.categoryName }}</h1>
            <div class="room-tags">
              <el-tag effect="plain" round type="info" size="small">
                <el-icon>
                  <House />
                </el-icon> {{ roomData.roomName }} 号房
              </el-tag>
            </div>
          </div>
          <div class="price-area">
            <span v-if="roomData.basePrice && roomData.basePrice > roomData.priceAfterDiscount" class="basic-price">¥{{
              roomData.basePrice }}</span>
            <span class="price-symbol">¥</span>
            <span class="price-number">{{ roomData.priceAfterDiscount }}</span>
            <span class="price-unit">/晚</span>
          </div>
        </div>

        <div class="booking-bar">
          <div class="date-info">
            <el-icon class="date-icon">
              <Calendar />
            </el-icon>

            <el-date-picker v-model="currentStartDate" type="date" placeholder="入住日期" value-format="YYYY-MM-DD"
              class="custom-date-picker" :disabled-date="disabledStartDate" />

            <span class="date-arrow">→</span>

            <el-date-picker v-model="currentEndDate" type="date" placeholder="退房日期" value-format="YYYY-MM-DD"
              class="custom-date-picker" :disabled-date="disabledEndDate" />

            <span class="days-count" v-if="totalDays > 0">(共 {{ totalDays }} 晚)</span>
            <span class="days-count" v-else>(请选择日期)</span>
          </div>

          <div class="total-price">
            <span v-if="totalBasicPrice > totalPrice && totalPrice > 0" class="total-basic">原价 ¥{{ totalBasicPrice}}</span>
            (已优享会员折扣)总价：
            <span class="highlight">¥{{ totalPrice }}</span>
          </div>
        </div>

        <div class="service-section">
          <h3 class="section-title">增值服务</h3>
          <div class="service-list">
            <div class="service-item">
              <div class="service-info">
                <span class="service-name">泊车服务</span>
                <span class="service-desc">¥{{ PARKING_PRICE }} / 晚</span>
              </div>
              <el-switch v-model="parking" />
            </div>
            <div class="service-item">
              <div class="service-info">
                <span class="service-name">加床服务</span>
                <span class="service-desc">¥{{ EXTRA_BED_PRICE }} / 晚</span>
              </div>
              <el-switch v-model="extraBed" />
            </div>
          </div>
          <div v-if="extrasTotal > 0" class="service-summary">
            增值服务合计：<span class="service-highlight">¥{{ extrasTotal }}</span>
            <span class="service-note">（已计入总价）</span>
          </div>
        </div>

        <div class="description-section">
          <h3 class="section-title">房间详情</h3>
          <p class="description-text">{{ roomData.description }}</p>
        </div>

        <div class="remark-section">
          <h3 class="section-title">备注信息</h3>
          <el-input v-model="remark" type="textarea" :rows="3" maxlength="200" show-word-limit resize="none"
            class="remark-input" placeholder="如有特殊需求（如无烟房、加床、预计到店时间等），请在此填写" />
        </div>

        <div class="action-footer">
          <el-button type="primary" class="book-btn" :loading="submitting" @click="handleBook">
            确认预订
          </el-button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, House, Calendar, VideoCamera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { accountId, isManage } from '@/store/auth'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()

//（清理路由参数，去掉首尾引号）
const cleanParam = (v) => {
  if (v == null) return ''
  return String(v).replace(/^['"]+|['"]+$/g, '')
}

//（页面状态与路由参数）
const roomId = cleanParam(route.params.id)
const isLoading = ref(true)
const roomData = ref(null)
const currentStartDate = ref(cleanParam(route.params.startDate))
const currentEndDate = ref(cleanParam(route.params.endDate))

const remark = ref('')

const PARKING_PRICE = 50
const EXTRA_BED_PRICE = 200
const parking = ref(false)
const extraBed = ref(false)

//（增值服务关键词与备注自动同步）
const SERVICE_KEYWORDS = ['需要泊车服务', '需要加床服务']

const syncServiceToRemark = () => {
  let text = remark.value || ''
  SERVICE_KEYWORDS.forEach((k) => {
    text = text.replace(new RegExp(`；?${k}`, 'g'), '')
  })
  text = text.trim().replace(/^；+|；+$/g, '')

  const active = []
  if (parking.value) active.push(SERVICE_KEYWORDS[0])
  if (extraBed.value) active.push(SERVICE_KEYWORDS[1])
  remark.value = [text, ...active].filter(Boolean).join('；')
}

watch(parking, syncServiceToRemark)
watch(extraBed, syncServiceToRemark)

const onImgError = (e) => {
  e.target.src = '/ex/1.png'
}

//（按日期计算入住晚数）
const totalDays = computed(() => {
  if (!currentStartDate.value || !currentEndDate.value) return 0
  const start = new Date(`${currentStartDate.value}T00:00:00`).getTime()
  const end = new Date(`${currentEndDate.value}T00:00:00`).getTime()
  const diff = Math.round((end - start) / 86400000)
  return diff > 0 ? diff : 0
})

//（增值服务费用合计）
const extrasTotal = computed(() => {
  if (totalDays.value === 0) return 0
  let sum = 0
  if (parking.value) sum += PARKING_PRICE * totalDays.value
  if (extraBed.value) sum += EXTRA_BED_PRICE * totalDays.value
  return sum
})

//（订单总价与原价合计）
const totalPrice = computed(() => {
  if (!roomData.value || totalDays.value === 0) return 0
  return roomData.value.priceAfterDiscount * totalDays.value + extrasTotal.value
})

const totalBasicPrice = computed(() => {
  if (!roomData.value || totalDays.value === 0) return 0
  return (roomData.value.basePrice || roomData.value.priceAfterDiscount) * totalDays.value
})

const disabledStartDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const disabledEndDate = (time) => {
  if (!currentStartDate.value) {
    return time.getTime() < Date.now() - 8.64e7
  }
  return time.getTime() <= new Date(`${currentStartDate.value}T00:00:00`).getTime()
}

//（加载房间详情数据）
const fetchRoomDetail = async () => {
  if (!accountId.value) {
    isLoading.value = false
    ElMessage.warning('请先登录')
    return
  }

  // 管理员查询时，userId 传 000000（与下单逻辑保持一致）
  const requestUserId = isManage.value ? '000000' : accountId.value

  isLoading.value = true
  try {
    console.log('[room] 请求房间详情，isManage =', isManage.value, '，传入 userId =', requestUserId, '，roomId =', roomId)
    const res = await request.get('/user/oneRoomDetail', {
      params: { userId: requestUserId, roomId }
    })
    roomData.value = res.data || null
    if (roomData.value) {
      roomData.value.pictures = Array.isArray(roomData.value.pictures) && roomData.value.pictures.length
        ? roomData.value.pictures
        : ['/ex/1.png']
    }
  } catch (err) {
    console.warn('获取房间详情失败：', err?.message || err)
  } finally {
    isLoading.value = false
  }
}

const goBack = () => {
  router.back()
}

// 跳转至 VR 全景页面
const goVr = () => {
  if (roomData.value?.categoryId) {
    router.push(`/vr/${roomData.value.categoryId}`)
  } else {
    ElMessage.warning('该房型暂未开通 VR 全景')
  }
}

const round2 = (n) => Math.round(Number(n) * 100) / 100

const formatNow = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

const submitting = ref(false)

//（提交预订订单）
const handleBook = async () => {
  if (!accountId.value) {
    ElMessage.warning('请先登录')
    return
  }
  if (!roomData.value) {
    ElMessage.warning('房间信息加载中，请稍后再试')
    return
  }
  if (!currentStartDate.value || !currentEndDate.value) {
    ElMessage.warning('请选择完整的入住和退房日期')
    return
  }

  const remarkText = remark.value.trim()

  const orderUserId = isManage.value ? '000000' : accountId.value

  submitting.value = true
  try {
    const res = await request.post('/user/userSetOrder', {
      user_accountId: orderUserId,
      roomId,
      orderTime: formatNow(),
      startTime: `${currentStartDate.value}T14:00:00`,
      endTime: `${currentEndDate.value}T12:00:00`,
      price: round2(totalPrice.value),
      remark: remarkText || null
    })

    const order = res.data || {}
    const paid = order.totalPrice != null ? order.totalPrice : round2(totalPrice.value)
    ElMessage.success(`预订成功！订单号：${order.orderId}，共 ${totalDays.value} 晚，应付 ¥${paid}`)
    router.push('/client/orders')
  } catch (err) {
    console.warn('下单失败：', err?.message || err)
  } finally {
    submitting.value = false
  }
}

onMounted(fetchRoomDetail)
</script>

<style scoped>
.room-detail-container {
  position: relative;
  isolation: isolate;
  width: 100%;
  min-height: 100vh;
  box-sizing: border-box;
}

.bg-layer {
  position: fixed;
  inset: 0;
  z-index: 0;
  background-image: url('/back.png');
  background-size: cover;
  background-position: center top;
  background-repeat: no-repeat;
  pointer-events: none;
}

.room-detail-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

.nav-header {
  margin-bottom: 16px;
}

.back-btn {
  font-size: 14px;
  color: #5d7691;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  padding: 6px 12px;
  border-radius: 999px;
}

.back-btn:hover {
  color: #061a2b;
  background: rgba(255, 255, 255, 0.8);
}

.gallery-wrapper {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 16px 40px rgba(6, 26, 43, 0.1);
  margin-bottom: 24px;
  background: #fff;
}

.custom-carousel {
  --carousel-height: 400px;
  border-radius: 24px;
}

:deep(.custom-carousel .el-carousel__container) {
  height: var(--carousel-height) !important;
}

:deep(.custom-carousel .el-carousel__item) {
  height: 100%;
  overflow: hidden;
}

.gallery-img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* VR 全景入口按钮 */
.vr-entry-btn {
  position: absolute;
  right: 20px;
  bottom: 20px;
  z-index: 5;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  background: rgba(6, 26, 43, 0.75);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  box-shadow: 0 8px 24px rgba(6, 26, 43, 0.35);
  transition: all 0.3s ease;
}

.vr-entry-btn:hover {
  color: #fff;
  background: rgba(20, 59, 92, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(6, 26, 43, 0.45);
}

.vr-entry-icon {
  font-size: 18px;
}

.gallery-wrapper {
  position: relative;
}

.main-info-card {
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(24px) saturate(160%);
  -webkit-backdrop-filter: blur(24px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(6, 26, 43, 0.12);
}

@supports not ((backdrop-filter: blur(1px)) or (-webkit-backdrop-filter: blur(1px))) {
  .main-info-card {
    background: rgba(255, 255, 255, 0.92);
  }

  .booking-bar {
    background: #f2f8ff;
  }
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.room-title {
  font-family: "Songti SC", "STSong", "SimSun", serif;
  font-size: 28px;
  color: #061a2b;
  margin: 0 0 12px 0;
  font-weight: 600;
}

.room-tags {
  display: flex;
  gap: 8px;
}

.room-tags .el-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: rgba(244, 247, 250, 0.8);
  border-color: rgba(225, 232, 240, 0.9);
  color: #5d7691;
}

.price-area {
  color: #061a2b;
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 0 2px;
}

.basic-price {
  font-size: 15px;
  color: #a8b6c4;
  text-decoration: line-through;
  margin-right: 8px;
  font-family: "Din", sans-serif;
}

.price-symbol {
  font-size: 18px;
  font-weight: 600;
  margin-right: 4px;
}

.price-number {
  font-size: 36px;
  font-weight: 700;
  font-family: "Din", sans-serif;
  line-height: 1;
}

.price-unit {
  font-size: 14px;
  color: #909399;
  margin-left: 4px;
}

.booking-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, rgba(246, 251, 255, 0.7) 0%, rgba(232, 242, 252, 0.6) 100%);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 16px;
  padding: 12px 24px;
  margin-bottom: 32px;
  border: 1px solid rgba(220, 232, 245, 0.9);
}

.date-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #303133;
}

.date-icon {
  color: #409eff;
  font-size: 18px;
  margin-right: 4px;
}

.date-arrow {
  color: #c0c4cc;
  font-weight: bold;
}

.days-count {
  font-size: 13px;
  color: #909399;
  margin-left: 4px;
}

.total-price {
  font-size: 15px;
  color: #606266;
  white-space: nowrap;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-basic {
  font-size: 13px;
  color: #a8b6c4;
  text-decoration: line-through;
  font-family: "Din", sans-serif;
}

.total-price .highlight {
  font-size: 22px;
  font-weight: 700;
  color: #f56c6c;
  font-family: "Din", sans-serif;
}

:deep(.custom-date-picker.el-date-editor) {
  width: 135px;
  --el-input-bg-color: transparent;
  --el-input-border-color: transparent;
  --el-input-hover-border-color: transparent;
  --el-input-focus-border-color: transparent;
}

:deep(.custom-date-picker .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.5);
  box-shadow: none;
  border-radius: 8px;
  padding: 0 8px;
  transition: all 0.3s;
}

:deep(.custom-date-picker .el-input__wrapper:hover) {
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

:deep(.custom-date-picker .el-input__wrapper.is-focus) {
  background-color: #ffffff;
  box-shadow: 0 0 0 1px #409eff inset;
}

:deep(.custom-date-picker .el-input__inner) {
  font-weight: 600;
  color: #061a2b;
  text-align: center;
}

.service-section {
  margin-bottom: 32px;
}

.service-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.service-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(220, 232, 245, 0.9);
  transition: background 0.25s ease, border-color 0.25s ease;
}

.service-item:hover {
  background: rgba(255, 255, 255, 0.75);
  border-color: rgba(64, 158, 255, 0.4);
}

.service-info {
  display: flex;
  align-items: baseline;
  gap: 10px;
  min-width: 0;
}

.service-name {
  font-size: 15px;
  font-weight: 600;
  color: #061a2b;
}

.service-desc {
  font-size: 13px;
  color: #909399;
}

.service-summary {
  margin-top: 12px;
  font-size: 13px;
  color: #606266;
  text-align: right;
}

.service-highlight {
  font-weight: 700;
  color: #f56c6c;
  font-family: "Din", sans-serif;
}

.service-note {
  color: #a8b6c4;
}

:deep(.service-item .el-switch.is-checked .el-switch__core) {
  background-color: #061a2b;
  border-color: #061a2b;
}

.description-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 18px;
  color: #061a2b;
  margin-bottom: 12px;
  font-weight: 600;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 4px;
  background-color: #409eff;
  border-radius: 2px;
}

.description-text {
  font-size: 15px;
  color: #5d7691;
  line-height: 1.8;
  margin: 0;
}

.remark-section {
  margin-bottom: 32px;
}

.remark-input {
  width: 100%;
}

:deep(.remark-input .el-textarea__inner) {
  background-color: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(6, 26, 43, 0.08);
  border-radius: 12px;
  box-shadow: none;
  padding: 12px 14px 24px;
  font-size: 14px;
  line-height: 1.7;
  color: #061a2b;
  transition: all 0.3s;
}

:deep(.remark-input .el-textarea__inner::placeholder) {
  color: #a8b6c4;
}

:deep(.remark-input .el-textarea__inner:hover) {
  background-color: rgba(255, 255, 255, 0.7);
}

:deep(.remark-input .el-textarea__inner:focus) {
  background-color: #ffffff;
  border-color: #409eff;
  box-shadow: 0 0 0 1px #409eff inset;
}

:deep(.remark-input .el-input__count),
:deep(.remark-input .el-textarea .el-input__count) {
  background: transparent;
  color: #909399;
  right: 12px;
  bottom: 6px;
}

.action-footer {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid rgba(6, 26, 43, 0.08);
  padding-top: 24px;
}

.book-btn {
  padding: 24px 48px;
  border-radius: 999px;
  font-size: 18px;
  font-weight: 600;
  background-color: #061a2b;
  border-color: #061a2b;
  letter-spacing: 2px;
  box-shadow: 0 8px 20px rgba(6, 26, 43, 0.2);
  transition: all 0.3s ease;
}

.book-btn:hover {
  background-color: #143b5c;
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(6, 26, 43, 0.3);
}

@media (max-width: 768px) {
  .room-detail-content {
    padding: 12px;
  }

  .main-info-card {
    padding: 20px;
  }

  .info-header {
    flex-direction: column;
    gap: 16px;
  }

  .price-area {
    justify-content: flex-start;
  }

  .booking-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
    padding: 16px;
  }

  .date-info {
    flex-wrap: wrap;
    justify-content: center;
  }

  .total-price {
    justify-content: center;
  }

  .book-btn {
    width: 100%;
    padding: 18px 0;
    font-size: 16px;
  }

  .gallery-wrapper {
    border-radius: 16px;
  }

  .custom-carousel {
    --carousel-height: 250px;
  }
}
</style>
