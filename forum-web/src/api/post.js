import request from './request';

/** 帖子列表 */
export function listPosts(params = {}) {
  return request.get('/posts', { params });
}

/** 帖子详情 */
export function getPostById(id) {
  return request.get(`/posts/${id}`);
}

/** 发帖 */
export function createPost(data) {
  return request.post('/posts', data);
}

/** 编辑帖子 */
export function updatePost(id, data) {
  return request.put(`/posts/${id}`, data);
}

/** 删除帖子 */
export function deletePost(id) {
  return request.delete(`/posts/${id}`);
}

/** 上传帖子图片 */
export function uploadPostImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/upload/image', formData);
}
