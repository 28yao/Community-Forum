import axios from 'axios';
import { useUserStore } from '@/stores/user';

/**
 * 统一封装的 Axios 实例
 *
 * - baseURL：dev 走 Vite 代理，prod 用 VITE_API_BASE_URL
 * - 请求拦截：自动注入 Authorization: Bearer <token>
 * - 响应拦截：自动展开后端统一响应体；code=1001 自动登出；其余错误抛出 BizError
 */
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

const request = axios.create({
  baseURL,
  timeout: 15000
});

// ===== 请求拦截：注入 Token =====
request.interceptors.request.use((config) => {
  try {
    const user = useUserStore();
    if (user.token) {
      config.headers.Authorization = `Bearer ${user.token}`;
    }
  } catch (e) {
    // Pinia 未初始化（极少见，例如在 store 构造期间）。忽略，让请求继续。
  }
  return config;
}, (err) => Promise.reject(err));

// ===== 响应拦截：解包统一响应体 =====
request.interceptors.response.use((resp) => {
  const data = resp.data;
  // 后端返回 { code, message, data }
  if (data && typeof data === 'object' && 'code' in data) {
    if (data.code === 0) {
      return data.data;          // 成功：直接给业务 data
    }
    // 未登录：清登录态。由各业务页面或路由守卫去跳 /login。
    if (data.code === 1001) {
      try {
        useUserStore().clear();
      } catch (e) { /* ignore */ }
    }
    // 业务错误抛给调用方 catch
    const err = new BizError(data.code, data.message || '请求失败');
    return Promise.reject(err);
  }
  // 非标准响应：直接返回原始
  return data;
}, (err) => {
  // 网络/超时/HTTP 5xx
  const status = err?.response?.status;
  const respData = err?.response?.data;
  if (respData && typeof respData === 'object' && 'code' in respData) {
    return Promise.reject(new BizError(respData.code, respData.message || '请求失败', status));
  }
  if (err?.code === 'ECONNABORTED') {
    return Promise.reject(new BizError(9999, '请求超时，请重试', status));
  }
  return Promise.reject(new BizError(9999, '网络异常，请稍后再试', status));
});

/**
 * 业务错误：携带后端 code 与 message，便于 UI 层友好提示。
 */
export class BizError extends Error {
  constructor(code, message, httpStatus) {
    super(message);
    this.name = 'BizError';
    this.code = code;
    this.httpStatus = httpStatus;
  }
}

export default request;
