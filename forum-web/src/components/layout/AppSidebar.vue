<template>
  <aside class="app-sidebar">
    <h3 class="sidebar-title">版块导航</h3>
    <el-skeleton v-if="loading" :rows="3" animated />
    <ul v-else-if="appStore.boards.length" class="board-list">
      <li v-for="b in appStore.boards" :key="b.id">
        <router-link :to="`/board/${b.id}`" class="board-link">
          <span class="board-name">{{ b.name }}</span>
          <el-tag size="small" type="info">{{ b.postCount || 0 }}</el-tag>
        </router-link>
      </li>
    </ul>
    <el-empty v-else description="暂无版块" :image-size="60" />
  </aside>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAppStore } from '@/stores/app';

const appStore = useAppStore();
const loading = ref(false);

onMounted(async () => {
  if (!appStore.boardsLoaded) {
    loading.value = true;
    await appStore.loadBoards();
    loading.value = false;
  }
});
</script>

<style scoped>
.app-sidebar {
  background: #fff;
  border-radius: 6px;
  padding: 16px;
  min-height: 200px;
}
.sidebar-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
}
.board-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.board-list li {
  margin-bottom: 8px;
}
.board-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 4px;
  color: #333;
  text-decoration: none;
  transition: background 0.2s;
}
.board-link:hover {
  background: #f0f6ff;
  color: #1677ff;
}
.board-name {
  font-size: 14px;
}
</style>
