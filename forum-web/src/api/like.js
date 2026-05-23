import request from './request';

/** 点赞 */
export function like(postId) {
  return request.post(`/posts/${postId}/like`);
}

/** 取消点赞 */
export function unlike(postId) {
  return request.delete(`/posts/${postId}/like`);
}
