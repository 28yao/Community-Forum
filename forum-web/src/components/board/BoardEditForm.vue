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
      <div class="dialog-footer">
        <el-button type="danger" text :loading="deleting" @click="handleDelete">删除板块</el-button>
        <div>
          <el-button @click="$emit('close')">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { updateBoardOwner, deleteBoard } from '@/api/board';

const props = defineProps({
  board: { type: Object, required: true }
});

const emit = defineEmits(['close', 'saved']);
const router = useRouter();
const submitting = ref(false);
const deleting = ref(false);

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

async function handleDelete() {
  try {
    await ElMessageBox.confirm(
      `确定删除板块「${props.board.name}」？此操作不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    );
  } catch {
    return; // cancelled
  }
  deleting.value = true;
  try {
    await deleteBoard(props.board.id);
    ElMessage.success('板块已删除');
    emit('close');
    router.push('/');
  } catch (e) {
    ElMessage.error(e.message || '删除失败');
  } finally {
    deleting.value = false;
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
