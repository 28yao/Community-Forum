<template>
  <router-link :to="`/board/${board.id}`" class="board-card" :class="{ disabled: board.status === 0 }">
    <el-avatar :size="size === 'compact' ? 32 : 40" :src="board.icon || ''" shape="square" class="board-icon">
      {{ board.name?.charAt(0) }}
    </el-avatar>
    <div class="board-info">
      <div class="board-name">{{ board.name }}</div>
      <div v-if="size !== 'compact'" class="board-meta">
        关注 {{ board.followerCount || 0 }} · 帖 {{ board.postCount || 0 }}
      </div>
    </div>
  </router-link>
</template>

<script setup>
/** 板块卡片（P2-M4），左栏与首页推荐区共用，size=compact 时不显示 meta */
defineProps({
  board: { type: Object, required: true },
  size: { type: String, default: 'default' }
});
</script>

<style scoped>
.board-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  color: #333;
  text-decoration: none;
  transition: background 0.15s;
}
.board-card:hover {
  background: #f0f6ff;
}
.board-card.disabled {
  opacity: 0.5;
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
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.board-meta {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
</style>
