<template>
  <div class="home-page">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="8" :md="6">
        <AppSidebar />
      </el-col>
      <el-col :xs="24" :sm="16" :md="18">
        <div class="main-content">
          <h2 style="margin:0 0 16px 0;">欢迎来到社区论坛</h2>
          <p style="color:#666;">浏览左侧版块，参与讨论。</p>

          <el-divider>各版块预览</el-divider>

          <div v-if="loading" v-loading="true" style="min-height:200px;"></div>
          <el-empty v-else-if="!appStore.boards.length" description="暂无版块" />
          <div v-else class="board-preview-grid">
            <el-card
              v-for="b in appStore.boards"
              :key="b.id"
              shadow="hover"
              class="board-card"
              @click="$router.push(`/board/${b.id}`)"
            >
              <div class="board-card-header">
                <h3 style="margin:0;">{{ b.name }}</h3>
                <el-tag size="small">{{ b.postCount || 0 }} 帖</el-tag>
              </div>
              <p class="board-desc">{{ b.description || '暂无描述' }}</p>
              <div class="board-card-footer">
                <span style="color:#999;font-size:12px;">点击查看帖子列表（M3 完成后启用）</span>
              </div>
            </el-card>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import AppSidebar from '@/components/layout/AppSidebar.vue';
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
.home-page {
  padding: 0;
}
.main-content {
  background: #fff;
  border-radius: 6px;
  padding: 24px;
}
.board-preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}
.board-card {
  cursor: pointer;
}
.board-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.board-desc {
  color: #666;
  font-size: 13px;
  min-height: 40px;
  margin: 0 0 8px 0;
}
.board-card-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 8px;
}
</style>
