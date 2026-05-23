<template>
  <div class="board-results">
    <router-link
      v-for="b in boards"
      :key="b.id"
      :to="`/board/${b.id}`"
      class="board-result-card"
    >
      <el-avatar :size="40" :src="b.icon || ''" shape="square" class="board-icon">
        {{ b.name?.charAt(0) }}
      </el-avatar>
      <div class="board-info">
        <div class="board-name" v-html="highlightHtml(b.name)"></div>
        <div class="board-desc" v-html="highlightHtml(b.description)"></div>
        <div class="board-meta">
          关注 {{ b.followerCount || 0 }} · 帖子 {{ b.postCount || 0 }}
        </div>
      </div>
    </router-link>
  </div>
</template>

<script setup>
import { highlight } from '@/utils/highlight';

const props = defineProps({
  boards: { type: Array, required: true },
  keyword: { type: String, default: '' }
});

function highlightHtml(text) {
  return highlight(text, props.keyword);
}
</script>

<style scoped>
.board-results {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.board-result-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  text-decoration: none;
  color: #333;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.board-result-card:hover {
  border-color: #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.board-icon {
  flex-shrink: 0;
  background: #ecf5ff;
  color: #1677ff;
  font-weight: 600;
}
.board-info {
  flex: 1;
  min-width: 0;
}
.board-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}
.board-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.board-meta {
  font-size: 12px;
  color: #999;
}
</style>
