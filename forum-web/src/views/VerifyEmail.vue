<template>
  <div class="verify-page">
    <el-card class="verify-card" shadow="hover">
      <div v-if="loading" v-loading="true" style="height:120px;"></div>
      <el-result v-else-if="success" icon="success" title="邮箱验证成功" sub-title="您现在可以登录了">
        <template #extra>
          <el-button type="primary" @click="$router.push('/login')">去登录</el-button>
        </template>
      </el-result>
      <el-result v-else icon="error" title="验证失败" :sub-title="errorMsg">
        <template #extra>
          <el-button @click="$router.push('/')">返回首页</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { verifyEmail } from '@/api/auth';

const route = useRoute();
const loading = ref(true);
const success = ref(false);
const errorMsg = ref('');

onMounted(async () => {
  const token = route.query.token;
  if (!token) {
    loading.value = false;
    errorMsg.value = '缺少验证参数';
    return;
  }
  try {
    await verifyEmail(token);
    success.value = true;
  } catch (e) {
    errorMsg.value = e.message || '验证链接无效或已过期';
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.verify-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 80px);
}
.verify-card {
  width: 500px;
  padding: 20px;
}
</style>
