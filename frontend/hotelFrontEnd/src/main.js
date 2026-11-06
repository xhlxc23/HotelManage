import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import axios from 'axios'
import router from './router'

//（应用入口：注册 Element Plus 与路由后挂载）
createApp(App).use(ElementPlus).use(router).mount('#app')
