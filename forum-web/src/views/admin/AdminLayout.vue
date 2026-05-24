<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-sider">
      <div class="admin-logo">论坛后台</div>
      <el-menu
        :default-active="activeMenu"
        background-color="#001529"
        text-color="#bfbfbf"
        active-text-color="#fff"
        router
      >
        <el-menu-item index="/admin">
          <el-icon><Odometer /></el-icon>
          <span>总览</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/boards">
          <el-icon><Grid /></el-icon>
          <span>版块管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/board-applications">
          <el-icon><Promotion /></el-icon>
          <span>板块申请审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/posts">
          <el-icon><Document /></el-icon>
          <span>帖子管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/announcements">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-link @click="$router.push('/')" :underline="false">← 返回前台</el-link>
        </div>
        <div class="header-right">
          <span class="who">{{ userStore.info?.nickname }}（管理员）</span>
          <el-button size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Odometer, User, Grid, Document, ChatDotRound, Promotion, Bell } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const activeMenu = computed(() => route.path);

async function handleLogout() {
  await userStore.logout();
  ElMessage.success('已退出');
  router.push('/admin/login');
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}
.admin-sider {
  background: #001529;
  color: #fff;
}
.admin-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background: #00111f;
  border-bottom: 1px solid #1f2d3d;
}
.admin-sider :deep(.el-menu) {
  border-right: none;
}
.admin-header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.who {
  color: #666;
  font-size: 14px;
}
.admin-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
