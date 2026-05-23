import { defineStore } from 'pinia';

/**
 * 发帖弹窗状态（P2-M6）
 *
 * 控制 PostEditorModal 的显示/隐藏 + 板块锁定。
 */
export const usePostEditorStore = defineStore('postEditor', {
  state: () => ({
    visible: false,
    /** 锁定板块 ID（板块页发帖时使用，null 表示可选） */
    lockedBoardId: null
  }),
  actions: {
    /**
     * 打开发帖弹窗
     * @param {Object} opts
     * @param {number} [opts.boardId] - 锁定板块 ID
     */
    open(opts = {}) {
      this.lockedBoardId = opts.boardId || null;
      this.visible = true;
    },
    close() {
      this.visible = false;
      this.lockedBoardId = null;
    }
  }
});
