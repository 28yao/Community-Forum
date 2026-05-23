import request from './request';

/** 获取启用版块列表 */
export function listBoards() {
  return request.get('/boards');
}

/** 获取版块详情 */
export function getBoardById(id) {
  return request.get(`/boards/${id}`);
}

/** 吧主修改板块信息（P2-M8）：description/icon/slogan/tags */
export function updateBoardOwner(id, data) {
  return request.patch(`/boards/${id}`, data);
}
