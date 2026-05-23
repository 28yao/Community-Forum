import request from './request';

/** 查申请资格 */
export function getEligibility() {
  return request.get('/board-applications/eligibility');
}

/** 提交板块申请 */
export function submitApplication(payload) {
  return request.post('/board-applications', payload);
}

/** 我的申请列表（分页） */
export function listMyApplications(page = 1, size = 20) {
  return request.get('/board-applications/mine', { params: { page, size } });
}

/** 管理端：审核列表 */
export function adminListApplications(status, page = 1, size = 20) {
  const params = { page, size };
  if (status != null) params.status = status;
  return request.get('/admin/board-applications', { params });
}

/** 管理端：审核通过 */
export function adminApproveApplication(id) {
  return request.post(`/admin/board-applications/${id}/approve`);
}

/** 管理端：审核驳回 */
export function adminRejectApplication(id, rejectReason) {
  return request.post(`/admin/board-applications/${id}/reject`, { rejectReason });
}
