<template>
  <div class="application-list">
    <el-empty v-if="!list.length" description="暂无申请记录" />
    <div v-for="item in list" :key="item.id" class="app-card">
      <div class="card-header">
        <span class="app-name">{{ item.name }}</span>
        <el-tag :type="statusTagType(item.status)" size="small">{{ statusText(item.status) }}</el-tag>
      </div>
      <div class="card-body">
        <p class="desc">{{ item.description }}</p>
        <p v-if="item.slogan" class="slogan">口号：{{ item.slogan }}</p>
        <p v-if="item.tags" class="tags">
          标签：
          <el-tag v-for="t in (item.tags || '').split(',').filter(Boolean)" :key="t" size="small" type="info" style="margin-right:4px;">
            {{ t.trim() }}
          </el-tag>
        </p>
      </div>
      <div v-if="item.status === 3 && item.rejectReason" class="reject-reason">
        驳回原因：{{ item.rejectReason }}
      </div>
      <div v-if="item.status === 2 && item.boardId" class="approved-link">
        <el-link type="primary" @click="$router.push(`/board/${item.boardId}`)">→ 进入新板块</el-link>
      </div>
      <div class="card-footer">
        申请时间：{{ formatTime(item.createdAt) }}
        <span v-if="item.reviewedAt"> · 审核时间：{{ formatTime(item.reviewedAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
/** 我的申请列表卡片视图（P2-M2） */
defineProps({
  list: { type: Array, required: true }
});

function statusText(s) {
  if (s === 1) return '待审核';
  if (s === 2) return '已通过';
  if (s === 3) return '已驳回';
  return '未知';
}
function statusTagType(s) {
  if (s === 1) return 'warning';
  if (s === 2) return 'success';
  if (s === 3) return 'danger';
  return '';
}
function formatTime(t) {
  if (!t) return '';
  if (typeof t === 'string') return t.replace('T', ' ').slice(0, 16);
  return new Date(t).toLocaleString();
}
</script>

<style scoped>
.app-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 12px;
  background: #fff;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.app-name {
  font-weight: 600;
  font-size: 16px;
}
.card-body p {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
}
.desc {
  white-space: pre-wrap;
}
.reject-reason {
  margin-top: 8px;
  padding: 8px;
  background: #fef0f0;
  color: #f56c6c;
  border-radius: 4px;
  font-size: 13px;
}
.approved-link {
  margin-top: 8px;
}
.card-footer {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}
</style>
