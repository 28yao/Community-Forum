import { createRouter, createWebHistory } from 'vue-router';
import { defineComponent, h } from 'vue';
import { useUserStore } from '@/stores/user';

// 占位组件：后续模块替换为真正的页面
const Placeholder = (name) => defineComponent({
  name,
  render() {
    return h('div', { class: 'placeholder-page' }, [
      h('h2', { style: 'margin:0 0 12px' }, name),
      h('p', { class: 'hint', style: 'color:#888' },
        `这是 ${name} 占位页。后续模块会替换为真实页面。`)
    ]);
  }
});

const routes = [
  { path: '/',          name: 'home',     component: () => import('@/views/Home.vue'),   meta: { title: '首页' } },
  { path: '/board/:id', name: 'board',    component: () => import('@/views/Board.vue'),  meta: { title: '版块' } },
  { path: '/post/create',   name: 'post-create', component: () => import('@/views/PostCreate.vue'), meta: { title: '发帖', requiresAuth: true } },
  { path: '/post/:id',      name: 'post-detail', component: () => import('@/views/PostDetail.vue'), meta: { title: '帖子详情' } },
  { path: '/post/:id/edit', name: 'post-edit',   component: () => import('@/views/PostEdit.vue'),   meta: { title: '编辑帖子', requiresAuth: true } },
  { path: '/login',     name: 'login',    component: () => import('@/views/Login.vue'),     meta: { title: '登录', guest: true } },
  { path: '/register',  name: 'register', component: () => import('@/views/Register.vue'),  meta: { title: '注册', guest: true } },
  { path: '/verify-email', name: 'verify-email', component: () => import('@/views/VerifyEmail.vue'), meta: { title: '邮箱验证' } },
  { path: '/settings',  name: 'settings', component: () => import('@/views/Settings.vue'),   meta: { title: '个人设置', requiresAuth: true } },
  // 兜底 404（M7-T6 会替换为 NotFound.vue 页面）
  { path: '/:pathMatch(.*)*', name: 'not-found', component: Placeholder('404 未找到'), meta: { title: '404' } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫（M1-T27）
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore();
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } });
  } else if (to.meta.guest && userStore.isLoggedIn) {
    next('/');
  } else {
    next();
  }
});

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 社区论坛系统`;
  }
});

export default router;
