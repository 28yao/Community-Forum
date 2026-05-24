<template>
  <div class="home-page">
    <div class="three-col-layout">
      <aside class="left-col">
        <UnifiedSidebar />
      </aside>
      <main class="main-content">
        <div class="home-header">
          <h2 class="page-title">综合 · 信息流</h2>
          <el-button v-if="userStore.isLoggedIn" type="primary" round @click="postEditorStore.open()">
            + 发帖
          </el-button>
        </div>
        <PostList mode="feed" />
      </main>
      <aside class="right-col">
        <AnnouncementSidebar scope="site" />
      </aside>
    </div>
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
import UnifiedSidebar from '@/components/layout/UnifiedSidebar.vue';
import AnnouncementSidebar from '@/components/announcement/AnnouncementSidebar.vue';
import PostList from '@/components/post/PostList.vue';
import { useUserStore } from '@/stores/user';
import { usePostEditorStore } from '@/stores/postEditor';

const userStore = useUserStore();
const postEditorStore = usePostEditorStore();
</script>

<style scoped>
.home-page {
  padding: 0;
}
.three-col-layout {
  display: flex;
  gap: 16px;
}
.left-col {
  width: 240px;
  flex-shrink: 0;
}
.main-content {
  flex: 1;
  min-width: 0;
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
  width: 260px;
  flex-shrink: 0;
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
@media (max-width: 768px) {
  .left-col,
  .right-col {
    display: none;
  }
}
</style>
