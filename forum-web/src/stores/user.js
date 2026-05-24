import { defineStore } from 'pinia';
import { login as loginApi, logout as logoutApi } from '@/api/auth';

/**
 * 用户登录态 Store（M1-T19）
 *
 * - localStorage 持久化 token + info
 * - login / logout actions 调用后端接口
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    /** JWT token，未登录为 '' */
    token: localStorage.getItem('forum_token') || '',
    /** 当前用户信息：{ id, nickname, avatar, role, email } */
    info: JSON.parse(localStorage.getItem('forum_user') || 'null')
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    isAdmin: (s) => s.info?.role === 'admin'
  },
  actions: {
    /** 登录：调接口 → 存 state + localStorage */
    async login(email, password) {
      const data = await loginApi({ email, password });
      this.token = data.token;
      this.info = data.user;
      localStorage.setItem('forum_token', data.token);
      localStorage.setItem('forum_user', JSON.stringify(data.user));
      return data;
    },
    /** 登出：先清本地状态（立即生效），后端请求异步发出不阻塞 */
    logout() {
      this.clear();
      // 异步通知后端，不等待结果
      logoutApi().catch(() => {});
    },
    /** 清除登录态 */
    clear() {
      this.token = '';
      this.info = null;
      localStorage.removeItem('forum_token');
      localStorage.removeItem('forum_user');
    },
    /** 更新用户信息（编辑资料后调用） */
    updateInfo(patch) {
      this.info = { ...this.info, ...patch };
      localStorage.setItem('forum_user', JSON.stringify(this.info));
    }
  }
});
