import request from './request';

/** 搜索帖子（兼容一期） */
export function searchPosts(q, params = {}) {
  return request.get('/search', { params: { q, type: 'post', ...params } });
}

/** 搜索板块（P2-M7） */
export function searchBoards(q, params = {}) {
  return request.get('/search', { params: { q, type: 'board', ...params } });
}

/** 搜索用户（P2-M7） */
export function searchUsers(q, params = {}) {
  return request.get('/search', { params: { q, type: 'user', ...params } });
}

/** 通用搜索（P2-M7，带 type + scope） */
export function search(q, params = {}) {
  return request.get('/search', { params: { q, ...params } });
}
