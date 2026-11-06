<template>
  <div class="bg-wrap">
    <div class="bg-fixed"></div>
    <div class="bg-mask"></div>

    <div v-if="accountId" class="home-entry">
      <button class="user-btn" @click="to">进入主页</button>
    </div>

    <div v-if="accountId" class="user-bar">
      <button class="user-btn" @click="switchAccount">切换账号</button>
      <button class="user-btn" @click="switchRole">
        {{ isManage ? '切换客户' : '切换管理' }}
      </button>
    </div>

    <div class="head-bar">
      <img class="full-width" src="/head.png" />
      <div class="head-line" :style="{ '--line-scale': lineScale }"></div>
    </div>

    <h1 class="welcome" :style="{ opacity: welcomeOpacity }">
      欢迎入住丽思卡尔顿酒店
    </h1>

    <div v-if="!accountId" class="entry-group" :style="{ opacity: iconOpacity }">
      <div class="entry-item" @click="handleClick('logIn')">
        <el-icon :size="30"><Avatar /></el-icon>
        <span>登录</span>
      </div>
      <div class="entry-item" @click="handleClick('register')">
        <el-icon :size="30"><HelpFilled /></el-icon>
        <span>注册</span>
        </div>
      <div class="entry-item" @click="handleClick('manageLog')">
        <el-icon :size="30"><UploadFilled /></el-icon>
        <span>管理</span>
      </div>
    </div>

    <div class="card-window">
      <button
        class="nav-btn nav-prev"
        :class="{ 'is-hidden': !canPrev }"
        aria-label="上一张"
        @click="scrollCards(-1)"
      >
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </button>

      <div
        ref="cardTrack"
        class="card-list"
        @scroll="updateArrows"
        @pointerdown="onPointerDown"
        @pointermove="onPointerMove"
        @pointerup="onPointerUp"
        @pointercancel="onPointerUp"
      >
        <div v-for="(item, index) in cardList" :key="index" class="card" @click="handleClick2(item)">
          <div class="card-img">
            <img :src="item.img" :alt="item.title" @error="onImgError(item)" />
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ item.title }}</h3>
            <p v-for="(line, i) in item.lines" :key="i" class="card-line">
              {{ line }}
            </p>
          </div>
        </div>
      </div>

      <button
        class="nav-btn nav-next"
        :class="{ 'is-hidden': !canNext }"
        aria-label="下一张"
        @click="scrollCards(1)"
      >
        <el-icon :size="20"><ArrowRight /></el-icon>
      </button>
    </div>

    <div class="glass-panel">
      <div class="glass-item">
        <h4 class="glass-title">关于酒店</h4>
        <p class="glass-text">居于静谧一隅，揽温柔光景入窗。以简约雅致的空间承载旅途休憩时光，一器一物皆藏用心，在舒缓氛围里，卸下奔波疲惫，邂逅自在从容的旅居体验。</p>
      </div>
      <div class="glass-item">
        <h4 class="glass-title">尊享服务</h4>
        <p class="glass-text">细致相伴，礼遇随行。我们珍视每一段旅程，以妥帖入微的贴心照料，响应您的所需所想，于细微处传递温暖，让每一次停留，都拥有专属舒心体验。</p>
      </div>
      <div class="glass-item">
        <h4 class="glass-title">联系方式</h4>
        <p class="glass-text">电话 400-000-0000 · 邮箱 hotel@example.com</p>
      </div>
    </div>
  </div>
    <footer class="footer">
      <h2 class="footer-brand">#RIZE CARLTON</h2>
      
      <div class="footer-desc">
        <p>透过我们宾客的视角探索丽思卡尔顿酒店。</p>
        <p>以@rizecarlton 及#RizeCarlton分享您的丽思之旅</p>
      </div>
      
      <div class="footer-social">
        <a href="#" class="social-btn social-wechat" aria-label="微信">
          <span>微信</span>
        </a>

        <a href="#" class="social-btn social-redbook" aria-label="小红书">
          <span>小红书</span>
        </a>
      </div>
    </footer>
    <div class="sub-footer">
      <div class="sub-footer-content">
        
        <div class="footer-nav">
          <h3 class="nav-title">探索更多</h3>
          <div class="nav-links-grid">
            <ul class="nav-col">
              <li><a href="#">媒体</a></li>
              <li><a href="#">职业发展</a></li>
              <li><a href="#">礼品卡</a></li>
              <li><a href="#">开发商</a></li>
            </ul>
            <ul class="nav-col">
              <li><a href="#">COOKIE 政策</a></li>
              <li><a href="#">隐私政策</a></li>
              <li><a href="#">辅助使用政策</a></li>
              <li><a href="#">联系我们</a></li>
            </ul>
          </div>
        </div>

        <div class="footer-contact">
          <h3 class="nav-title">客房 预订</h3>
          <div class="contact-group">
            <p>免费订房热线（中国）：</p>
            <p>电话：0000000000</p>
          </div>
          <div class="contact-group">
            <p>国际直拨电话：</p>
            <p>电话：+1 0000000000</p>
          </div>
          <a href="#" class="contact-link">查看免费预订热线</a>
        </div>
        
      </div>

        <div class="footer-copyright">
        ©2030 RIZE CARLTON | 沪ICP备********号 | 公安机关备案号: ********（实验所需，并非真实备案号）
      </div>
    </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { accountId, isManage, clearAuth } from '@/store/auth'
