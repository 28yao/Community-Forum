/** 检测内容是否已含 HTML 标签 */
const HTML_TAG_RE = /<\/?([a-z][a-z0-9]*)\b[^>]*>/i;

function escapeHtml(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

/**
 * 公告/富文本展示：纯文本换行转 <br>，已是 HTML 则原样输出
 */
export function formatRichContent(content) {
  if (!content) return '';
  if (HTML_TAG_RE.test(content.trim())) {
    return content;
  }
  return escapeHtml(content).replace(/\n/g, '<br>');
}

/**
 * 列表预览：去掉 HTML 标签，保留纯文本（含换行）
 */
export function toPlainTextPreview(content) {
  if (!content) return '';
  if (HTML_TAG_RE.test(content.trim())) {
    return content
      .replace(/<br\s*\/?>/gi, '\n')
      .replace(/<\/p>/gi, '\n')
      .replace(/<[^>]+>/g, '')
      .replace(/&nbsp;/g, ' ')
      .replace(/&amp;/g, '&')
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .trim();
  }
  return content;
}
