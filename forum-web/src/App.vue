<template>
  <!-- 后台布局：直接渲染 router-view（AdminLayout 自带布局；AdminLogin 全屏） -->
  <router-view v-if="isAdminLayout" />

  <!-- 前台布局 -->
  <div v-else class="app-shell">
    <AppHeader />
    <main class="app-main">
      <router-view />
    </main>
    <AppFooter />
    <PostEditorModal />
    <SettingsModal />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import AppHeader from '@/components/layout/AppHeader.vue';
import AppFooter from '@/components/layout/AppFooter.vue';
import PostEditorModal from '@/components/post-editor/PostEditorModal.vue';
import SettingsModal from '@/components/user/SettingsModal.vue';

const route = useRoute();
const isAdminLayout = computed(() => !!route.meta?.adminLayout);
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', sans-serif;
}
.app-main {
  flex: 1;
  max-width: var(--page-max-width);
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}
</style>
