<template>
  <el-config-provider :locale="zhCn">
    <div class="room-list-container">
      <el-card class="header-card" shadow="never">
        <div class="header-inner">
          <div class="header-text">
            <h1 class="page-title">房间管理</h1>
            <el-text class="page-desc" type="info" size="small">
              共 {{ rooms.length }} 条房间信息，其中运营中 {{ runningCount }} 条
            </el-text>
          </div>
          <el-space>
            <el-button class="ghost-btn" @click="handleAddCategory">
              <el-icon><FolderAdd /></el-icon>
              <span>增加房型</span>
            </el-button>
            <el-button type="primary" class="primary-btn" @click="handleAddRoom">
              <el-icon><Plus /></el-icon>
              <span>增加房间</span>
            </el-button>
          </el-space>
        </div>
      </el-card>

      <transition name="fade-slide">
        <div v-if="showCategoryPanel" class="category-anchor">
          <el-card class="category-card" shadow="never">
            <div class="category-header">
              <div class="category-title-wrap">
                <h2 class="category-title">房型管理</h2>
                <el-text size="small" type="info">
                  共 {{ categories.length }} 个房型
                </el-text>
              </div>
              <el-button text circle class="close-btn" @click="handleAddCategory">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>

            <div v-loading="categoryLoading" class="category-list">
              <div
                v-for="cat in categories"
                :key="cat.id"
                class="category-item"
                :class="{ 'is-selected': selectedCategoryId === cat.id }"
                @click="handleSelectCategory(cat)"
              >
                <el-icon class="category-icon"><CollectionTag /></el-icon>
                <span class="category-name">{{ cat.name }}</span>
                <span class="category-id">{{ cat.id }}</span>
                <el-button
                  link
                  type="danger"
                  size="small"
                  class="category-delete-btn"
                  :loading="deletingId === cat.id"
                  @click.stop="handleDeleteCategory(cat)"
                >
                  删除
                </el-button>
              </div>
              <el-text
                v-if="!categoryLoading && !categories.length"
                size="small"
                type="info"
              >
                暂无房型数据
              </el-text>
            </div>

            <el-divider class="card-divider" />

            <el-form
              :model="categoryForm"
              label-width="88px"
              label-position="left"
              class="category-form"
            >
              <el-form-item label="房型名称">
                <el-input
                  v-model="categoryForm.name"
                  placeholder="请输入房型名称"
                  clearable
                />
              </el-form-item>

              <el-form-item label="VIP 折扣">
                <div class="vip-discounts">
                  <el-input
                    v-for="(item, index) in categoryForm.vipDiscounts"
                    :key="index"
                    v-model="categoryForm.vipDiscounts[index]"
                    :placeholder="`${vipLevelLabels[index]} 如 0.9`"
                    class="vip-input"
                  />
                </div>
              </el-form-item>

              <el-form-item label="基础价格">
                <el-input
                  v-model="categoryForm.basicPrice"
                  placeholder="请输入基础价格"
                >
                  <template #prepend>¥</template>
                </el-input>
              </el-form-item>

              <el-form-item label="房型描述">
                <el-input
                  v-model="categoryForm.description"
                  type="textarea"
                  :rows="3"
                  maxlength="200"
                  show-word-limit
                  placeholder="请输入房型描述"
                />
              </el-form-item>

              <el-form-item label="房型图片">
                <el-upload
                  v-model:file-list="pictureList"
                  list-type="picture-card"
                  accept="image/*"
                  multiple
                  :http-request="handleUploadPicture"
                  :on-preview="handlePicturePreview"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
              </el-form-item>

              <div class="category-actions">
                <template v-if="selectedCategoryId">
                  <el-button class="ghost-btn" @click="resetCategoryForm">
                    取消编辑
                  </el-button>
                  <el-button
                    type="primary"
                    class="primary-btn"
                    :loading="submitLoading"
                    @click="handleUpdateCategory"
                  >
                    保存修改
                  </el-button>
                </template>
                <el-button
                  v-else
                  type="primary"
                  class="primary-btn"
                  :loading="submitLoading"
                  @click="handleSubmitCategory"
                >
                  确定添加
                </el-button>
              </div>
            </el-form>
          </el-card>
        </div>
      </transition>

      <transition name="fade-slide">
        <div v-if="showRoomPanel" class="category-anchor">
          <el-card class="category-card" shadow="never">
            <div class="category-header">
              <div class="category-title-wrap">
                <h2 class="category-title">添加房间</h2>
                <el-text size="small" type="info">
                  填写房间名称并选择所属房型
                </el-text>
              </div>
              <el-button text circle class="close-btn" @click="handleAddRoom">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>

            <el-form
              :model="roomForm"
              label-width="88px"
              label-position="left"
              class="category-form"
            >
              <el-form-item label="房间名称">
                <el-input
                  v-model="roomForm.name"
                  placeholder="请输入房间名称"
                  clearable
                />
              </el-form-item>

              <el-form-item label="所属房型">
                <el-select
                  v-model="roomForm.categoryId"
                  placeholder="请选择房型"
                  class="room-category-select"
                >
                  <el-option
                    v-for="cat in categories"
                    :key="cat.id"
                    :label="cat.name"
                    :value="cat.id"
                  />
                </el-select>
              </el-form-item>

              <div class="category-actions">
                <el-button
                  type="primary"
                  class="primary-btn"
                  :loading="roomSubmitLoading"
                  @click="handleSubmitRoom"
                >
                  确定添加
                </el-button>
              </div>
            </el-form>
          </el-card>
        </div>
      </transition>

      
      <transition name="fade-slide">
        <div v-if="showEditPanel" class="category-anchor">
          <el-card class="category-card" shadow="never">
            <div class="category-header">
              <div class="category-title-wrap">
                <h2 class="category-title">修改房间</h2>
                <el-text size="small" type="info">
                  {{ editingRoom ? `${editingRoom.name} · 可修改所属房型` : '' }}
                </el-text>
              </div>
              <el-button text circle class="close-btn" @click="closeEditPanel">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>

            <el-form
              :model="editForm"
              label-width="88px"
              label-position="left"
              class="category-form"
            >
              <el-form-item label="房间名称">
                <el-input
                  :model-value="editingRoom ? editingRoom.name : ''"
                  disabled
                  placeholder="房间名称"
                />
              </el-form-item>

              <el-form-item label="所属房型">
                <el-select
                  v-model="editForm.categoryId"
                  placeholder="请选择房型"
                  class="room-category-select"
                  :loading="categoryLoading"
                >
                  <el-option
                    v-for="cat in categories"
                    :key="cat.id"
                    :label="cat.name"
                    :value="cat.id"
                  />
                </el-select>
              </el-form-item>

              <div class="category-actions">
                <el-button
                  type="primary"
                  class="primary-btn"
                  @click="handleSubmitRoomEdit"
                >
                  确认修改
                </el-button>
              </div>
            </el-form>
          </el-card>
        </div>
      </transition>
      <transition name="fade-slide">
        <div v-if="selectedRoomId" ref="occupancyAnchor" class="occupancy-anchor">
          <el-card class="occupancy-card" shadow="never">
            <div class="occupancy-header">
              <div class="occupancy-title-wrap">
                <h2 class="occupancy-title">{{ selectedRoom?.name }}</h2>
                <el-text size="small" type="info">
                  占用时间表（自今日起）
                </el-text>
              </div>
              <el-button text circle class="close-btn" @click="closeOccupancy">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>

            <div class="occupancy-body">
              <el-skeleton v-if="occupancyLoading" :rows="5" animated />

              <template v-else>
                <el-calendar v-model="calendarDate" class="occupancy-calendar">
                  <template #date-cell="{ data }">
                    <div
                      class="cal-cell"
                      :class="{
                        'is-occupied': occupiedSet.has(data.day),
                        'is-other': data.type !== 'current-month'
                      }"
                    >
                      <span class="cal-num">
                        {{ Number(data.day.split('-')[2]) }}
                      </span>
                      <span
                        v-if="occupiedSet.has(data.day)"
                        class="cal-label"
                      >占用</span>
                    </div>
                  </template>
                </el-calendar>

                <div class="occupancy-table-wrap">
                  <div class="table-head">
                    <el-text size="small" type="info">占用明细</el-text>
                    <el-tag
                      v-if="occupancyRows.length"
                      type="danger"
                      effect="light"
                      round
                      size="small"
                    >
                      共 {{ occupancyRows.length }} 个
                    </el-tag>
                  </div>

                  <el-table
                    :data="occupancyRows"
                    size="small"
                    class="occupancy-table"
                    :header-cell-style="{ background: 'transparent' }"
                  >
                    <el-table-column
                      type="index"
                      label="#"
                      width="48"
                      align="center"
                    />
                    <el-table-column prop="date" label="日期" min-width="130" />
                    <el-table-column
                      prop="weekday"
                      label="星期"
                      width="90"
                      align="center"
                    />
                    <el-table-column label="状态" width="100" align="center">
                      <template #default>
                        <el-tag
                          type="danger"
                          effect="light"
                          round
                          size="small"
                        >
                          占用
                        </el-tag>
                      </template>
                    </el-table-column>

                    <template #empty>
                      <el-text size="small" type="info">
                        该房间暂无占用时间
                      </el-text>
                    </template>
                  </el-table>
                </div>
              </template>
            </div>
          </el-card>
        </div>
      </transition>

      <el-row v-if="isLoading" :gutter="16">
        <el-col
          v-for="i in 6"
          :key="i"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="col-item"
        >
          <el-card class="room-card skeleton-card" shadow="never">
            <el-skeleton :rows="3" animated />
          </el-card>
        </el-col>
      </el-row>

      <el-row v-else-if="rooms.length" :gutter="16">
        <el-col
          v-for="room in rooms"
          :key="room.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="col-item"
        >
          <el-card
            class="room-card"
            :class="{
              'is-disabled': room.status === 0,
              'is-active': selectedRoomId === room.id
            }"
            shadow="hover"
            @click="openOccupancy(room)"
          >
            <div class="card-top">
              <el-text class="room-name" truncated>{{ room.name }}</el-text>
              <el-tag
                :type="room.status === 1 ? 'success' : 'info'"
                effect="light"
                round
                size="small"
                class="status-tag"
              >
                <span>{{ room.status === 1 ? '运营中' : '不可用' }}</span>
              </el-tag>
            </div>

            <div class="card-meta">
              <el-tag
                effect="plain"
                round
                type="info"
                size="small"
                class="category-tag"
              >
                <el-icon><CollectionTag /></el-icon>
                <span>{{ room.categoryName }}</span>
              </el-tag>
            </div>

            <el-divider class="card-divider" />

            <div class="card-footer">
              <div class="footer-id">ID：{{ room.id }}</div>

              <div class="footer-actions">
                <el-button
                  class="switch-btn edit-btn"
                  round
                  size="small"
                  @click.stop="handleEditRoom(room)"
                >
                  修改
                </el-button>
                <el-button
                  class="switch-btn"
                  :class="room.status === 1 ? 'disable-btn' : 'enable-btn'"
                  round
                  size="small"
                  @click.stop="toggleStatus(room)"
                >
                  {{ room.status === 1 ? '禁用' : '启用' }}
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card v-else class="empty-card" shadow="never">
        <el-empty description="暂无房间信息">
          <el-button type="primary" class="primary-btn" @click="handleAddRoom">
            <el-icon><Plus /></el-icon>
            <span>添加房间</span>
          </el-button>
        </el-empty>
      </el-card>
      <el-dialog
        v-model="previewVisible"
        title="图片预览"
        width="520px"
        append-to-body
      >
        <img :src="previewUrl" class="preview-img" alt="房型图片预览" />
      </el-dialog>
    </div>
  </el-config-provider>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import {
  Plus,
  FolderAdd,
  CollectionTag,
  Close
} from '@element-plus/icons-vue'
import request from '@/api/request'
import { uploadImage } from '@/api/upload'

