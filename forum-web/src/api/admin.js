import request from './request';

/** ===== Auth ===== */
export function adminLogin(body) {
  return request.post('/admin/auth/login', body);
}

/** ===== Users ===== */
export function adminListUsers(params) {
  return request.get('/admin/users', { params });
}
export function adminGetUser(id) {
  return request.get(`/admin/users/${id}`);
}
export function adminBanUser(id, reason) {
  return request.post(`/admin/users/${id}/ban`, { reason });
}
export function adminUnbanUser(id) {
  return request.post(`/admin/users/${id}/unban`);
}
export function adminResetPassword(id, newPassword) {
  return request.post(`/admin/users/${id}/reset-password`, { newPassword });
}

/** ===== Boards ===== */
export function adminListBoards() {
  return request.get('/admin/boards');
}
export function adminCreateBoard(body) {
  return request.post('/admin/boards', body);
}
export function adminUpdateBoard(id, body) {
  return request.put(`/admin/boards/${id}`, body);
}
export function adminSetBoardStatus(id, status) {
  return request.post(`/admin/boards/${id}/status`, { status });
}
export function adminTransferOwner(id, newOwnerId) {
  return request.post(`/admin/boards/${id}/transfer-owner`, { newOwnerId });
}

/** ===== Posts ===== */
export function adminListPosts(params) {
  return request.get('/admin/posts', { params });
}
export function adminDeletePost(id) {
  return request.delete(`/admin/posts/${id}`);
}
export function adminRestorePost(id) {
  return request.post(`/admin/posts/${id}/restore`);
}
export function adminPinPost(id, pinned) {
  return request.post(`/admin/posts/${id}/pin`, { pinned });
}

/** ===== Comments ===== */
export function adminListComments(params) {
  return request.get('/admin/comments', { params });
}
export function adminDeleteComment(id) {
  return request.delete(`/admin/comments/${id}`);
}
export function adminRestoreComment(id) {
  return request.post(`/admin/comments/${id}/restore`);
}
