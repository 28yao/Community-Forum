import { defineStore } from 'pinia';

/**
 * 全局应用 Store
 *
 * 缓存版块列表等不频繁变化的数据，避免组件切换时重复请求。
 * M2-T12 会扩展真实的版块加载逻辑。
 */
export const useAppStore = defineStore('app', {
  state: () => ({
    /** 版块列表 [{ id, name, description, postCount }] */
    boards: [],
    /** 站点设置（二期实现） */
    siteSettings: {}
  }),
  actions: {
    setBoards(list) {
      this.boards = Array.isArray(list) ? list : [];
    }
  }
});
