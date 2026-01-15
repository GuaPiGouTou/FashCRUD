import { createApp } from 'vue'
import ElementPlus from 'element-plus' // 1. 引入库
import 'element-plus/dist/index.css'   // 2. ⚠️ 必须引入这行 CSS！
import App from './App.vue'
import SmartGridView from './views/SmartGridView.vue'
import ModuleDashboard from './views/ModuleDashboard.vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue' // 引入图标
import './style.css'

const app = createApp(App)

app.use(ElementPlus)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')