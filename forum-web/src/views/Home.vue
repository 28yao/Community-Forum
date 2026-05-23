<template>
  <div class="home-page">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="8" :md="6" :lg="5">
        <BoardSidebar />
      </el-col>
      <el-col :xs="24" :sm="16" :md="18" :lg="14">
        <div class="main-content">
          <div class="home-header">
            <h2 class="page-title">综合 · 信息流</h2>
            <el-button v-if="userStore.isLoggedIn" type="primary" round @click="$router.push('/post/create')">
              + 发帖
            </el-button>
          </div>
          <PostList mode="feed" />
        </div>
      </el-col>
      <!-- 右栏占位（按 PRD，二期不实现热点） -->
      <el-col :xs="0" :sm="0" :md="0" :lg="5" class="right-col">
        <aside class="right-placeholder">
          <h4>· 公告 ·</h4>
          <p class="hint-text">这里将来会显示热门话题 / 站点公告</p>
        </aside>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/**
 * 首页（P2-M4 贴吧风重构，覆盖一期 M3-T28）
 *
 * 布局：左栏导航（关注/推荐/全部） + 中间信息流 + 右栏占位
 * 数据流不变：仍调一期 PostList → /api/posts
 *
 * 注：P2-M5 会把中间区域换为 /feed 接口。本模块仅 UI 重构，数据来源不变。
 */
import { onMounted } from 'vue';
import BoardSidebar from '@/components/layout/BoardSidebar.vue';
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
.home-page {
  padding: 0;
}
.main-content {
  background: #fff;
  border-radius: 6px;
  padding: 16px 20px;
}
.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  margin-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}
.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}
.right-col {
  /* 移动端隐藏，桌面端 lg+ 显示 */
}
.right-placeholder {
  background: #fff;
  border-radius: 6px;
  padding: 16px;
  position: sticky;
  top: 70px;
  text-align: center;
  color: #999;
}
.right-placeholder h4 {
  margin: 0 0 8px 0;
  color: #666;
  font-weight: 500;
}
.hint-text {
  font-size: 12px;
  margin: 0;
}
</style>
