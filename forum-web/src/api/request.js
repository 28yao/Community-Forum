import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';

/**
 * 统一封装的 Axios 实例
 *
 * - baseURL：dev 走 Vite 代理，prod 用 VITE_API_BASE_URL
 * - 请求拦截：自动注入 Authorization: Bearer <token>
 * - 响应拦截：自动展开后端统一响应体；code=1001 自动登出 + 跳 /login；
 *   网络异常/5xx 走全局 toast 兜底（业务也可 catch 后自行处理）
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
    // 未登录：清登录态 + 跳登录页（避免业务页面到处写跳转）
    if (data.code === 1001) {
      try {
        useUserStore().clear();
      } catch (e) { /* ignore */ }
      const cur = window.location.pathname + window.location.search;
      // 后台路径跳后台登录
      if (cur.startsWith('/admin')) {
        if (window.location.pathname !== '/admin/login') {
          window.location.href = '/admin/login';
        }
      } else if (!['/login', '/register'].includes(window.location.pathname)) {
        window.location.href = '/login?redirect=' + encodeURIComponent(cur);
      }
    }
    // 业务错误抛给调用方 catch
    const err = new BizError(data.code, data.message || '请求失败');
    return Promise.reject(err);
  }
  // 非标准响应：直接返回原始
  return data;
}, (err) => {
  // 网络/超时/HTTP 5xx：全局 toast 兜底（业务 catch 时 ElMessage 可能与此叠加一次，可接受）
  const status = err?.response?.status;
  const respData = err?.response?.data;
  if (respData && typeof respData === 'object' && 'code' in respData) {
    return Promise.reject(new BizError(respData.code, respData.message || '请求失败', status));
  }
  let bizErr;
  if (err?.code === 'ECONNABORTED') {
    bizErr = new BizError(9999, '请求超时，请重试', status);
  } else if (!err?.response) {
    bizErr = new BizError(9999, '网络异常，请检查网络后重试', status);
  } else {
    bizErr = new BizError(9999, `服务器异常（${status}），请稍后重试`, status);
  }
  ElMessage.error(bizErr.message);
  return Promise.reject(bizErr);
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
