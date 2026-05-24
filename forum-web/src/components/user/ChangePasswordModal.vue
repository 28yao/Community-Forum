<template>
  <el-dialog
    v-model="visible"
    title="修改密码"
    width="420px"
    :close-on-click-modal="false"
    append-to-body
    destroy-on-close
    class="change-password-modal"
    @closed="resetForm"
  >
    <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-position="top">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="pwdForm.oldPassword"
          type="password"
          show-password
          placeholder="请输入当前密码"
          autocomplete="current-password"
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="pwdForm.newPassword"
          type="password"
          show-password
          placeholder="6-32 位，需含字母和数字"
          autocomplete="new-password"
        />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
          v-model="pwdForm.confirmPassword"
          type="password"
          show-password
          placeholder="请再次输入新密码"
          autocomplete="new-password"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="pwdSaving" @click="handleSubmit">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { changePassword } from '@/api/user';

const props = defineProps({
  modelValue: { type: Boolean, default: false }
});

const emit = defineEmits(['update:modelValue', 'success']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
});

const pwdFormRef = ref(null);
const pwdSaving = ref(false);

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const validateConfirmPwd = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'));
  } else if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'));
  } else {
    callback();
  }
};

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码需包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPwd, trigger: 'blur' }]
};

function resetForm() {
  pwdForm.oldPassword = '';
  pwdForm.newPassword = '';
  pwdForm.confirmPassword = '';
  pwdFormRef.value?.resetFields();
}

async function handleSubmit() {
  await pwdFormRef.value.validate();
  pwdSaving.value = true;
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    });
    ElMessage.success('密码修改成功');
    emit('success');
    visible.value = false;
  } catch (e) {
    ElMessage.error(e.message || '修改失败');
  } finally {
    pwdSaving.value = false;
  }
}
</script>

<style scoped>
.change-password-modal :deep(.el-dialog) {
  border-radius: 12px;
}
</style>
