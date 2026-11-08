import vue from '@vitejs/plugin-vue'
import path from 'path'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [vue()],
  //（路径别名 @ 指向 src 目录）
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    proxy: {
      //（本地后端代理，转发 /api 请求）
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // ⚠️ 新增：MinIO 图片代理，把 /minio 替换成 9000 端口
      '/minio': {
        target: 'http://localhost:9000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/minio/, ''), 
      },
    },
  },
})
