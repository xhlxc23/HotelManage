<template>
  <div class="vr-wrapper">
    <div class="bg-layer" aria-hidden="true"></div>
    <div class="vr-content">
      <div class="nav-header">
        <el-button link @click="goBack" class="back-btn">
          <el-icon>
            <ArrowLeft />
          </el-icon> 返回
        </el-button>
      </div>

      <!-- 全景卡片 -->
      <div class="vr-card">
        <!-- 加载中动画 -->
        <div v-if="loading" class="state-mask loading-mask">
          <div class="spinner"></div>
          <p>VR 全景加载中...</p>
        </div>

        <!-- 异常提示（比如该房型未配置 VR） -->
        <div v-else-if="errorMsg" class="state-mask error-mask">
          <el-icon class="error-icon">
            <WarningFilled />
          </el-icon>
          <p>{{ errorMsg }}</p>
        </div>

        <!-- 全景图容器 -->
        <div v-if="!loading && !errorMsg" ref="viewerContainer" class="viewer-box"></div>

        <!-- 左上角悬浮信息面板 -->
        <div v-if="!loading && !errorMsg && currentNodeName" class="info-panel">
          <h2>{{ currentNodeName }}</h2>
          <p>拖动鼠标环顾四周，点击地上的箭头切换房间</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, WarningFilled } from '@element-plus/icons-vue';
import request from '@/api/request';
import { Viewer } from '@photo-sphere-viewer/core';
import { VirtualTourPlugin } from '@photo-sphere-viewer/virtual-tour-plugin';

// 必须引入样式
import '@photo-sphere-viewer/core/index.css';
import '@photo-sphere-viewer/virtual-tour-plugin/index.css';

// 从路由参数获取房型 ID（路由：/vr/:categoryId）
const route = useRoute();
const router = useRouter();
const categoryId = route.params.categoryId;

const viewerContainer = ref(null);
const loading = ref(true);
const errorMsg = ref('');
const currentNodeName = ref('');
let viewer = null;

const goBack = () => {
  router.back();
};

onMounted(async () => {
  if (!categoryId) {
    loading.value = false;
    errorMsg.value = '缺少房型参数，无法加载 VR 全景';
    return;
  }

  try {
    // 请求后端 API
    const res = await request.get('/vr', { params: { categoryId } });
    const tourData = res.data;

    if (!tourData || !tourData.nodes || tourData.nodes.length === 0) {
      throw new Error('该房型暂未配置 VR 全景图');
    }

  tourData.nodes.forEach(node => {
      if (node.panorama && node.panorama.includes('localhost:9000')) {
        node.panorama = node.panorama.replace('http://localhost:9000', '/minio');
      }
    });
    loading.value = false;
    await nextTick();
    initViewer(tourData);
  } catch (error) {
    console.error('VR加载失败:', error);
    loading.value = false;
    errorMsg.value = error.message || 'VR 数据加载失败';
  }
});

// const initViewer = (tourData) => {
// console.log(
//   '完整节点数据：',
//   JSON.stringify(tourData.nodes, null, 2)
// );

//   // 初始化 Viewer
//   viewer = new Viewer({
//     container: viewerContainer.value,
//     panorama: tourData.nodes.find(n => n.id === tourData.startNodeId)?.panorama,

//     navbar: ['zoom', 'fullscreen'],
//     plugins: [
//       [VirtualTourPlugin, {
//         nodes: tourData.nodes,
//         startNodeId: tourData.startNodeId,
//         renderMode: '3d',
//         transitionOptions: {
//           effect: 'fade',
//           speed: '10rpm'
//         }
//       }]
//     ]
//   });

//   // ⚠️ 调试监听：查看图片是否真的在加载
//   viewer.addEventListener('ready', () => {
//     console.log('✅ PSV 初始化成功，第一张全景图已加载完毕');
//   });

//   viewer.addEventListener('error', (e) => {
//     console.error('❌ PSV 加载图片失败：', e.error);
//   });

