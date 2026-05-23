<template>
  <div class="app-shell">
    <header class="placeholder-header">
      <span>社区论坛系统</span>
      <el-tag size="small" type="info" effect="plain">M0 基础设施验证</el-tag>
    </header>
    <main class="placeholder-main">
      <router-view v-slot="{ Component, route }">
        <component :is="Component" v-if="Component" />
        <div v-else>
          <p>当前路由：<code>{{ route.fullPath }}</code></p>
        </div>
      </router-view>

      <el-divider>Element Plus 验证（M0-T18）</el-divider>

      <el-space wrap>
        <el-button type="primary" @click="onPing">健康检查后端 /api/boards</el-button>
        <el-button @click="onTo('/login')">前往 /login</el-button>
        <el-button @click="onTo('/no-such')">前往 /no-such（404 兜底）</el-button>
      </el-space>

      <el-alert
        v-if="pingResult"
        :title="pingResult"
        :type="pingType"
        show-icon
        :closable="false"
        style="margin-top: 16px"
      />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useAppStore } from '@/stores/app';
import request, { BizError } from '@/api/request';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const appStore = useAppStore();

const pingResult = ref('');
const pingType = ref('info');

// eslint-disable-next-line no-console
console.log('[M0] init OK:', {
  isLoggedIn: userStore.isLoggedIn,
  boards: appStore.boards.length,
  axiosBaseURL: request.defaults.baseURL
});

function onTo(path) {
  router.push(path);
}

async function onPing() {
  pingResult.value = '请求中...';
  pingType.value = 'info';
  try {
    // 后端 M2-T4 才实现真实接口；当前后端无该路由，预期返回 9999 系统异常或 BizError
    const data = await request.get('/boards');
    pingResult.value = `成功：返回 ${Array.isArray(data) ? data.length : 0} 个版块`;
    pingType.value = 'success';
    ElMessage.success('接口可达');
  } catch (e) {
    if (e instanceof BizError) {
      pingResult.value = `BizError: code=${e.code}, message=${e.message}`;
      pingType.value = e.code === 9999 ? 'error' : 'warning';
    } else {
      pingResult.value = `未知错误：${e.message || e}`;
      pingType.value = 'error';
    }
  }
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', sans-serif;
}
.placeholder-header {
  padding: 16px 24px;
  font-size: 20px;
  font-weight: 600;
  border-bottom: 1px solid #e8e8e8;
  background: #fff;
  display: flex;
  align-items: center;
  gap: 12px;
}
.placeholder-main {
  flex: 1;
  padding: 32px 24px;
  color: #333;
  max-width: 720px;
  margin: 0 auto;
  line-height: 1.6;
}
code {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
