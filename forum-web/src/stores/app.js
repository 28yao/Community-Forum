import { defineStore } from 'pinia';
import { listBoards } from '@/api/board';

/**
 * 全局应用 Store
 *
 * 缓存版块列表，避免组件切换时重复请求。
 */
export const useAppStore = defineStore('app', {
  state: () => ({
    /** 版块列表 [{ id, name, description, postCount }] */
    boards: [],
    /** 是否已加载过版块 */
    boardsLoaded: false,
    /** 站点设置（二期实现） */
    siteSettings: {}
  }),
  actions: {
    setBoards(list) {
      this.boards = Array.isArray(list) ? list : [];
      this.boardsLoaded = true;
    },
    /** 加载版块列表（首次/强制刷新） */
    async loadBoards(force = false) {
      if (this.boardsLoaded && !force) return this.boards;
      try {
        const list = await listBoards();
        this.setBoards(list);
      } catch (e) {
        // 加载失败保留空数组
        this.boards = [];
      }
      return this.boards;
    }
  }
});
