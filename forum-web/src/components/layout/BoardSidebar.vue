<template>
  <aside class="board-sidebar">
    <!-- 关注的板块（仅登录态） -->
    <section class="sidebar-section">
      <h3 class="section-title">
        <el-icon><Star /></el-icon>
        <span>我关注的</span>
      </h3>
      <template v-if="!userStore.isLoggedIn">
        <p class="hint">
          <el-link type="primary" @click="goLogin">登录</el-link>
          后可关注感兴趣的板块
        </p>
      </template>
      <el-skeleton v-else-if="followStore.followedLoaded === false" :rows="2" animated />
      <template v-else-if="followStore.followedBoards.length">
        <BoardCard
          v-for="b in followStore.followedBoards"
          :key="b.id"
          :board="b"
          size="compact"
        />
      </template>
      <el-empty v-else description="还没有关注任何板块" :image-size="40" />
    </section>

    <el-divider class="sidebar-divider" />

    <!-- 推荐板块 -->
    <section class="sidebar-section">
      <h3 class="section-title">
        <el-icon><Histogram /></el-icon>
        <span>推荐板块</span>
      </h3>
      <el-skeleton v-if="followStore.recommendedLoaded === false" :rows="3" animated />
      <template v-else-if="followStore.recommendedBoards.length">
        <BoardCard
          v-for="b in followStore.recommendedBoards"
          :key="b.id"
          :board="b"
          size="compact"
        />
      </template>
      <el-empty v-else description="暂无推荐" :image-size="40" />
    </section>

    <el-divider class="sidebar-divider" />

    <!-- 全部板块（一期 boards） -->
    <section class="sidebar-section">
      <h3 class="section-title">
        <el-icon><Grid /></el-icon>
        <span>全部板块</span>
      </h3>
      <el-skeleton v-if="!appStore.boardsLoaded" :rows="3" animated />
      <template v-else-if="appStore.boards.length">
        <BoardCard
          v-for="b in appStore.boards"
          :key="b.id"
          :board="b"
          size="compact"
        />
      </template>
      <el-empty v-else description="暂无板块" :image-size="40" />
    </section>
  </aside>
</template>

<script setup>
/**
 * 贴吧风左栏（P2-M4）：关注 / 推荐 / 全部 三段
 *
 * 复用一期 appStore.boards（全部）+ 二期 boardFollowStore（关注+推荐）。
 * 仅 UI 组装，不引入新业务逻辑。
 */
import { onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Star, Histogram, Grid } from '@element-plus/icons-vue';
import BoardCard from '@/components/board/BoardCard.vue';
import { useAppStore } from '@/stores/app';
import { useUserStore } from '@/stores/user';
import { useBoardFollowStore } from '@/stores/boardFollow';

const appStore = useAppStore();
const userStore = useUserStore();
const followStore = useBoardFollowStore();
const router = useRouter();
const route = useRoute();

function goLogin() {
  router.push('/login?redirect=' + encodeURIComponent(route.fullPath));
}

onMounted(() => {
  if (!appStore.boardsLoaded) appStore.loadBoards();
  if (!followStore.recommendedLoaded) followStore.loadRecommended();
  if (userStore.isLoggedIn && !followStore.followedLoaded) followStore.loadFollowed();
});
</script>

<style scoped>
.board-sidebar {
  background: #fff;
  border-radius: 6px;
  padding: 16px 12px;
  min-height: 200px;
  position: sticky;
  top: 70px;
}
.sidebar-section {
  margin-bottom: 8px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 8px 10px;
  font-size: 14px;
  font-weight: 600;
  color: #666;
}
.sidebar-divider {
  margin: 12px 0;
}
.hint {
  color: #999;
  font-size: 13px;
  margin: 0 0 0 10px;
}
</style>