//   // 获取插件实例，监听房间切换事件
//   const tourPlugin = viewer.getPlugin(VirtualTourPlugin);

//   const startNode = tourData.nodes.find(n => n.id === tourData.startNodeId);
//   if (startNode) currentNodeName.value = startNode.name;

//   tourPlugin.addEventListener('node-changed', ({ node }) => {
//     currentNodeName.value = node.name;
//     console.log(`🚪 切换房间: ${node.name}`);
//   });
// };
const initViewer = (tourData) => {
  if (!viewerContainer.value) {
    console.error('找不到 viewerContainer 容器 DOM！');
    return;
  }
  const startNode = tourData.nodes.find(
    n => n.id === tourData.startNodeId
  );

  if (!startNode) {
    console.error('找不到起始节点！');
    return;
  }

  console.log('开始初始化 VR 全景');
  console.log('起始节点：', startNode);

  try {
    // 初始化 Viewer
    viewer = new Viewer({
      container: viewerContainer.value,

      navbar: ['zoom', 'fullscreen'],

      plugins: [
        VirtualTourPlugin.withConfig({
          nodes: tourData.nodes,

          startNodeId: tourData.startNodeId,

          renderMode: '3d',

          transitionOptions: {
            effect: 'fade',
            speed: '10rpm',
            rotation: true,
            showLoader: true
          }
        })
      ]
    });

    // Viewer 初始化完成
    viewer.addEventListener('ready', () => {
      console.log('VR Viewer 初始化成功');
    });

    // Viewer 加载异常
    viewer.addEventListener('error', (e) => {
      console.error('VR Viewer 加载失败：', e);
    });

    // 获取虚拟漫游插件
    const tourPlugin = viewer.getPlugin(VirtualTourPlugin);

    // 设置初始房间名称
    currentNodeName.value = startNode.name;

    // 监听房间切换
    tourPlugin.addEventListener(
      'node-changed',
      ({ node }) => {
        currentNodeName.value = node.name;

        console.log('🚪 当前房间：', node.name);
      }
    );

    console.log('VirtualTourPlugin 注册成功');

  } catch (error) {
    console.error('VR Viewer 初始化异常：', error);
  }
};
onBeforeUnmount(() => {
  if (viewer) {
    viewer.destroy();
    viewer = null;
  }
});
</script>

<style scoped>
/* 保持你原本的样式不变 */
.vr-wrapper {
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

.vr-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1100px;
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

.vr-card {
  position: relative;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(24px) saturate(160%);
  -webkit-backdrop-filter: blur(24px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(6, 26, 43, 0.12);
  aspect-ratio: 16 / 9;
  min-height: 520px;
}

@supports not ((backdrop-filter: blur(1px)) or (-webkit-backdrop-filter: blur(1px))) {
  .vr-card {
    background: rgba(255, 255, 255, 0.92);
  }
}

.viewer-box {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  border-radius: 24px;
  overflow: hidden;
}

.info-panel {
  position: absolute;
  top: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: #061a2b;
  padding: 14px 22px;
  border-radius: 16px;
  pointer-events: none;
  z-index: 10;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 16px rgba(6, 26, 43, 0.1);
}

.info-panel h2 {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 600;
  font-family: "Songti SC", "STSong", "SimSun", serif;
}

.info-panel p {
  margin: 0;
  font-size: 13px;
  color: #5d7691;
}

.state-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 20;
  background: rgba(246, 251, 255, 0.6);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.loading-mask p {
  color: #409eff;
  font-size: 15px;
  margin: 0;
}

.error-mask p {
  color: #f56c6c;
  font-size: 16px;
  margin: 12px 0 0 0;
}

.error-icon {
  font-size: 42px;
  color: #f56c6c;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(64, 158, 255, 0.2);
  border-top-color: #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .vr-content {
    padding: 12px;
  }

  .vr-card {
    min-height: 380px;
    border-radius: 16px;
  }

  .info-panel {
    top: 12px;
    left: 12px;
    padding: 10px 16px;
  }

  .info-panel h2 {
    font-size: 16px;
  }
}
</style>