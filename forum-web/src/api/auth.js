import request from './request';

/** 注册 */
export function register(data) {
  return request.post('/auth/register', data);
}

/** 邮箱验证 */
export function verifyEmail(token) {
  return request.post('/auth/verify-email', null, { params: { token } });
}

/** 重发验证邮件 */
export function resendVerification(email) {
  return request.post('/auth/resend-verification', null, { params: { email } });
}

/** 登录 */
export function login(data) {
  return request.post('/auth/login', data);
}

/** 登出 */
export function logout() {
  return request.post('/auth/logout');
}
