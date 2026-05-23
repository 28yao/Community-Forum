<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="submitting">
    <el-form-item label="板块名称" prop="name">
      <el-input v-model="form.name" placeholder="5-20 字，例如：洛克王国手游" maxlength="20" show-word-limit />
    </el-form-item>

    <el-form-item label="板块描述" prop="description">
      <el-input
        v-model="form.description"
        type="textarea"
        :rows="3"
        placeholder="10-100 字，介绍板块主题、定位、欢迎的内容"
        maxlength="100"
        show-word-limit
      />
    </el-form-item>

    <el-form-item label="板块口号（可选）" prop="slogan">
      <el-input v-model="form.slogan" placeholder="最多 30 字" maxlength="30" show-word-limit />
    </el-form-item>

    <el-form-item label="标签（可选）" prop="tags">
      <el-input v-model="form.tags" placeholder="逗号分隔，最多 3 个，每个 1-10 字" />
      <div class="form-tip">例如：游戏,洛克王国,怀旧</div>
    </el-form-item>

    <el-form-item label="板块头像 URL（可选）" prop="icon">
      <el-input v-model="form.icon" placeholder="图片 URL" />
    </el-form-item>

    <el-form-item>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
      <el-button @click="$emit('cancel')">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
/**
 * 板块申请表单（P2-M2）
 *
 * @emit submitted - 提交成功后触发，参数为 { id }
 * @emit cancel    - 用户点取消
 */
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { submitApplication } from '@/api/boardApplication';

const emit = defineEmits(['submitted', 'cancel']);

const formRef = ref(null);
const submitting = ref(false);
const form = reactive({
  name: '',
  description: '',
  slogan: '',
  tags: '',
  icon: ''
});

const rules = {
  name: [
    { required: true, message: '请填写板块名称', trigger: 'blur' },
    { min: 5, max: 20, message: '5-20 字', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请填写板块描述', trigger: 'blur' },
    { min: 10, max: 100, message: '10-100 字', trigger: 'blur' }
  ],
  slogan: [{ max: 30, message: '最多 30 字', trigger: 'blur' }],
  tags: [
    {
      validator: (_rule, value, callback) => {
        if (!value) return callback();
        const arr = value.split(',').map((s) => s.trim()).filter(Boolean);
        if (arr.length > 3) return callback(new Error('标签最多 3 个'));
        for (const t of arr) {
          if (t.length > 10) return callback(new Error('单个标签最多 10 字'));
        }
        callback();
      },
      trigger: 'blur'
    }
  ]
};

async function handleSubmit() {
  await formRef.value.validate();
  submitting.value = true;
  try {
    const data = await submitApplication({
      name: form.name.trim(),
      description: form.description.trim(),
      slogan: form.slogan || null,
      tags: form.tags || null,
      icon: form.icon || null
    });
    ElMessage.success('申请已提交，等待管理员审核');
    emit('submitted', data);
  } catch (e) {
    // 业务错由 request.js 已 toast，这里仅记录
    if (e?.message) ElMessage.error(e.message);
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.form-tip {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style>
