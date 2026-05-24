<template>
  <el-dialog
    v-model="settingsStore.visible"
    :show-close="false"
    width="520px"
    :close-on-click-modal="false"
    destroy-on-close
    class="settings-modal"
    @open="onOpen"
  >
    <template #header>
      <div class="modal-header">
        <span class="modal-title">个人中心</span>
        <el-button :icon="Close" circle text @click="settingsStore.close()" />
      </div>
    </template>

    <div class="modal-body">
      <div class="avatar-block">
        <el-avatar :size="72" :src="userStore.info?.avatar || ''">
          {{ userStore.info?.nickname?.charAt(0) || '?' }}
        </el-avatar>
        <el-upload :show-file-list="false" :before-upload="handleAvatarUpload" accept="image/*">
          <el-button size="small" type="primary" text>更换头像</el-button>
        </el-upload>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="邮箱">
          <el-input :model-value="userStore.info?.email" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
          <div class="form-tip">昵称每 30 天可修改一次</div>
        </el-form-item>
        <el-form-item label="简介" prop="bio">
          <el-input v-model="form.bio" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存资料</el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <div class="security-section">
        <h3 class="section-title">账号安全</h3>
        <button type="button" class="security-entry" @click="showPwdDialog = true">
          <span class="entry-label">登录密码</span>
          <span class="entry-action">修改密码</span>
          <el-icon class="entry-arrow"><ArrowRight /></el-icon>
        </button>
      </div>
    </div>

    <ChangePasswordModal v-model="showPwdDialog" />
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, ArrowRight } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { useUserSettingsStore } from '@/stores/userSettings';
import { updateProfile, uploadAvatar } from '@/api/user';
import ChangePasswordModal from './ChangePasswordModal.vue';

const userStore = useUserStore();
const settingsStore = useUserSettingsStore();

const formRef = ref(null);
const saving = ref(false);
const showPwdDialog = ref(false);

const form = reactive({
  nickname: '',
  bio: ''
});

const rules = {
  nickname: [{ min: 2, max: 20, message: '昵称长度 2-20 字符', trigger: 'blur' }],
  bio: [{ max: 200, message: '简介最多 200 字符', trigger: 'blur' }]
};

function onOpen() {
  if (userStore.info) {
    form.nickname = userStore.info.nickname || '';
    form.bio = userStore.info.bio || '';
  }
  showPwdDialog.value = false;
}

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
  return false;
}
</script>

<style scoped>
.settings-modal :deep(.el-dialog) {
  border-radius: 12px;
}
.settings-modal :deep(.el-dialog__header) {
  padding: 16px 20px 0;
  margin: 0;
}
.settings-modal :deep(.el-dialog__body) {
  padding: 8px 20px 20px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.modal-title {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
}
.avatar-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}
.form-tip {
  color: #999;
  font-size: 12px;
  line-height: 1.4;
  margin-top: 4px;
}
.security-section {
  margin-top: 4px;
}
.section-title {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}
.security-entry {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background: #fafafa;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}
.security-entry:hover {
  border-color: #4080ff;
  background: #f5f8ff;
}
.entry-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.entry-action {
  margin-left: auto;
  font-size: 13px;
  color: #4080ff;
}
.entry-arrow {
  margin-left: 6px;
  color: #999;
  font-size: 14px;
}
.security-entry:hover .entry-arrow {
  color: #4080ff;
}
</style>
