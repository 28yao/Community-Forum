<template>
  <div class="admin-login-page">
    <el-card class="login-card">
      <h2 class="title">社区论坛 · 后台管理</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="管理员邮箱" prop="email">
          <el-input v-model="form.email" placeholder="admin@forum.com" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" @keyup.enter="handleSubmit" />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width:100%;" @click="handleSubmit">
          {{ loading ? '登录中...' : '登录后台' }}
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { adminLogin } from '@/api/admin';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

const formRef = ref();
const loading = ref(false);
const form = ref({ email: '', password: '' });
const rules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    const data = await adminLogin({ email: form.value.email, password: form.value.password });
    userStore.token = data.token;
    userStore.info = data.user;
    localStorage.setItem('forum_token', data.token);
    localStorage.setItem('forum_user', JSON.stringify(data.user));
    ElMessage.success('登录成功');
    router.push('/admin');
  } catch (e) {
    ElMessage.error(e.message || '登录失败');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1677ff 0%, #5e6dff 100%);
}
.login-card {
  width: 400px;
  padding: 20px;
}
.title {
  text-align: center;
  margin: 0 0 24px 0;
  color: #1677ff;
}
</style>
