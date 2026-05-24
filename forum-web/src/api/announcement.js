import request from './request';

/** 站点公告列表（前台） */
export function listSiteAnnouncements(params = {}) {
  return request.get('/announcements', { params: { scope: 'site', ...params } });
}

/** 板块公告列表（前台） */
export function listBoardAnnouncements(boardId, params = {}) {
  return request.get(`/boards/${boardId}/announcements`, { params });
}

/** 吧主发板块公告 */
export function createBoardAnnouncement(boardId, data) {
  return request.post(`/boards/${boardId}/announcements`, data);
}

/** 编辑公告（吧主/管理员，前台端点） */
export function updateAnnouncement(id, data) {
  return request.put(`/announcements/${id}`, data);
}

/** 删除公告（吧主/管理员，前台端点） */
export function deleteAnnouncement(id) {
  return request.delete(`/announcements/${id}`);
}

// ========== 后台管理（admin） ==========

/** 后台公告列表 */
export function adminListAnnouncements(params = {}) {
  return request.get('/admin/announcements', { params });
}

/** 后台创建公告 */
export function adminCreateAnnouncement(data) {
  return request.post('/admin/announcements', data);
}

/** 后台编辑公告 */
export function adminUpdateAnnouncement(id, data) {
  return request.put(`/admin/announcements/${id}`, data);
}

/** 后台删除公告 */
export function adminDeleteAnnouncement(id) {
  return request.delete(`/admin/announcements/${id}`);
}
