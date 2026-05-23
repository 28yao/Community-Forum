<template>
  <article class="post-card-tieba" @click="$router.push(`/post/${post.id}`)">
    <!-- 顶部：板块标 + 作者 + 时间 -->
    <header class="card-header">
      <router-link
        v-if="boardLabel"
        :to="`/board/${post.boardId}`"
        class="board-tag"
        @click.stop
      >{{ boardLabel }}</router-link>
      <el-avatar :size="20" :src="post.author?.avatar || ''" class="author-avatar">
        {{ post.author?.nickname?.charAt(0) }}
      </el-avatar>
      <span class="author-name">{{ post.author?.nickname || '已注销' }}</span>
      <span class="dot">·</span>
      <span class="time">{{ formatTime(post.createdAt) }}</span>
      <el-tag v-if="post.isPinned" type="danger" size="small" effect="dark" class="badge">置顶</el-tag>
      <el-tag v-if="post.isEdited" type="info" size="small" class="badge">已编辑</el-tag>
    </header>

    <!-- 标题 -->
    <h3 class="card-title">{{ post.title }}</h3>

    <!-- 摘要 -->
    <p v-if="post.summary" class="card-summary">{{ post.summary }}</p>

    <!-- 图片网格（最多 3 张大图，超出 +N） -->
    <PostImageGrid v-if="images.length" :images="images" :max-display="3" />

    <!-- 底部统计 -->
    <footer class="card-footer">
      <span class="stat">
        <el-icon><View /></el-icon>
        {{ post.viewCount || 0 }}
      </span>
      <span class="stat">
        <el-icon><ChatDotRound /></el-icon>
        {{ post.commentCount || 0 }}
      </span>
      <span class="stat">
        <el-icon><Pointer /></el-icon>
        {{ post.likeCount || 0 }}
      </span>
    </footer>
  </article>
</template>

<script setup>
/**
 * 贴吧风帖子卡片（P2-M4，覆盖一期 M3-T20）
 *
 * 数据来源完全沿用一期 listPosts DTO，仅消费方式变化：
 * - post.images (P2-M4 后端新增) → 大图网格（最多 3 张）
 * - post.boardId → 通过 appStore 反查板块名（零额外请求）
 * - 兼容旧字段：firstImage / imageCount 仍可用，若 images 缺省回退到 firstImage
 */
import { computed } from 'vue';
import { View, ChatDotRound, Pointer } from '@element-plus/icons-vue';
import PostImageGrid from './PostImageGrid.vue';
import { useAppStore } from '@/stores/app';

const props = defineProps({
  post: { type: Object, required: true }
});

const appStore = useAppStore();

/** 优先用 post.images（P2-M4 新字段），缺省回退到 firstImage（一期兼容） */
const images = computed(() => {
  if (Array.isArray(props.post.images) && props.post.images.length) {
    return props.post.images;
  }
  if (props.post.firstImage) return [props.post.firstImage];
  return [];
});

/** 通过 appStore.boards 字典反查板块名；若缓存未加载则隐藏标签（不阻塞） */
const boardLabel = computed(() => {
  if (!props.post.boardId) return '';
  const b = appStore.boards.find((x) => x.id === props.post.boardId);
  return b?.name || '';
});

function formatTime(t) {
  if (!t) return '';
  const d = new Date(typeof t === 'string' ? t.replace(' ', 'T') : t);
  if (Number.isNaN(d.getTime())) return t;
  const now = new Date();
  const diffMs = now - d;
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return '刚刚';
  if (diffMin < 60) return diffMin + ' 分钟前';
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return diffHour + ' 小时前';
  const diffDay = Math.floor(diffHour / 24);
  if (diffDay < 30) return diffDay + ' 天前';
  return d.toLocaleDateString('zh-CN');
}
</script>

<style scoped>
.post-card-tieba {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 10px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.post-card-tieba:hover {
  border-color: #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.board-tag {
  color: #1677ff;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  text-decoration: none;
}
.board-tag:hover {
  background: #d9ecff;
}
.author-avatar {
  margin-left: 4px;
}
.author-name {
  color: #606266;
}
.dot {
  color: #ddd;
}
.badge {
  margin-left: 4px;
}
.card-title {
  font-size: 17px;
  font-weight: 600;
  margin: 4px 0 6px 0;
  color: #1a1a1a;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.card-summary {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-footer {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 12px;
  color: #999;
  font-size: 13px;
}
.stat {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
