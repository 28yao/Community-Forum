import request from './request';

/** 获取用户公开资料 */
export function getUserById(id) {
  return request.get(`/users/${id}`);
}

/** 更新个人资料 */
export function updateProfile(data) {
  return request.put('/users/profile', data);
}

/** 上传头像 */
export function uploadAvatar(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/users/avatar', formData);
}
