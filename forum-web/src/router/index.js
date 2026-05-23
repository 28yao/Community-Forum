import { createRouter, createWebHistory } from 'vue-router';
import { defineComponent, h } from 'vue';
import { ElMessage } from 'element-plus';
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
  { path: '/search',    name: 'search',   component: () => import('@/views/Search.vue'),     meta: { title: '搜索' } },

  // 后台（M6）
  { path: '/admin/login', name: 'admin-login', component: () => import('@/views/admin/AdminLogin.vue'),
    meta: { title: '后台登录', adminLayout: true } },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { adminOnly: true, adminLayout: true },
    children: [
      { path: '',          name: 'admin-dashboard', component: () => import('@/views/admin/AdminDashboard.vue'), meta: { title: '后台总览' } },
      { path: 'users',     name: 'admin-users',     component: () => import('@/views/admin/AdminUsers.vue'),     meta: { title: '用户管理' } },
      { path: 'boards',    name: 'admin-boards',    component: () => import('@/views/admin/AdminBoards.vue'),    meta: { title: '版块管理' } },
      { path: 'posts',     name: 'admin-posts',     component: () => import('@/views/admin/AdminPosts.vue'),     meta: { title: '帖子管理' } },
      { path: 'comments',  name: 'admin-comments',  component: () => import('@/views/admin/AdminComments.vue'),  meta: { title: '评论管理' } }
    ]
  },

  // 兜底 404
  { path: '/:pathMatch(.*)*', name: 'not-found', component: Placeholder('404 未找到'), meta: { title: '404' } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫（M1-T27 + M6-T20）
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore();
  // 后台守卫：要求 admin
  if (to.meta.adminOnly) {
    if (!userStore.isLoggedIn) {
      return next({ path: '/admin/login' });
    }
    if (!userStore.isAdmin) {
      ElMessage.warning('需要管理员权限');
      return next({ path: '/' });
    }
    return next();
  }
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return next({ path: '/login', query: { redirect: to.fullPath } });
  }
  if (to.meta.guest && userStore.isLoggedIn) {
    return next('/');
  }
  next();
});

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 社区论坛系统`;
  }
});

export default router;
