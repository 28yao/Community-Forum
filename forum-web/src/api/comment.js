import request from './request';

/** 查询某帖评论树（公开） */
export function listComments(postId) {
  return request.get(`/posts/${postId}/comments`);
}

/**
 * 发表评论
 * @param {Object} body { content, parentId?, replyToUserId? }
 */
export function createComment(postId, body) {
  return request.post(`/posts/${postId}/comments`, body);
}

/** 删除评论 */
export function deleteComment(commentId) {
  return request.delete(`/comments/${commentId}`);
}
