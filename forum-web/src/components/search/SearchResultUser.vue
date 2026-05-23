<template>
  <div class="user-results">
    <div
      v-for="u in users"
      :key="u.id"
      class="user-result-card"
    >
      <el-avatar :size="40" :src="u.avatar || ''">
        {{ u.nickname?.charAt(0) }}
      </el-avatar>
      <div class="user-info">
        <div class="user-name" v-html="highlightHtml(u.nickname)"></div>
        <div v-if="u.bio" class="user-bio">{{ u.bio }}</div>
        <div class="user-meta">注册于 {{ formatTime(u.createdAt) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { highlight } from '@/utils/highlight';

const props = defineProps({
  users: { type: Array, required: true },
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
.user-results {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.user-result-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}
.user-info {
  flex: 1;
  min-width: 0;
}
.user-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}
.user-bio {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-meta {
  font-size: 12px;
  color: #999;
}
</style>
