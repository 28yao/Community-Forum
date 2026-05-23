import { defineStore } from 'pinia';
import {
  followBoard,
  unfollowBoard,
  listFollowedBoards,
  listRecommendedBoards
} from '@/api/boardFollow';

/**
 * 板块关注 Store（P2-M3）
 *
 * 状态：
 * - followedIds: Set<number> 当前用户已关注的 board_id 集合
 * - followedBoards: Board[] 完整板块列表（含名/icon/计数），左栏渲染用
 * - recommendedBoards: Board[] 推荐板块（左栏 + 首页用）
 * - loaded: 标记是否已首次加载过，避免重复请求
 *
 * 写操作（follow/unfollow）走乐观更新 + 失败回滚。
 */
export const useBoardFollowStore = defineStore('boardFollow', {
  state: () => ({
    followedIds: new Set(),
    followedBoards: [],
    recommendedBoards: [],
    followedLoaded: false,
    recommendedLoaded: false
  }),
  getters: {
    isFollowed: (s) => (boardId) => s.followedIds.has(Number(boardId)),
    followedCount: (s) => s.followedIds.size
  },
  actions: {
    /** 加载关注列表（登录后调用） */
    async loadFollowed(force = false) {
      if (this.followedLoaded && !force) return this.followedBoards;
      const list = await listFollowedBoards();
      this.followedBoards = list || [];
      this.followedIds = new Set((list || []).map((b) => Number(b.id)));
      this.followedLoaded = true;
      return this.followedBoards;
    },

    /** 加载推荐板块（公开，按需 force 刷新） */
    async loadRecommended(force = false, limit = 10) {
      if (this.recommendedLoaded && !force) return this.recommendedBoards;
      const list = await listRecommendedBoards(limit);
      this.recommendedBoards = list || [];
      this.recommendedLoaded = true;
      return this.recommendedBoards;
    },

    /**
     * 关注板块（乐观更新）。返回 Promise，失败时自动回滚。
     * @param {object} board - 完整 board 对象，用于乐观加入 followedBoards
     */
    async follow(board) {
      const id = Number(board.id);
      if (this.followedIds.has(id)) return;
      // 乐观：先加入
      this.followedIds.add(id);
      this.followedBoards = [{ ...board }, ...this.followedBoards];
      try {
        await followBoard(id);
      } catch (e) {
        // 回滚
        this.followedIds.delete(id);
        this.followedBoards = this.followedBoards.filter((b) => Number(b.id) !== id);
        throw e;
      }
    },

    /** 取消关注（乐观更新） */
    async unfollow(boardId) {
      const id = Number(boardId);
      if (!this.followedIds.has(id)) return;
      const oldBoard = this.followedBoards.find((b) => Number(b.id) === id);
      this.followedIds.delete(id);
      this.followedBoards = this.followedBoards.filter((b) => Number(b.id) !== id);
      try {
        await unfollowBoard(id);
      } catch (e) {
        // 回滚
        this.followedIds.add(id);
        if (oldBoard) this.followedBoards = [oldBoard, ...this.followedBoards];
        throw e;
      }
    },

    /** 登出时清空 */
    clear() {
      this.followedIds = new Set();
      this.followedBoards = [];
      this.followedLoaded = false;
      // 推荐板块不清，匿名仍可看
    }
  }
});
