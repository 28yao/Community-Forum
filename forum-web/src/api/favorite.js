import request from './request';

/** 收藏 */
export function favorite(postId) {
  return request.post(`/posts/${postId}/favorite`);
}

/** 取消收藏 */
export function unfavorite(postId) {
  return request.delete(`/posts/${postId}/favorite`);
}
