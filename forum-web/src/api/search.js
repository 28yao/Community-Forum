import request from './request';

/** 关键词搜索帖子 */
export function searchPosts(q, params = {}) {
  return request.get('/search', { params: { q, ...params } });
}