//（房间列表数据与加载状态）
const isLoading = ref(true)
const rooms = ref([])

const runningCount = computed(
  () => rooms.value.filter((r) => r.status === 1).length
)

//（拉取全部房间数据）
const fetchRooms = async () => {
  isLoading.value = true
  try {
    const res = await request.get('/admin/rooms')
    rooms.value = (res.data || []).map((room) => ({
      id: room.id,
      name: room.roomName,
      categoryId: room.categoryId,
      categoryName: room.categoryName,
      status: room.status == null ? 1 : room.status
    }))
  } catch (err) {
    console.warn('获取房间列表失败：', err?.message || err)
  } finally {
    isLoading.value = false
  }
}

const handleAddCategory = () => {
  if (showCategoryPanel.value) {
    showCategoryPanel.value = false
    return
  }
  showCategoryPanel.value = true
  selectedRoomId.value = false
  showRoomPanel.value = false
  closeEditPanel()
  resetCategoryForm()
  if (!categories.value.length && !categoryLoading.value) {
    fetchCategories()
  }
}

const handleAddRoom = () => {
  if (showRoomPanel.value) {
    showRoomPanel.value = false
    return
  }
  showRoomPanel.value = true
  selectedRoomId.value = false
  showCategoryPanel.value = false
  closeEditPanel()
  if (!categories.value.length && !categoryLoading.value) {
    fetchCategories()
  }
}

