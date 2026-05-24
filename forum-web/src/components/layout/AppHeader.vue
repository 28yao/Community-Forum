<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">社区论坛</router-link>

      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索帖子..."
          clearable
          @keyup.enter="handleSearch"
          @clear="keyword = ''"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="nav-right">
        <template v-if="userStore.isLoggedIn">
          <el-button type="primary" size="small" @click="postEditorStore.open()">
            发帖
          </el-button>
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
                <el-dropdown-item command="my-applications">我的板块申请</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided>进入后台</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <BoardApplicationDialog v-model="showApplicationDialog" />
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
import { ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { usePostEditorStore } from '@/stores/postEditor';
import BoardApplicationDialog from '@/components/board-application/BoardApplicationDialog.vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const postEditorStore = usePostEditorStore();

const showApplicationDialog = ref(false);

const keyword = ref(route.query.q || '');

// 路由切换时同步输入框（从其他页面回到搜索页时）
watch(() => route.query.q, (q) => {
  if (route.name === 'search') keyword.value = q || '';
});

function handleSearch() {
  const q = keyword.value.trim();
  if (!q) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }
  if (q.length < 2) {
    ElMessage.warning('关键词至少 2 个字符');
    return;
  }
  router.push({ path: '/search', query: { q, type: 'post' } });
}

async function handleCommand(cmd) {
  if (cmd === 'settings') {
    router.push('/settings');
  } else if (cmd === 'my-applications') {
    showApplicationDialog.value = true;
  } else if (cmd === 'admin') {
    router.push('/admin');
  } else if (cmd === 'logout') {
    userStore.logout();
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
  max-width: var(--page-max-width);
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 16px;
}
.logo {
  font-size: 18px;
  font-weight: bold;
  color: #1677ff;
  text-decoration: none;
  flex-shrink: 0;
}
.search-box {
  flex: 1;
  max-width: 480px;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
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
