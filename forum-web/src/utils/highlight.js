/**
 * 关键词高亮工具
 *
 * 把文本中所有出现的 keyword 包裹 <mark class="hl">...</mark>
 * 使用前确保 text 已经是纯文本（HTML 应先剥离）
 */
export function highlight(text, keyword) {
  if (!text || !keyword) return text || '';
  const safe = String(keyword).trim();
  if (!safe) return text;
  const escaped = safe.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const re = new RegExp(escaped, 'gi');
  return String(text).replace(re, (m) => `<mark class="hl">${m}</mark>`);
}