//（房型管理状态与表单）
const showCategoryPanel = ref(false)
const categoryLoading = ref(false)
const categories = ref([])
const submitLoading = ref(false)

const vipLevelLabels = ['普通会员', '银卡会员', '金卡会员', '大师会员']
const selectedCategoryId = ref(null)
const deletingId = ref(null)

const categoryForm = reactive({
  name: '',
  vipDiscounts: ['', '', '', ''],
  basicPrice: '',
  description: ''
})

//（房型图片列表与预览弹窗状态）
const pictureList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

//（拉取全部房型数据）
const fetchCategories = async () => {
  categoryLoading.value = true
  try {
    const res = await request.get('/admin/getallcategory')
    categories.value = (res.data || []).map((cat) => ({
      id: cat.id,
      name: cat.name,
      basicPrice: cat.basicPrice,
      description: cat.description || '',
      picture: Array.isArray(cat.picture) ? cat.picture : [],
      discounts: cat.discounts || []
    }))
  } catch (err) {
    console.warn('获取房型列表失败：', err?.message || err)
  } finally {
    categoryLoading.value = false
  }
}

const resetCategoryForm = () => {
  selectedCategoryId.value = null
  categoryForm.name = ''
  categoryForm.vipDiscounts = ['', '', '', '']
  categoryForm.basicPrice = ''
  categoryForm.description = ''
  pictureList.value = []
}

const handleSelectCategory = (cat) => {
  if (!cat) return
  selectedCategoryId.value = cat.id
  categoryForm.name = cat.name || ''
  categoryForm.basicPrice = cat.basicPrice != null ? String(cat.basicPrice) : ''
  categoryForm.description = cat.description || ''
  pictureList.value = (cat.picture || []).map((url, i) => ({
    uid: `old-${i}`,
    name: url.split('/').pop() || `图片${i + 1}`,
    url,
    status: 'success'
  }))

  const discounts = ['', '', '', '']
  ;(cat.discounts || []).forEach((item) => {
    const index = Number(item.viplevel) - 1
    if (index >= 0 && index < 4) {
      discounts[index] =
        item.vipDiscount != null ? String(item.vipDiscount) : ''
    }
  })
  categoryForm.vipDiscounts = discounts
}

const isPositiveNumber = (val) => /^\d+(\.\d+)?$/.test(String(val).trim())

