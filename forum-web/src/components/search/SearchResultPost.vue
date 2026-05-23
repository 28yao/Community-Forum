<template>
  <div class="post-results">
    <el-card
      v-for="r in posts"
      :key="r.id"
      shadow="hover"
      class="result-card"
      @click="$router.push(`/post/${r.id}`)"
    >
      <div class="result-main">
        <h3 class="result-title" v-html="highlightHtml(r.title)"></h3>
        <p class="result-summary" v-html="highlightHtml(r.summary)"></p>
        <div class="result-meta">
          <span>{{ r.author?.nickname || '已注销' }}</span>
          <span class="sep">·</span>
          <router-link v-if="r.board?.id" :to="`/board/${r.board.id}`" @click.stop>{{ r.board.name }}</router-link>
          <span class="sep">·</span>
          <span>{{ formatTime(r.createdAt) }}</span>
          <span class="sep">·</span>
          <span>{{ r.likeCount || 0 }} 赞</span>
          <span class="sep">·</span>
          <span>{{ r.commentCount || 0 }} 评论</span>
        </div>
      </div>
      <el-image
        v-if="r.firstImage"
        :src="r.firstImage"
        fit="cover"
        class="result-thumb"
      />
    </el-card>
  </div>
</template>

<script setup>
import { highlight } from '@/utils/highlight';

const props = defineProps({
  posts: { type: Array, required: true },
  keyword: { type: String, default: '' }
});

function highlightHtml(text) {
  return highlight(text, props.keyword);
}

function formatTime(t) {
  if (!t) return '';
  const d = new Date(typeof t === 'string' ? t.replace(' ', 'T') : t);
  return Number.isNaN(d.getTime()) ? t : d.toLocaleDateString('zh-CN');
}
</script>

<style scoped>
.post-results {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.result-card {
  cursor: pointer;
}
.result-card :deep(.el-card__body) {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.result-main {
  flex: 1;
  min-width: 0;
}
.result-title {
  margin: 0 0 8px 0;
  font-size: 16px;
}
.result-summary {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.result-meta {
  color: #999;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.sep {
  color: #ddd;
}
.result-thumb {
  width: 100px;
  height: 80px;
  border-radius: 4px;
  flex-shrink: 0;
}
</style>
