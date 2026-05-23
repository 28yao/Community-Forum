<template>
  <el-dialog
    model-value="true"
    @update:model-value="$emit('close')"
    title="编辑板块"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form :model="form" label-width="80px">
      <el-form-item label="名称">
        <el-input :model-value="board.name" disabled />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="板块描述" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="图标 URL">
        <el-input v-model="form.icon" placeholder="图标链接（可选）" />
      </el-form-item>
      <el-form-item label="口号">
        <el-input v-model="form.slogan" placeholder="一句话口号（可选）" maxlength="50" />
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" maxlength="100" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { updateBoardOwner } from '@/api/board';

const props = defineProps({
  board: { type: Object, required: true }
});

const emit = defineEmits(['close', 'saved']);
const submitting = ref(false);

const form = reactive({
  description: props.board.description || '',
  icon: props.board.icon || '',
  slogan: props.board.slogan || '',
  tags: props.board.tags || ''
});

async function handleSubmit() {
  submitting.value = true;
  try {
    await updateBoardOwner(props.board.id, form);
    ElMessage.success('已保存');
    emit('saved');
  } catch (e) {
    ElMessage.error(e.message || '保存失败');
  } finally {
    submitting.value = false;
  }
}
</script>
