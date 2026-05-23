<template>
  <el-card shadow="hover" class="post-card" @click="$router.push(`/post/${post.id}`)">
    <div class="post-card-body">
      <div class="post-card-main">
        <div class="post-card-title">
          <el-tag v-if="post.isPinned" type="danger" size="small" effect="dark">置顶</el-tag>
          <span class="title-text">{{ post.title }}</span>
          <el-tag v-if="post.isEdited" type="info" size="small">已编辑</el-tag>
        </div>
        <p class="post-card-summary">{{ post.summary }}</p>
        <div class="post-card-meta">
          <span class="meta-item">
            <el-avatar :size="20" :src="post.author?.avatar || ''">
              {{ post.author?.nickname?.charAt(0) }}
            </el-avatar>
            <span>{{ post.author?.nickname || '已注销' }}</span>
          </span>
          <span class="meta-sep">·</span>
          <span class="meta-item">{{ formatTime(post.createdAt) }}</span>
          <span class="meta-sep">·</span>
          <span class="meta-item">{{ post.viewCount || 0 }} 浏览</span>
          <span class="meta-sep">·</span>
          <span class="meta-item">{{ post.likeCount || 0 }} 赞</span>
          <span class="meta-sep">·</span>
          <span class="meta-item">{{ post.commentCount || 0 }} 评</span>
        </div>
      </div>
      <div v-if="post.firstImage" class="post-card-thumb">
        <el-image :src="post.firstImage" fit="cover" />
        <div v-if="post.imageCount > 1" class="thumb-badge">+{{ post.imageCount - 1 }}</div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  post: { type: Object, required: true }
});

function formatTime(t) {
  if (!t) return '';
  const d = new Date(typeof t === 'string' ? t.replace(' ', 'T') : t);
  if (Number.isNaN(d.getTime())) return t;
  return d.toLocaleString('zh-CN', { hour12: false });
}
</script>

<style scoped>
.post-card {
  margin-bottom: 12px;
  cursor: pointer;
}
.post-card-body {
  display: flex;
  gap: 12px;
}
.post-card-main {
  flex: 1;
  min-width: 0;
}
.post-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1a1a1a;
}
.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.post-card-summary {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.post-card-meta {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #999;
  gap: 6px;
  flex-wrap: wrap;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.meta-sep {
  color: #ddd;
}
.post-card-thumb {
  width: 120px;
  height: 90px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
  position: relative;
}
.post-card-thumb .el-image {
  width: 100%;
  height: 100%;
}
.thumb-badge {
  position: absolute;
  right: 4px;
  bottom: 4px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  border-radius: 10px;
  font-size: 11px;
  padding: 2px 6px;
}
</style>