import request from '@/api/request'
import { Avatar, HelpFilled, UploadFilled, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

//（页面路由跳转）
const handleClick = (path) => {
  if (!router.resolve(path).matched.length) {
    ElMessage.info('该功能暂未开放')
    return
  }
  router.push(path)
}
const to = () => {
  if(isManage.value) {
    router.push('/employee')
  } else {
    router.push('/client')
  }
}

//（登录后的身份切换）
const switchAccount = () => {
  const target = isManage.value ? '/manageLog' : '/logIn'
  router.push(target)
}

const switchRole = () => {
  const target = isManage.value ? '/logIn' : '/manageLog'
  router.push(target)
}

//（房间卡片列表数据）
const cardList = ref([])

const FALLBACK_IMGS = ['/ex/1.png', '/ex/2.png', '/ex/3.png']

//（拉取房间并生成卡片数据）
const fetchRooms = async () => {
  try {
    const res = await request.get('/admin/rooms')
    const list = res.data || []
    cardList.value = list.map((room, index) => {
      const fallback = FALLBACK_IMGS[index % FALLBACK_IMGS.length]
      return {
        id: room.id,
        img: room.picture || fallback,
        title: room.categoryName,
        lines: [room.roomName, room.description].filter(Boolean),
        fallback
      }
    })
  } catch (err) {
    console.warn('获取房间列表失败：', err?.message || err)
  }
}

const onImgError = (item) => {
  item.img = item.fallback
}

const handleClick2 = (item) => {
  if (accountId.value) router.push(`/room/${item.id}`)
  else router.push('/logIn')
}
//（横向卡片滚动与拖拽）
const cardTrack = ref(null)
const canPrev = ref(false)
const canNext = ref(true)

const updateArrows = () => {
  const el = cardTrack.value
  if (!el) return
  canPrev.value = el.scrollLeft > 1
  canNext.value = el.scrollLeft + el.clientWidth < el.scrollWidth - 1
}

const scrollCards = (dir) => {
  const el = cardTrack.value
  if (!el) return
  const card = el.querySelector('.card')
  const step = card ? card.offsetWidth + 24 : el.clientWidth * 0.8
  el.scrollBy({ left: dir * step, behavior: 'smooth' })
}

let dragState = null
const DRAG_THRESHOLD = 5

const onPointerDown = (e) => {
  if (e.pointerType === 'touch') return
  const el = cardTrack.value
  dragState = { startX: e.clientX, startY: e.clientY, left: el.scrollLeft, id: e.pointerId, dragging: false }
}

const onPointerMove = (e) => {
  if (!dragState) return
  const dx = e.clientX - dragState.startX
  const dy = e.clientY - dragState.startY
  if (!dragState.dragging && Math.abs(dx) + Math.abs(dy) > DRAG_THRESHOLD) {
    dragState.dragging = true
    cardTrack.value.setPointerCapture(dragState.id)
    cardTrack.value.classList.add('dragging')
  }
  if (dragState.dragging) {
    cardTrack.value.scrollLeft = dragState.left - dx
  }
}

const onPointerUp = () => {
  if (!dragState) return
  if (dragState.dragging) {
    try { cardTrack.value.releasePointerCapture(dragState.id) } catch (err) {}
    cardTrack.value.classList.remove('dragging')
  }
  dragState = null
  updateArrows()
}

//（滚动驱动的顶部动画）
const welcomeOpacity = ref(1)
const iconOpacity = ref(1)
const lineScale = ref(0.1)

let ticking = false
const handleScroll = () => {
  if (ticking) return
  ticking = true
  requestAnimationFrame(() => {
    const y = window.scrollY

    lineScale.value = Math.min(1, 0.1 + (y / 220) * 0.9)

    welcomeOpacity.value = Math.max(0, 1 - y / 120)

    iconOpacity.value = Math.max(0, 1 - Math.max(0, y - 40) / 160)

    ticking = false
  })
}

onMounted(async () => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  window.addEventListener('resize', updateArrows)
  handleScroll()
  await fetchRooms()
  nextTick(updateArrows)
})
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', updateArrows)
})
</script>