const collectDiscounts = () => {
  const list = []
  categoryForm.vipDiscounts.forEach((val, index) => {
    const text = String(val == null ? '' : val).trim()
    if (!text) return
    list.push({ viplevel: index + 1, vipDiscount: Number(text) })
  })
  return list
}

const validateDiscounts = (list) =>
  list.every((item) => item.vipDiscount > 0 && item.vipDiscount <= 1)

//（上传图片到 MinIO 并回填可访问地址）
const handleUploadPicture = async ({ file, onSuccess, onError }) => {
  try {
    const url = await uploadImage(file)
    const item = pictureList.value.find((f) => f.uid === file.uid)
    if (item) item.url = url
    onSuccess(url)
  } catch (err) {
    console.warn('图片上传失败：', err?.message || err)
    ElMessage.error('图片上传失败，请检查 MinIO 服务是否可用')
    onError(err)
  }
}

const handlePicturePreview = (file) => {
  previewUrl.value = file.url
  previewVisible.value = true
}

//（确认没有图片仍在上传中）
const picturesReady = () => {
  if (pictureList.value.some((f) => !f.url)) {
    ElMessage.warning('图片仍在上传中，请稍候')
    return false
  }
  return true
}

const currentPictures = () =>
  pictureList.value.map((f) => f.url).filter(Boolean)

const handleSubmitCategory = async () => {
  const name = categoryForm.name.trim()
  if (!name) {
    ElMessage.warning('请输入房型名称')
    return
  }
  if (categories.value.some((c) => c.name === name)) {
    ElMessage.warning(`房型「${name}」已存在`)
    return
  }
  if (categoryForm.basicPrice === '' || !isPositiveNumber(categoryForm.basicPrice)) {
    ElMessage.warning('基础价格必须为正数')
    return
  }

  const discounts = collectDiscounts()
  if (!discounts.length) {
    ElMessage.warning('请至少填写一档 VIP 折扣')
    return
  }
  if (!validateDiscounts(discounts)) {
    ElMessage.warning('VIP 折扣需为 0~1 之间的数字（如 0.9）')
    return
  }
  if (!picturesReady()) return

  const pictures = currentPictures()
  submitLoading.value = true
  try {
    await request.post('/admin/addcategory', {
      name,
      basicPrice: Number(categoryForm.basicPrice),
      discounts,
      ...(pictures.length ? { picture: pictures } : {}),
      description: categoryForm.description.trim() || undefined
    })
    ElMessage.success(`已添加房型「${name}」`)
    resetCategoryForm()
    await fetchCategories()
  } catch (err) {
    console.warn('新增房型失败：', err?.message || err)
  } finally {
    submitLoading.value = false
  }
}

