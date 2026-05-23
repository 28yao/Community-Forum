import request from './request';

/** 获取启用版块列表 */
export function listBoards() {
  return request.get('/boards');
}

/** 获取版块详情 */
export function getBoardById(id) {
  return request.get(`/boards/${id}`);
}
