<template>
  <div class="settings-page">
    <el-card shadow="hover" style="max-width:600px;margin:20px auto;">
      <template #header>
        <h2 style="margin:0;">个人设置</h2>
      </template>

      <!-- 头像 -->
      <div style="text-align:center;margin-bottom:24px;">
        <el-avatar :size="80" :src="userStore.info?.avatar || ''">
          {{ userStore.info?.nickname?.charAt(0) || '?' }}
        </el-avatar>
        <div style="margin-top:8px;">
          <el-upload :show-file-list="false" :before-upload="handleAvatarUpload" accept="image/*">
            <el-button size="small">更换头像</el-button>
          </el-upload>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="邮箱">
          <el-input :value="userStore.info?.email" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
          <div class="el-form-item__tip" style="color:#999;font-size:12px;">
            昵称每 30 天可修改一次
          </div>
        </el-form-item>
        <el-form-item label="简介" prop="bio">
          <el-input v-model="form.bio" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { updateProfile, uploadAvatar } from '@/api/user';

const userStore = useUserStore();
const formRef = ref(null);
const saving = ref(false);

const form = reactive({
  nickname: '',
  bio: ''
});

const rules = {
  nickname: [
    { min: 2, max: 20, message: '昵称长度 2-20 字符', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '简介最多 200 字符', trigger: 'blur' }
  ]
};

onMounted(() => {
  if (userStore.info) {
    form.nickname = userStore.info.nickname || '';
    form.bio = userStore.info.bio || '';
  }
});

async function handleSave() {
  await formRef.value.validate();
  saving.value = true;
  try {
    await updateProfile({ nickname: form.nickname, bio: form.bio });
    userStore.updateInfo({ nickname: form.nickname, bio: form.bio });
    ElMessage.success('资料更新成功');
  } catch (e) {
    ElMessage.error(e.message || '更新失败');
  } finally {
    saving.value = false;
  }
}

async function handleAvatarUpload(file) {
  try {
    const data = await uploadAvatar(file);
    userStore.updateInfo({ avatar: data.avatar });
    ElMessage.success('头像更新成功');
  } catch (e) {
    ElMessage.error(e.message || '头像上传失败');
  }
  return false; // 阻止 el-upload 自动上传
}
</script>

<style scoped>
.settings-page {
  padding: 20px;
}
</style>
