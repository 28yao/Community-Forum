<template>
  <div class="home-page">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="8" :md="6">
        <AppSidebar />
      </el-col>
      <el-col :xs="24" :sm="16" :md="18">
        <div class="main-content">
          <div class="home-header">
            <h2 style="margin:0;">最新帖子</h2>
            <el-button v-if="userStore.isLoggedIn" type="primary" @click="$router.push('/post/create')">发帖</el-button>
          </div>
          <el-divider />
          <PostList />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import AppSidebar from '@/components/layout/AppSidebar.vue';
import PostList from '@/components/post/PostList.vue';
import { useAppStore } from '@/stores/app';
import { useUserStore } from '@/stores/user';

const appStore = useAppStore();
const userStore = useUserStore();

onMounted(() => {
  if (!appStore.boardsLoaded) appStore.loadBoards();
});
</script>

<style scoped>
.home-page { padding: 0; }
.main-content {
  background: #fff;
  border-radius: 6px;
  padding: 24px;
}
.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
