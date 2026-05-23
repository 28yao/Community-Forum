import request from './request';

/** 关注板块（幂等） */
export function followBoard(boardId) {
  return request.post(`/boards/${boardId}/follow`);
}

/** 取消关注板块（幂等） */
export function unfollowBoard(boardId) {
  return request.delete(`/boards/${boardId}/follow`);
}

/** 我关注的板块列表（需登录） */
export function listFollowedBoards() {
  return request.get('/boards/followed');
}

/** 推荐板块（公开，按 follower_count + post_count 排序） */
export function listRecommendedBoards(limit = 10) {
  return request.get('/boards/recommended', { params: { limit } });
}