const handleUpdateCategory = async () => {
  const cat = categories.value.find((c) => c.id === selectedCategoryId.value)
  if (!cat) {
    ElMessage.warning('房型不存在，请重新选择')
    return
  }

  const name = categoryForm.name.trim()
  if (!name) {
    ElMessage.warning('请输入房型名称')
    return
  }
  if (categories.value.some((c) => c.name === name && c.id !== cat.id)) {
    ElMessage.warning(`房型「${name}」已存在`)
    return
  }
  if (categoryForm.basicPrice === '' || !isPositiveNumber(categoryForm.basicPrice)) {
    ElMessage.warning('基础价格必须为正数')
    return
  }

  const discounts = collectDiscounts()
  if (!validateDiscounts(discounts)) {
    ElMessage.warning('VIP 折扣需为 0~1 之间的数字（如 0.9）')
    return
  }
  if (!picturesReady()) return

  const payload = { id: cat.id }
  if (name !== cat.name) payload.name = name
  payload.basicPrice = Number(categoryForm.basicPrice)
  if (discounts.length) payload.discounts = discounts
  const description = categoryForm.description.trim()
  if (description) payload.description = description
  const pictures = currentPictures()
  if (pictures.join('|') !== (cat.picture || []).join('|')) {
    payload.picture = pictures
  }

  submitLoading.value = true
  try {
    await request.post('/admin/updatecategory', payload)
    ElMessage.success(`房型「${name}」已更新`)
    await fetchCategories()
    fetchRooms()
    handleSelectCategory(categories.value.find((c) => c.id === cat.id))
  } catch (err) {
    console.warn('修改房型失败：', err?.message || err)
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteCategory = async (cat) => {
  try {
    await ElMessageBox.confirm(
      `确定删除房型「${cat.name}」吗？该房型下存在房间时无法删除。`,
      '删除房型',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'confirm-disable-btn'
      }
    )
  } catch {
    return
  }

  deletingId.value = cat.id
  try {
    await request.post('/admin/deletecategory', null, {
      params: { id: cat.id }
    })
    ElMessage.success(`已删除房型「${cat.name}」`)
    if (selectedCategoryId.value === cat.id) {
      resetCategoryForm()
    }
    await fetchCategories()
  } catch (err) {
    console.warn('删除房型失败：', err?.message || err)
  } finally {
    deletingId.value = null
  }
}

//（添加房间状态与表单）
const showRoomPanel = ref(false)
const roomSubmitLoading = ref(false)

const roomForm = reactive({
  name: '',
  categoryId: ''
})

const handleSubmitRoom = async () => {
  const name = roomForm.name.trim()
  if (!name) {
    ElMessage.warning('请输入房间名称')
    return
  }
  if (name.length > 10) {
    ElMessage.warning('房间名称不超过 10 个字符')
    return
  }
  if (!roomForm.categoryId) {
    ElMessage.warning('请选择所属房型')
    return
  }
  if (rooms.value.some((r) => r.name === name)) {
    ElMessage.warning(`房间「${name}」已存在`)
    return
  }

  roomSubmitLoading.value = true
  try {
    await request.post('/admin/addroom', {
      name,
      categoryId: roomForm.categoryId
    })
    ElMessage.success(`已添加房间「${name}」`)
    roomForm.name = ''
    roomForm.categoryId = ''
    await fetchRooms()
  } catch (err) {
    console.warn('新增房间失败：', err?.message || err)
  } finally {
    roomSubmitLoading.value = false
  }
}


//（修改房间所属房型状态）
const showEditPanel = ref(false)
const editingRoomId = ref(null)
const editForm = reactive({
  categoryId: ''
})

const editingRoom = computed(
  () => rooms.value.find((r) => r.id === editingRoomId.value) || null
)

const handleEditRoom = (room) => {
  if (showEditPanel.value && editingRoomId.value === room.id) {
    closeEditPanel()
    return
  }
  showEditPanel.value = true
  editingRoomId.value = room.id
  editForm.categoryId = room.categoryId || ''
  selectedRoomId.value = null
  showCategoryPanel.value = false
  showRoomPanel.value = false
  if (!categories.value.length && !categoryLoading.value) {
    fetchCategories()
  }
}

const closeEditPanel = () => {
  showEditPanel.value = false
  editingRoomId.value = null
  editForm.categoryId = ''
}

const handleSubmitRoomEdit = async () => {
  if (!editingRoom.value) {
    ElMessage.warning('房间信息不存在')
    return
  }
  if (!editForm.categoryId) {
    ElMessage.warning('请选择所属房型')
    return
  }

  try {
    await request.post('/admin/updateroomcategory', {
      id: editingRoom.value.id,
      categoryId: editForm.categoryId
    })
    ElMessage.success('房型修改成功')
    closeEditPanel()
    await fetchRooms()
  } catch (err) {
    console.warn('修改房间房型失败：', err?.message || err)
  }
}

const toggleStatus = async (room) => {
  const nextStatus = room.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(
      `确定要${actionText}「${room.name}」吗？`,
      `${actionText}房间`,
      {
        confirmButtonText: `确认${actionText}`,
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass:
          nextStatus === 1 ? 'confirm-enable-btn' : 'confirm-disable-btn'
      }
    )
  } catch {
    return
  }

  try {
    await request.post('/admin/setroomstatus', {
      id: room.id,
      status: nextStatus
    })
    room.status = nextStatus
    ElMessage.success(`已${actionText}「${room.name}」`)
  } catch (err) {
    console.warn('设置房间状态失败：', err?.message || err)
  }
}


//（占用时间表数据与查询）
const WEEKDAY_CN = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

const fetchOccupancy = async (roomId) => {
  const res = await request.get('/admin/getroomstatus', {
    params: { roomId }
  })
  const dates = Array.isArray(res.data) ? res.data : []
  return dates.filter((d) => typeof d === 'string' && d)
}

const selectedRoomId = ref(null)
const occupancyDates = ref([])
const occupancyLoading = ref(false)
const calendarDate = ref(new Date())
const occupancyAnchor = ref(null)

const selectedRoom = computed(
  () => rooms.value.find((r) => r.id === selectedRoomId.value) || null
)

const occupiedSet = computed(() => new Set(occupancyDates.value))

const occupancyRows = computed(() =>
  occupancyDates.value.map((d, i) => {
    const date = new Date(`${d}T00:00:00`)
    return {
      seq: i + 1,
      date: d,
      weekday: WEEKDAY_CN[date.getDay()]
    }
  })
)

let loadToken = 0

const openOccupancy = async (room) => {
  if (selectedRoomId.value === room.id) {
    closeOccupancy()
    return
  }

  const token = ++loadToken

  selectedRoomId.value = room.id
  occupancyDates.value = []
  occupancyLoading.value = true
  showCategoryPanel.value = false
  showRoomPanel.value = false
  closeEditPanel()
  await nextTick()
  setTimeout(() => {
    occupancyAnchor.value?.scrollIntoView({
      behavior: 'smooth',
      block: 'nearest'
    })
  }, 60)

  let dates = []
  try {
    dates = await fetchOccupancy(room.id)
  } catch (err) {
    console.warn('获取房间占用情况失败：', err?.message || err)
  }
  if (token !== loadToken) return

  const sorted = [...dates].sort()
  occupancyDates.value = sorted
  calendarDate.value = sorted.length
    ? new Date(`${sorted[0]}T00:00:00`)
    : new Date()

  occupancyLoading.value = false
}

const closeOccupancy = () => {
  loadToken++
  selectedRoomId.value = null
  occupancyDates.value = []
  occupancyLoading.value = false
}

onMounted(fetchRooms)
</script>

<style scoped>
.room-list-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

