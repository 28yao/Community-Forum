<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">社区论坛</router-link>
      <div class="nav-right">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="28" :src="userStore.info?.avatar || ''">
                {{ userStore.info?.nickname?.charAt(0) || '?' }}
              </el-avatar>
              <span class="nickname">{{ userStore.info?.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">个人设置</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" size="small" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

async function handleCommand(cmd) {
  if (cmd === 'settings') {
    router.push('/settings');
  } else if (cmd === 'logout') {
    await userStore.logout();
    ElMessage.success('已退出登录');
    router.push('/');
  }
}
</script>

<style scoped>
.app-header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  height: 56px;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  font-size: 18px;
  font-weight: bold;
  color: #1677ff;
  text-decoration: none;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.nickname {
  font-size: 14px;
  color: #333;
}
</style>