<style scoped>
.bg-wrap {
  position: relative;
  width: 100%;
  min-height: 100vh;
}

.bg-fixed {
  position: fixed;
  inset: 0;
  background-image: url('/back.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 0;
}

.bg-mask {
  position: fixed;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.6) 0%,
    rgba(0, 0, 0, 0.1) 50%,
    rgba(0, 0, 0, 0.6) 100%
  );
  z-index: 1;
  pointer-events: none;
}

.home-entry {
  position: fixed;
  top: 20px;
  left: 24px;
  z-index: 20;
}

.user-bar {
  position: fixed;
  top: 20px;
  right: 24px;
  z-index: 20;
  display: flex;
  gap: 10px;
}

.user-btn {
  height: 34px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #fff;
  font-size: 13px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: background 0.25s ease, border-color 0.25s ease, transform 0.25s ease;
}

.user-btn:hover {
  background: rgba(255, 255, 255, 0.24);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-1px);
}

.head-bar {
  position: sticky;
  top: 0;
  z-index: 10;

  display: flex;
  flex-direction: column;
  align-items: center;
}

.full-width {
  display: block;
  width: 50%;
  height: auto;
  border-radius: 0 0 16px 16px;
}

.head-line {
  width: 100%;
  max-width: 640px;
  height: 1px;
  margin-top: 14px;
  background: rgba(255, 255, 255, 0.6);

  transform: scaleX(var(--line-scale, 0.1));
  transform-origin: center;
  transition: transform 0.1s linear;
  will-change: transform;
}

.welcome {
  position: relative;
  z-index: 2;
  margin: 0;
  padding: 40px 20px 28px;
  text-align: center;
  color: #fff;
  font-size: 28px;
  font-weight: 300;
  letter-spacing: 6px;
  line-height: 1.4;
  font-family: "Songti SC", "STSong", "SimSun", serif;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.45);
  animation: fadeUp 0.9s ease both;
}

@keyframes fadeUp {
  from { transform: translateY(24px); }
  to   { transform: translateY(0); }
}

.entry-group {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  gap: 56px;
  margin-top: 24px;
  animation: fadeUp 0.9s ease 0.2s both;
}

.entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  transition: transform 0.25s ease, color 0.25s ease;
}

.entry-item:hover {
  color: #fff;
  transform: translateY(-4px);
}

.entry-item .el-icon {
  padding: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  transition: background 0.25s ease, border-color 0.25s ease;
}

.entry-item:hover .el-icon {
  background: rgba(255, 255, 255, 0.22);
  border-color: rgba(255, 255, 255, 0.5);
}

.entry-item span {
  font-size: 14px;
  letter-spacing: 2px;
  font-family: "Songti SC", "STSong", "SimSun", serif;
}

.card-window {
  position: relative;
  z-index: 2;
  margin-top: 12px;
  animation: fadeUp 0.9s ease 0.4s both;
}

.card-list {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  justify-content: safe center;
  gap: 24px;
  padding: 40px 24px 60px;
  overflow-x: auto;
  overflow-y: hidden;
  overscroll-behavior-x: contain;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  cursor: grab;
  user-select: none;
}

.card-list::-webkit-scrollbar {
  display: none;
}

.card-list.dragging {
  cursor: grabbing;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  padding: 0;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: #fff;
  cursor: pointer;
  transition: opacity 0.25s ease, background 0.25s ease, transform 0.25s ease;
}

.nav-btn:hover {
  background: rgba(255, 255, 255, 0.26);
}

.nav-prev { left: 8px; }
.nav-next { right: 8px; }

.nav-btn.is-hidden {
  opacity: 0;
  pointer-events: none;
  transform: translateY(-50%) scale(0.9);
}

.card {
  flex: 0 0 260px;
  width: 260px;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px) saturate(140%);
  -webkit-backdrop-filter: blur(16px) saturate(140%);
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.18);
  transition: transform 0.3s ease, box-shadow 0.3s ease, background 0.3s ease;
}

.card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.14);
  box-shadow:
    0 16px 40px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.28);
}

.card-img {
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
}

.card-img img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: transform 0.5s ease;
}

.card:hover .card-img img {
  transform: scale(1.06);
}

.card-body {
  padding: 16px 18px 20px;
  color: #fff;
}

.card-title {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 2px;
  font-family: "Songti SC", "STSong", "SimSun", serif;
  text-shadow: 0 1px 6px rgba(0, 0, 0, 0.35);
}

.card-line {
  margin: 5px 0;
  font-size: 13px;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.5;
}

.glass-panel {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0;
  margin: 0 20px 60px;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(36px) saturate(160%);
  -webkit-backdrop-filter: blur(36px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.16);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.16);
  animation: fadeUp 0.9s ease 0.6s both;
}

.glass-item + .glass-item {
  border-left: 1px solid rgba(255, 255, 255, 0.12);
}

.glass-item {
  padding: 28px 24px;
  color: #fff;
  text-align: center;
}

.glass-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 3px;
  font-family: "Songti SC", "STSong", "SimSun", serif;
  text-shadow: 0 1px 6px rgba(0, 0, 0, 0.3);
}

.glass-text {
  max-width: 200px;
  margin: 0 auto;
  font-size: 13px;
  line-height: 1.7;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.8);
  text-align: justify;
}

@media (max-width: 100px) {
  .glass-panel {
    grid-template-columns: 1fr;
    margin: 0 16px 40px;
  }
  .glass-item + .glass-item {
    border-left: none;
    border-top: 1px solid rgba(255, 255, 255, 0.12);
  }
  .glass-item { padding: 22px 18px; }
}

@media (max-width: 1000px) {
  .full-width { width: 100%; }

  .welcome {
    font-size: 22px;
    letter-spacing: 4px;
    padding: 32px 16px 22px;
  }
  .entry-group {
    gap: 32px;
    margin-top: 18px;
  }
  .entry-item .el-icon { padding: 10px; }
  .entry-item span {
    font-size: 13px;
    letter-spacing: 1px;
  }
}
.footer {
  position: relative;
  z-index: 5;
  background-color: #061a2b;
  padding: 60px 8vw;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 40px;
}

