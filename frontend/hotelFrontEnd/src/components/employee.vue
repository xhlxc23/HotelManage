<template>
  <div class="common-layout">
    <el-container class="layout-container">
      
      <el-header class="layout-header">
        <div class="header-right" v-if="accountId">
          <button class="header-btn" @click="handleSwitchRole">切换客户</button>
          <button class="header-btn header-btn--danger" @click="handleLogout">退出</button>
        </div>
      </el-header>

      <el-container class="layout-body">
        <el-aside
          :width="isCollapse ? '64px' : '220px'"
          class="layout-aside"
          :class="{ 'is-collapse': isCollapse }"
        >
          <div class="aside-toggle">
            <el-icon class="collapse-btn" @click="toggleCollapse">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
          </div>

          <el-menu
            :default-active="route.path"
            :collapse="isCollapse"
            :collapse-transition="false"
            router
            background-color="transparent"
            text-color="rgba(255,255,255,0.75)"
            active-text-color="#ffffff"
            class="aside-menu"
          >
            <el-menu-item index="/employee/adminInfo">
              <el-icon><User /></el-icon>
              <template #title>个人中心</template>
            </el-menu-item>
            <el-menu-item index="/employee/roomOrder">
              <el-icon><House /></el-icon>
              <template #title>预定房间</template>
            </el-menu-item>
            
            <el-menu-item index="/employee/selectOrder">
              <el-icon><List /></el-icon>
              <template #title>订单管理</template>
            </el-menu-item>
            <el-menu-item index="/employee/roomAndCat">
              <el-icon><List /></el-icon>
              <template #title>房间管理</template>
            </el-menu-item>
            <el-menu-item index="/employee/allAccount">
              <el-icon><List /></el-icon>
              <template #title>客户管理</template>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main class="layout-main">
          <div class="main-content">
            <router-view v-slot="{ Component }">
              <transition name="fade-transform" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { accountId, isManage, clearAuth } from '@/store/auth'
import { Expand, Fold, User, House, List } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

//（路由实例）
const route = useRoute()
const router = useRouter()

//（侧边栏折叠状态）
const isCollapse = ref(false)
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

//（切换为客户角色）
const handleSwitchRole = () => {
  router.push('/logIn')
}

//（退出登录）
const handleLogout = () => {
  clearAuth()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.common-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #f5f6f8;
}

.layout-container {
  height: 100%;
}

.layout-header {
  position: relative;
  z-index: 10;
  height: 150px !important;
  flex-shrink: 0;
  padding: 0 24px !important;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  background: #061a2b url('/client.png') center / cover no-repeat;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.22);
}

.layout-header::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(6, 26, 43, 0.2) 0%,
    rgba(6, 26, 43, 0.55) 100%
  );
  pointer-events: none;
  z-index: 0;
}

.header-right {
  position: relative;
  z-index: 1;
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  cursor: pointer;
  padding: 9px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 0 14px rgba(0, 0, 0, 0.28);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  transition: background 0.25s ease, transform 0.25s ease, box-shadow 0.25s ease;
}
.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.24);
  box-shadow: 0 0 18px rgba(0, 0, 0, 0.35);
  transform: translateY(-1px);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #fff;
}

.user-role {
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.25);
  padding: 4px 12px;
  border-radius: 999px;
  color: #fff;
  letter-spacing: 1px;
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.user-name {
  color: #fff;
  font-weight: 600;
  letter-spacing: 1px;
}

.header-btn {
  height: 34px;
  padding: 0 18px;
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

.header-btn:hover {
  background: rgba(255, 255, 255, 0.24);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-1px);
}

.header-btn--danger {
  border-color: rgba(255, 120, 130, 0.55);
  color: #ffbcc2;
}

.header-btn--danger:hover {
  background: rgba(255, 36, 66, 0.85);
  border-color: rgba(255, 36, 66, 0.85);
  color: #fff;
}

.layout-body {
  flex: 1;
  min-height: 0;
}

.layout-aside {
  display: flex;
  flex-direction: column;
  background:
    linear-gradient(
      180deg,
      rgba(6, 26, 43, 0.55) 0%,
      rgba(6, 26, 43, 0.72) 45%,
      rgba(6, 26, 43, 0.9) 100%
    ),
    #061a2b url('/side.png') center bottom / cover no-repeat;
  transition: width 0.3s ease;
  overflow: hidden;
  border-right: none;
  scrollbar-width: none;
}

:deep(.aside-menu) {
  background-color: transparent !important;
}

.layout-aside::-webkit-scrollbar {
  display: none;
}

.aside-toggle {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64px;
  padding: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.aside-menu {
  flex: 1;
  min-height: 0;
  border-right: none;
  overflow: hidden;
}

.aside-menu::-webkit-scrollbar {
  display: none;
}

:deep(.el-menu-item) {
  margin: 8px 12px;
  border-radius: 8px;
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.08) !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #0d2d48 !important;
  font-weight: 600;
}

:deep(.el-menu--collapse .el-menu-item) {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 8px 10px !important;
  padding: 0 !important;
  width: 44px;
  height: 50px;
  line-height: 1;
}

:deep(.el-menu--collapse .el-menu-item .el-icon) {
  margin: 0 !important;
  font-size: 18px;
}

.layout-main {
  height: 100%;
  padding: 24px;
  background-color: #f5f6f8;
  overflow-y: auto;
}

.main-content {
  background:
    linear-gradient(
      rgba(6, 26, 43, 0.1) 0%,
      rgba(6, 26, 43, 0.8) 100%
    ),
    #061a2b url('/center.png') center / cover no-repeat;
  border-radius: 12px;
  padding: 24px;
  min-height: 100%;
  color: #eaf2f9;
  box-shadow: 0 10px 30px rgba(6, 26, 43, 0.16);
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
:deep(.aside-menu.el-menu--collapse) {
  width: 64px !important;
}

:deep(.el-menu--collapse .el-menu-item) {
  position: relative !important;
  display: block !important;

  width: 44px !important;
  height: 50px;
  margin: 8px 10px !important;
  padding: 0 !important;

  box-sizing: border-box;
}

:deep(.el-menu--collapse .el-menu-item .el-icon) {
  position: absolute !important;
  left: 50% !important;
  top: 50% !important;

  transform: translate(-50%, -50%) !important;

  display: flex !important;
  align-items: center !important;
  justify-content: center !important;

  width: 20px;
  height: 20px;
  margin: 0 !important;
  padding: 0 !important;
  font-size: 20px;
}
</style>
