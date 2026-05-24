import { defineStore } from 'pinia';

/**
 * 发帖/编辑弹窗状态（P2-M6）
 *
 * 控制 PostEditorModal 的显示/隐藏 + 板块锁定 + 编辑模式。
 */
export const usePostEditorStore = defineStore('postEditor', {
  state: () => ({
    visible: false,
    /** 锁定板块 ID（板块页发帖 / 编辑时使用，null 表示可选） */
    lockedBoardId: null,
    /** 编辑模式下的帖子 ID，null 表示发帖 */
    editingPostId: null,
    /** 编辑模式预填数据（避免重复请求） */
    editDraft: null,
    /** 保存成功后的回调（如详情页刷新） */
    onSaved: null
  }),
  actions: {
    /**
     * 打开发帖弹窗
     * @param {Object} opts
     * @param {number} [opts.boardId] - 锁定板块 ID
     */
    open(opts = {}) {
      this.editingPostId = null;
      this.editDraft = null;
      this.onSaved = null;
      this.lockedBoardId = opts.boardId || null;
      this.visible = true;
    },
    /**
     * 打开编辑弹窗（与发帖共用 PostEditorModal）
     * @param {Object} post - { id, boardId, title, content, images }
     * @param {Function} [onSaved] - 保存成功后回调
     */
    openForEdit(post, onSaved) {
      this.editingPostId = post.id;
      this.editDraft = {
        boardId: post.boardId,
        title: post.title,
        content: post.content,
        images: post.images || []
      };
      this.onSaved = onSaved || null;
      this.lockedBoardId = post.boardId || null;
      this.visible = true;
    },
    close() {
      this.visible = false;
      this.lockedBoardId = null;
      this.editingPostId = null;
      this.editDraft = null;
      this.onSaved = null;
    }
  }
});
