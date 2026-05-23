import { createRouter, createWebHistory } from 'vue-router';
import { defineComponent, h } from 'vue';

// 占位组件：后续模块替换为真正的页面
const Placeholder = (name) => defineComponent({
  name,
  render() {
    return h('div', { class: 'placeholder-page' }, [
      h('h2', { style: 'margin:0 0 12px' }, name),
      h('p', { class: 'hint', style: 'color:#888' },
        `这是 ${name} 占位页。M0-T15 路由可达性已验证；后续任务会替换为真实页面。`)
    ]);
  }
});

// 占位路由：后续 M2-T11 (Home), M1-T22~T25 (Auth pages), M3 / M4 / M6 等模块将注册真实路由。
const routes = [
  { path: '/',          name: 'home',     component: Placeholder('首页'),   meta: { title: '首页' } },
  { path: '/login',     name: 'login',    component: Placeholder('登录'),   meta: { title: '登录' } },
  { path: '/register',  name: 'register', component: Placeholder('注册'),   meta: { title: '注册' } },
  // 兜底 404（M7-T6 会替换为 NotFound.vue 页面）
  { path: '/:pathMatch(.*)*', name: 'not-found', component: Placeholder('404 未找到'), meta: { title: '404' } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 社区论坛系统`;
  }
});

export default router;
