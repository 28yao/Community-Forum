<template>
  <div class="comment-form">
    <el-input
      v-model="content"
      type="textarea"
      :rows="rows"
      :maxlength="1000"
      show-word-limit
      :placeholder="placeholder"
    />
    <div class="form-actions">
      <el-button v-if="cancellable" size="small" @click="$emit('cancel')">取消</el-button>
      <el-button size="small" type="primary" :loading="loading" @click="submit">
        {{ loading ? '提交中...' : '发表' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { createComment } from '@/api/comment';

const props = defineProps({
  postId: { type: [Number, String], required: true },
  parentId: { type: [Number, String], default: null },
  replyToUserId: { type: [Number, String], default: null },
  placeholder: { type: String, default: '说点什么吧...' },
  rows: { type: Number, default: 3 },
  cancellable: { type: Boolean, default: false }
});

const emit = defineEmits(['submitted', 'cancel']);

const content = ref('');
const loading = ref(false);

async function submit() {
  const text = content.value.trim();
  if (!text) {
    ElMessage.warning('请输入评论内容');
    return;
  }
  loading.value = true;
  try {
    const body = { content: text };
    if (props.parentId) body.parentId = props.parentId;
    if (props.replyToUserId) body.replyToUserId = props.replyToUserId;
    await createComment(props.postId, body);
    content.value = '';
    ElMessage.success('评论已发表');
    emit('submitted');
  } catch (e) {
    ElMessage.error(e.message || '评论失败');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.comment-form {
  margin-bottom: 12px;
}
.form-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
