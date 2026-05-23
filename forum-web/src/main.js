import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import App from './App.vue';
import router from './router';
import './styles/global.css';

const app = createApp(App);

// 全量注册 Element Plus（中文 locale）
app.use(ElementPlus, { locale: zhCn });

// 注册所有图标组件（用法：<el-icon><User /></el-icon>）
for (const [name, icon] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, icon);
}

app.use(createPinia());
app.use(router);
app.mount('#app');