.footer-brand {
  margin: 0;
  font-size: 42px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 1px;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
}

.footer-desc {
  flex: 1;
  max-width: 420px;
  margin: 0 40px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  line-height: 1.8;
}

.footer-desc p {
  margin: 0;
}

.footer-social {
  display: flex;
  align-items: center;
  gap: 12px;
}

.social-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  padding: 0 22px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 13px;
  letter-spacing: 2px;
  font-family: "Songti SC", "STSong", "SimSun", serif;
  transition:
    color 0.3s ease,
    background 0.3s ease,
    border-color 0.3s ease,
    transform 0.3s ease,
    box-shadow 0.3s ease;
}

.social-wechat {
  color: #8fe6ae;
  background: rgba(7, 193, 96, 0.12);
  border-color: rgba(7, 193, 96, 0.45);
}

.social-wechat:hover {
  color: #fff;
  background: #07c160;
  border-color: #07c160;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(7, 193, 96, 0.35);
}

.social-redbook {
  color: #ffa3ad;
  background: rgba(255, 36, 66, 0.12);
  border-color: rgba(255, 36, 66, 0.45);
}

.social-redbook:hover {
  color: #fff;
  background: #ff2442;
  border-color: #ff2442;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 36, 66, 0.35);
}

@media (max-width: 1000px) {
  .footer {
    flex-direction: column;
    align-items: flex-start;
    padding: 40px 20px;
    gap: 24px;
  }
  .footer-brand {
    font-size: 32px;
  }
  .footer-desc {
    margin: 0;
    font-size: 13px;
  }
}
.sub-footer {
  position: relative;
  z-index: 10;
  background-color: #f7f7f7;
  color: #222;
  padding: 80px 8vw 40px;
  font-family: "Helvetica Neue", Helvetica, Arial, "Microsoft YaHei", sans-serif;
}

.sub-footer-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  max-width: 1200px;
  margin: 0 auto;
  gap: 40px;
}

.nav-title {
  font-size: 22px;
  font-weight: 500;
  margin: 0 0 40px 0;
  color: #111;
  font-family: "Songti SC", "STSong", "SimSun", serif;
  letter-spacing: 1px;
}

.footer-nav {
  flex: 1;
}

.nav-links-grid {
  display: flex;
  gap: 120px;
}

.nav-col {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-col li {
  margin-bottom: 24px;
}

.nav-col a {
  color: #333;
  text-decoration: none;
  font-size: 15px;
  transition: color 0.3s ease;
}

.nav-col a:hover {
  color: #000;
  text-decoration: underline;
}

.footer-contact {
  flex: 1;
  max-width: 400px;
}

.contact-group {
  margin-bottom: 24px;
}

.contact-group p {
  margin: 0 0 6px 0;
  font-size: 15px;
  color: #333;
  line-height: 1.5;
}

.contact-link {
  display: inline-block;
  margin-top: 10px;
  color: #333;
  font-size: 15px;
  font-weight: 500;
  text-decoration: underline;
  text-underline-offset: 4px;
  transition: color 0.3s ease;
}

.contact-link:hover {
  color: #000;
}

.footer-copyright {
  max-width: 1200px;
  margin: 80px auto 0;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  font-size: 13px;
  color: #666;
}

@media (max-width: 1000px) {
  .sub-footer {
    padding: 50px 20px 30px;
  }
  
  .sub-footer-content {
    flex-direction: column;
    gap: 50px;
  }

  .nav-links-grid {
    gap: 60px;
  }

  .nav-title {
    margin-bottom: 24px;
  }

  .footer-contact {
    max-width: 100%;
  }

  .footer-copyright {
    margin-top: 50px;
    line-height: 1.6;
  }
}
</style>