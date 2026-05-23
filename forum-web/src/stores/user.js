import { defineStore } from 'pinia';

/**
 * 用户登录态 Store（基础骨架）
 *
 * M1-T19 任务将扩展：
 *  - persistedstate 持久化到 localStorage
 *  - 真实 login / logout actions（调 api/auth.js）
 *  - 自动加载用户资料
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    /** JWT token，未登录为 '' */
    token: '',
    /** 当前用户信息：{ id, nickname, avatar, role, ... } */
    info: null
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    isAdmin: (s) => s.info?.role === 'admin'
  },
  actions: {
    setToken(token) {
      this.token = token || '';
    },
    setInfo(info) {
      this.info = info || null;
    },
    clear() {
      this.token = '';
      this.info = null;
    }
  }
});
