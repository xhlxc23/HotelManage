<template>
  <div class="info-container">
    <h2 class="page-title">房间查询</h2>

    <div class="filter-section">
      <div class="filter-card">
        <div class="filter-fields">
          <el-date-picker
            v-model="checkInDate"
            type="date"
            placeholder="入住日期"
            size="default"
            class="custom-date-picker"
            value-format="YYYY-MM-DD"
          />
          <span class="date-separator">至</span>
          <el-date-picker
            v-model="checkOutDate"
            type="date"
            placeholder="退房日期"
            size="default"
            class="custom-date-picker"
            value-format="YYYY-MM-DD"
          />
        </div>
        
        <el-button type="primary" class="filter-btn" @click="handleFilter" :loading="isLoading">
          筛选
        </el-button>
      </div>
    </div>

    <div class="info-list" v-loading="isLoading">
      
      <div v-for="room in cardList" :key="room.id" class="info-card">
        <div class="card-label">{{ room.categoryName }}</div>

        <div class="card-content room-content">
          <div class="room-img-wrapper">
            <img :src="room.picture" alt="room" class="room-img" />
          </div>

          <div class="room-details">
            <ul class="room-lines">
              <li>{{ room.roomName }} 号房</li>
              <li>{{ room.description }}</li>
            </ul>

            <div class="room-bottom">
              <div class="price-box">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ room.priceAfterDiscount }}</span>
                <span class="price-unit">/晚</span>
                <span
                  v-if="Number(room.oneNightPrice) !== Number(room.priceAfterDiscount)"
                  class="price-origin"
                >原价 ¥{{ room.oneNightPrice }}</span>
              </div>

              <div class="card-actions">
                <el-button link type="primary" class="book-btn" @click="handleClick2(room)">立即预订</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="cardList.length === 0 && !isLoading" description="暂无符合条件的房间" />

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { accountId } from '@/store/auth'
import request from '@/api/request'

const router = useRouter()

//（筛选条件与房间列表状态）
const checkInDate = ref('')
const checkOutDate = ref('')
const isLoading = ref(false)
const cardList = ref([])

//（根据日期跳转到预订页）
const handleClick2 = (room) => {
  if (checkInDate.value && checkOutDate.value) {
    router.push(`/room/${room.id}/${checkInDate.value}/${checkOutDate.value}`)
  } else {
    router.push(`/room/${room.id}`)
  }
}

//（校验登录与日期并查询可用房间）
const handleFilter = async () => {
  if (!checkInDate.value || !checkOutDate.value) {
    ElMessage.warning('请选择完整的入住和退房日期')
    return
  }
  if (new Date(checkInDate.value) >= new Date(checkOutDate.value)) {
    ElMessage.warning('退房日期必须晚于入住日期')
    return
  }
  if (!accountId.value) {
    ElMessage.warning('请先登录')
    return
  }

  isLoading.value = true
  try {
    const res = await request.post('/user/availableRooms', {
      userId: accountId.value,
      startTime: `${checkInDate.value}T14:00:00`,
      endTime: `${checkOutDate.value}T12:00:00`
    })
    cardList.value = res.data || []

    if (cardList.value.length > 0) {
      ElMessage.success(`找到 ${cardList.value.length} 个可用房间`)
    } else {
      ElMessage.info('该时段暂无可用房间')
    }
  } catch (err) {
    //（错误提示由请求拦截器统一处理）
    console.warn('查询可用房间失败：', err?.message || err)
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.info-container {
  width: 100%;
  max-width: 860px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.info-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -1;
  background-image: 
    radial-gradient(circle at 20% 10%, rgba(180, 215, 255, 0.6) 0%, transparent 40%),
    radial-gradient(circle at 80% 80%, rgba(200, 230, 255, 0.5) 0%, transparent 50%);
  border-radius: 32px;
  pointer-events: none;
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
  margin-bottom: 32px;
}

.filter-card, .card-label, .card-content {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(200, 220, 240, 0.4);
}

.filter-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 16px 24px;
  border-radius: 999px;
  box-shadow:
    0 10px 30px rgba(6, 26, 43, 0.06),
    0 2px 8px rgba(6, 26, 43, 0.03);
}

.filter-fields {
  display: flex;
  align-items: center;
  gap: 16px;
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

.info-list {
  display: flex;
  flex-direction: column;
  gap: 32px;
  min-height: 300px;
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
  width: 140px;
  padding: 8px 0;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #061a2b;
  border-radius: 999px;
  box-shadow:
    0 6px 16px rgba(6, 26, 43, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transition: transform 0.35s cubic-bezier(0.22, 0.61, 0.36, 1),
              box-shadow 0.35s ease,
              background 0.35s ease;
}

.card-content {
  align-self: stretch;
  margin-left: 80px;
  display: flex;
  align-items: stretch;
  gap: 24px;
  padding: 20px 28px;
  border-radius: 24px;
  box-shadow:
    0 12px 32px rgba(6, 26, 43, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transition: transform 0.35s cubic-bezier(0.22, 0.61, 0.36, 1),
              box-shadow 0.35s ease,
              background 0.35s ease;
}

.info-card:hover .card-label {
  transform: translateY(-3px);
  background: rgba(255, 255, 255, 0.7);
  box-shadow:
    0 12px 24px rgba(6, 26, 43, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 1);
}

.info-card:hover .card-content {
  transform: translateY(-3px);
  background: rgba(255, 255, 255, 0.7);
  box-shadow:
    0 20px 40px rgba(6, 26, 43, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 1);
}

.room-img-wrapper {
  width: 140px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(6, 26, 43, 0.15);
}

.room-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.info-card:hover .room-img {
  transform: scale(1.05);
}

.room-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 16px;
}

.room-lines {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}

.room-lines li {
  font-size: 14px;
  color: #5d7691;
  position: relative;
}

.room-lines li::before {
  content: '•';
  color: #061a2b;
  margin-right: 6px;
  font-weight: bold;
}

.room-bottom {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: auto;
}

.price-box {
  display: flex;
  align-items: baseline;
  color: #061a2b;
}

.price-symbol {
  font-size: 16px;
  font-weight: 600;
  margin-right: 2px;
}

.price-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1;
  font-family: "Din", "Helvetica Neue", Helvetica, Arial, sans-serif;
}

.price-unit {
  font-size: 13px;
  color: #5d7691;
  margin-left: 6px;
}

.price-origin {
  font-size: 12px;
  color: #9aa9bb;
  text-decoration: line-through;
  margin-left: 10px;
}

.card-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.book-btn {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 1px;
  border-bottom: 1px solid transparent;
  transition: all 0.2s;
}

.book-btn:hover {
  border-bottom: 1px solid #061a2b;
}

@media (max-width: 768px) {
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

  .info-list {
    gap: 24px;
  }

  .card-label {
    width: 120px;
    padding: 7px 0;
    font-size: 13px;
  }

  .card-content {
    margin-left: 40px;
    padding: 16px;
    flex-direction: column;
    gap: 16px;
  }

  .room-img-wrapper {
    width: 100%;
    height: 180px;
  }

  .room-details {
    width: 100%;
  }

  .room-bottom {
    align-items: center;
  }
}
</style>