:deep(.header-card.el-card) {
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(20px) saturate(160%);
  -webkit-backdrop-filter: blur(20px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 20px;
  box-shadow: 0 8px 28px rgba(6, 26, 43, 0.08);
  margin-bottom: 24px;
}
:deep(.header-card .el-card__body) {
  padding: 20px 24px;
}

.header-inner {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.page-title {
  font-family: "Songti SC", "STSong", "SimSun", serif;
  font-size: 26px;
  font-weight: 600;
  color: #061a2b;
  margin: 0 0 8px 0;
}

.page-desc {
  display: block;
  color: #5d7691 !important;
}

.ghost-btn {
  height: 40px;
  padding: 0 20px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  color: #061a2b;
  background: rgba(255, 255, 255, 0.6);
  border-color: rgba(6, 26, 43, 0.12);
  transition: all 0.3s ease;
}
.ghost-btn:hover {
  color: #061a2b;
  background: #ffffff;
  border-color: rgba(64, 158, 255, 0.6);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.18);
  transform: translateY(-2px);
}
.ghost-btn .el-icon {
  margin-right: 4px;
}

.primary-btn {
  height: 40px;
  padding: 0 22px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 1px;
  background-color: #061a2b;
  border-color: #061a2b;
  box-shadow: 0 8px 20px rgba(6, 26, 43, 0.2);
  transition: all 0.3s ease;
}
.primary-btn:hover {
  background-color: #143b5c;
  border-color: #143b5c;
  transform: translateY(-2px);
  box-shadow: 0 12px 26px rgba(6, 26, 43, 0.28);
}
.primary-btn .el-icon {
  margin-right: 4px;
}

.col-item {
  margin-bottom: 16px;
}

:deep(.room-card.el-card) {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 18px;
  box-shadow: 0 8px 24px rgba(6, 26, 43, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease, background 0.3s ease,
    border-color 0.3s ease;
  height: 100%;
  box-sizing: border-box;
  cursor: pointer;
}
:deep(.room-card .el-card__body) {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
:deep(.room-card.el-card:hover) {
  transform: translateY(-4px);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 16px 36px rgba(6, 26, 43, 0.14) !important;
}

:deep(.room-card.is-active.el-card) {
  border-color: rgba(64, 158, 255, 0.75);
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.22),
    0 16px 36px rgba(6, 26, 43, 0.14) !important;
  transform: translateY(-2px);
}

:deep(.room-card.is-disabled.el-card) {
  background: rgba(242, 245, 248, 0.72);
  border-color: rgba(6, 26, 43, 0.08);
}
:deep(.room-card.is-disabled.el-card:hover) {
  transform: translateY(-2px);
  background: rgba(245, 247, 250, 0.85);
}
:deep(.room-card.is-disabled.is-active.el-card) {
  border-color: rgba(64, 158, 255, 0.55);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.16),
    0 12px 28px rgba(6, 26, 43, 0.1) !important;
}
:deep(.room-card.is-disabled .room-name) {
  color: #7c8899 !important;
  text-shadow: none;
}
:deep(.room-card.is-disabled .footer-id) {
  color: #b3bcc7 !important;
}

:deep(.skeleton-card.el-card),
:deep(.skeleton-card.el-card:hover) {
  transform: none;
  background: rgba(255, 255, 255, 0.5);
  cursor: default;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.room-name {
  font-family: "Songti SC", "STSong", "SimSun", "Noto Serif SC", serif !important;
  font-size: 17px !important;
  font-weight: 500 !important;
  color: #061a2b !important;
  line-height: 1.35;
  letter-spacing: 1.5px;
  min-width: 0;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.6);
  transition: color 0.3s ease;
}

.status-tag {
  flex-shrink: 0;
}
:deep(.status-tag .el-tag__content) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-tag {
  display: inline-flex;
  align-items: center;
  background-color: rgba(244, 247, 250, 0.85);
  border-color: rgba(225, 232, 240, 0.9);
  color: #5d7691;
  --el-tag-text-color: #5d7691;
}
:deep(.category-tag .el-tag__content) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

:deep(.card-divider.el-divider--horizontal) {
  margin: 4px 0;
  border-top-color: rgba(6, 26, 43, 0.07);
}

.card-footer {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 10px;
}

.footer-id {
  font-family: "Din", monospace;
  font-size: 11px;
  letter-spacing: 0.3px;
  line-height: 1.4;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.3s ease;
}

.footer-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.edit-btn {
  color: #061a2b;
  border-color: rgba(6, 26, 43, 0.25);
}
.edit-btn:hover {
  color: #ffffff;
  background: #061a2b;
  border-color: #061a2b;
  box-shadow: 0 6px 14px rgba(6, 26, 43, 0.22);
  transform: translateY(-1px);
}

.switch-btn {
  flex-shrink: 0;
  height: 30px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  background: rgba(255, 255, 255, 0.7);
  transition: all 0.25s ease;
}

.disable-btn {
  color: #d95656;
  border-color: rgba(245, 108, 108, 0.4);
}
.disable-btn:hover {
  color: #ffffff;
  background: #f56c6c;
  border-color: #f56c6c;
  box-shadow: 0 6px 14px rgba(245, 108, 108, 0.28);
  transform: translateY(-1px);
}

.enable-btn {
  color: #2e9e5b;
  border-color: rgba(103, 194, 58, 0.4);
}
.enable-btn:hover {
  color: #ffffff;
  background: #2e9e5b;
  border-color: #2e9e5b;
  box-shadow: 0 6px 14px rgba(46, 158, 91, 0.28);
  transform: translateY(-1px);
}

:deep(.empty-card.el-card) {
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(6, 26, 43, 0.06);
}
:deep(.empty-card .el-card__body) {
  padding: 40px 20px;
}

.category-anchor {
  margin-top: 8px;
  margin-bottom: 16px;
}

:deep(.category-card.el-card) {
  background: rgba(255, 255, 255, 0.62);
  backdrop-filter: blur(20px) saturate(160%);
  -webkit-backdrop-filter: blur(20px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(6, 26, 43, 0.1);
}
:deep(.category-card .el-card__body) {
  padding: 20px 24px 24px;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.category-title-wrap {
  min-width: 0;
}

.category-title {
  font-family: "Songti SC", "STSong", "SimSun", serif;
  font-size: 20px;
  font-weight: 600;
  color: #061a2b;
  margin: 0 0 4px 0;
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  min-height: 60px;
  padding: 14px;
  border: 1px solid rgba(6, 26, 43, 0.12);
  border-radius: 14px;
  background: rgba(246, 249, 252, 0.7);
  box-sizing: border-box;
}

.category-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(225, 232, 240, 0.9);
  font-size: 13px;
  color: #5d7691;
  cursor: pointer;
  user-select: none;
  transition: all 0.25s ease;
}
.category-item:hover {
  border-color: #061a2b;
  color: #061a2b;
  transform: translateY(-1px);
}

.category-item.is-selected {
  background: #061a2b;
  border-color: #061a2b;
  box-shadow: 0 8px 18px rgba(6, 26, 43, 0.2);
}
.category-item.is-selected .category-icon,
.category-item.is-selected .category-name,
.category-item.is-selected .category-id {
  color: #ffffff;
}

.category-delete-btn {
  height: auto;
  padding: 0;
  margin-left: 2px;
  font-size: 12px;
  color: #d95656;
}
.category-item.is-selected .category-delete-btn {
  color: #ffb3b3;
}

.category-icon {
  color: #061a2b;
  font-size: 14px;
}

.category-name {
  font-family: "Songti SC", "STSong", "SimSun", serif;
  font-weight: 600;
  letter-spacing: 1px;
  color: #061a2b;
}

.category-id {
  font-family: "Din", monospace;
  font-size: 11px;
  color: #909399;
}

.category-form {
  margin-top: 4px;
}

:deep(.category-form .el-form-item__label) {
  color: #5d7691;
  font-size: 13px;
}

:deep(.category-form .el-input__wrapper) {
  border-radius: 10px;
  background-color: rgba(255, 255, 255, 0.7);
  box-shadow: none;
  border: 1px solid rgba(225, 232, 240, 0.9);
  transition: all 0.3s ease;
}
:deep(.category-form .el-input__wrapper.is-focus) {
  border-color: #061a2b;
  background-color: #ffffff;
  box-shadow: 0 0 0 1px #061a2b inset;
}

:deep(.category-form .el-textarea__inner) {
  border-radius: 10px;
  background-color: rgba(255, 255, 255, 0.7);
  box-shadow: none;
  border: 1px solid rgba(225, 232, 240, 0.9);
  transition: all 0.3s ease;
}
:deep(.category-form .el-textarea__inner:focus) {
  border-color: #061a2b;
  background-color: #ffffff;
  box-shadow: 0 0 0 1px #061a2b inset;
}

:deep(.category-form .el-input-group__prepend) {
  background-color: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(225, 232, 240, 0.9);
  border-right: none;
  box-shadow: none;
  border-radius: 10px 0 0 10px;
  color: #5d7691;
}
:deep(.category-form .el-input-group--prepend .el-input__wrapper) {
  border-radius: 0 10px 10px 0;
}

:deep(.category-form .el-input__count) {
  background: transparent;
  color: #909399;
}

.room-category-select {
  width: 100%;
}

:deep(.category-form .el-select__wrapper) {
  border-radius: 10px;
  background-color: rgba(255, 255, 255, 0.7);
  box-shadow: none;
  border: 1px solid rgba(225, 232, 240, 0.9);
  min-height: 32px;
  transition: all 0.3s ease;
}
:deep(.category-form .el-select__wrapper.is-focused) {
  border-color: #061a2b;
  background-color: #ffffff;
  box-shadow: 0 0 0 1px #061a2b inset;
}

.vip-discounts {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  width: 100%;
}

:deep(.category-form .el-upload--picture-card),
:deep(.category-form .el-upload-list--picture-card .el-upload-list__item) {
  width: 92px;
  height: 92px;
  border-radius: 8px;
}

:deep(.category-form .el-upload--picture-card) {
  border-color: rgba(6, 26, 43, 0.2);
  background: rgba(255, 255, 255, 0.6);
}

.preview-img {
  display: block;
  width: 100%;
  border-radius: 8px;
}

.category-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 4px;
}

.occupancy-anchor {
  margin-top: 8px;
  margin-bottom: 8px;
}

:deep(.occupancy-card.el-card) {
  background: rgba(255, 255, 255, 0.62);
  backdrop-filter: blur(20px) saturate(160%);
  -webkit-backdrop-filter: blur(20px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(6, 26, 43, 0.1);
}
:deep(.occupancy-card .el-card__body) {
  padding: 20px 24px 24px;
}

.occupancy-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.occupancy-title-wrap {
  min-width: 0;
}

.occupancy-title {
  font-family: "Songti SC", "STSong", "SimSun", serif;
  font-size: 20px;
  font-weight: 600;
  color: #061a2b;
  margin: 0 0 4px 0;
}

.close-btn {
  color: #8a94a6;
  flex-shrink: 0;
  transition: all 0.25s ease;
}
.close-btn:hover {
  color: #061a2b;
  background: rgba(6, 26, 43, 0.06);
  transform: rotate(90deg);
}

.occupancy-body {
  min-height: 120px;
  margin-bottom: 16px;
}

:deep(.occupancy-calendar.el-calendar) {
  background: transparent;
  --el-calendar-border: rgba(6, 26, 43, 0.06);
}

:deep(.occupancy-calendar .el-calendar__header) {
  padding: 0 0 12px;
  border-bottom: 1px solid rgba(6, 26, 43, 0.06);
}

:deep(.occupancy-calendar .el-calendar__title) {
  font-size: 15px;
  font-weight: 600;
  color: #061a2b;
}

:deep(.occupancy-calendar .el-calendar__button-group .el-button) {
  border-radius: 8px;
  color: #5d7691;
  border-color: rgba(6, 26, 43, 0.12);
  background: rgba(255, 255, 255, 0.6);
}
:deep(.occupancy-calendar .el-calendar__button-group .el-button:hover) {
  color: #409eff;
  border-color: rgba(64, 158, 255, 0.5);
  background: #ffffff;
}

:deep(.occupancy-calendar .el-calendar__body) {
  padding: 8px 0 0;
}

:deep(.occupancy-calendar .el-calendar-table thead th) {
  padding: 6px 0;
  font-size: 12px;
  font-weight: 500;
  color: #909399;
}

:deep(.occupancy-calendar .el-calendar-table td) {
  border-color: rgba(6, 26, 43, 0.05);
  transition: background 0.2s ease;
}
:deep(.occupancy-calendar .el-calendar-table td:hover) {
  background: rgba(64, 158, 255, 0.05);
}

:deep(.occupancy-calendar .el-calendar-table td.is-selected) {
  background: transparent;
}

:deep(.occupancy-calendar .el-calendar-table .el-calendar-day) {
  height: 52px;
  padding: 3px;
  box-sizing: border-box;
}

.cal-cell {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
  border-radius: 8px;
  color: #061a2b;
  transition: all 0.2s ease;
}

.cal-cell.is-other {
  opacity: 0.32;
}

.cal-cell.is-occupied {
  background: rgba(245, 108, 108, 0.12);
  color: #e04848;
  font-weight: 600;
}

.cal-num {
  font-size: 13px;
  line-height: 1.1;
  font-family: "Din", sans-serif;
}

.cal-label {
  font-size: 10px;
  line-height: 1;
  letter-spacing: 0.5px;
}

.occupancy-table-wrap {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed rgba(6, 26, 43, 0.1);
}

.table-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

:deep(.occupancy-table.el-table) {
  background: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(246, 249, 252, 0.65);
  --el-table-border-color: rgba(6, 26, 43, 0.06);
  --el-table-row-hover-bg-color: rgba(64, 158, 255, 0.06);
  --el-table-text-color: #061a2b;
  --el-table-header-text-color: #5d7691;
  border-radius: 10px;
  overflow: hidden;
  font-size: 13px;
}

:deep(.occupancy-table .el-table__cell) {
  padding: 8px 0;
}

:deep(.occupancy-table .el-table__inner-wrapper::before) {
  display: none;
}

:deep(.occupancy-table .el-table__empty-block) {
  min-height: 64px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

@supports not ((backdrop-filter: blur(1px)) or (-webkit-backdrop-filter: blur(1px))) {
  :deep(.header-card.el-card),
  :deep(.room-card.el-card),
  :deep(.empty-card.el-card),
  :deep(.occupancy-card.el-card),
  :deep(.category-card.el-card) {
    background: rgba(255, 255, 255, 0.94);
  }
}

@media (max-width: 768px) {
  .room-list-container {
    padding: 12px;
  }

  .header-inner {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .page-title {
    font-size: 22px;
  }

  :deep(.header-inner .el-space__item) {
    flex: 1;
  }
  :deep(.header-inner .el-space) {
    width: 100%;
  }
  .ghost-btn,
  .primary-btn {
    width: 100%;
  }

  :deep(.occupancy-card .el-card__body) {
    padding: 16px;
  }

  :deep(.category-card .el-card__body) {
    padding: 16px;
  }
  .vip-discounts {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  :deep(.occupancy-calendar .el-calendar-table .el-calendar-day) {
    height: 42px;
    padding: 2px;
  }
  .cal-num {
    font-size: 12px;
  }
  .cal-label {
    font-size: 9px;
  }
  :deep(.occupancy-calendar .el-calendar__header) {
    padding-bottom: 8px;
  }
}
</style>

<style>
.confirm-enable-btn.el-button--primary {
  background-color: #2e9e5b;
  border-color: #2e9e5b;
}
.confirm-enable-btn.el-button--primary:hover {
  background-color: #38b76b;
  border-color: #38b76b;
}
.confirm-disable-btn.el-button--primary {
  background-color: #f56c6c;
  border-color: #f56c6c;
}
.confirm-disable-btn.el-button--primary:hover {
  background-color: #f78989;
  border-color: #f78989;
}
</style>