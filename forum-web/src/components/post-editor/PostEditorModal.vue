<template>
  <el-dialog
    v-model="editorStore.visible"
    :show-close="false"
    width="680px"
    :close-on-click-modal="false"
    :before-close="handleBeforeClose"
    destroy-on-close
    class="post-editor-modal"
  >
    <template #header>
      <div class="modal-header">
        <div class="modal-header-left">
          <el-avatar :size="36" :src="userStore.info?.avatar || ''">
            {{ userStore.info?.nickname?.charAt(0) || '?' }}
          </el-avatar>
          <div class="header-info">
            <span class="header-name">{{ userStore.info?.nickname }}</span>
            <BoardSelectDropdown
              v-model="form.boardId"
              :disabled="!!editorStore.lockedBoardId"
              class="header-board-select"
            />
          </div>
        </div>
        <el-button :icon="Close" circle @click="handleClose" />
      </div>
    </template>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="editor-form">
      <el-form-item prop="title" class="title-item">
        <el-input
          v-model="form.title"
          placeholder="请输入标题（5-31 字）"
          maxlength="31"
          show-word-limit
          class="title-input"
        />
      </el-form-item>

      <el-form-item prop="content" class="content-item">
        <PostEditor v-if="editorStore.visible" v-model="form.content" />
      </el-form-item>
    </el-form>

    <div class="modal-bottom-bar">
      <div class="bottom-actions">
        <el-upload
          :show-file-list="false"
          :http-request="handleImageUpload"
          accept="image/*"
          multiple
        >
          <el-button :icon="Picture" circle />
        </el-upload>
        <EmojiPicker @select="insertEmoji" />
      </div>
      <div class="bottom-right">
        <span v-if="form.imageUrls.length" class="image-count">{{ form.imageUrls.length }}/9 图片</span>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit" class="publish-btn">
          发布
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Close, Picture } from '@element-plus/icons-vue';
import PostEditor from '@/components/post/PostEditor.vue';
import BoardSelectDropdown from '@/components/board/BoardSelectDropdown.vue';
import EmojiPicker from './EmojiPicker.vue';
import { usePostEditorStore } from '@/stores/postEditor';
import { useUserStore } from '@/stores/user';
import { createPost, uploadPostImage } from '@/api/post';

const router = useRouter();
const editorStore = usePostEditorStore();
const userStore = useUserStore();

const formRef = ref(null);
const submitting = ref(false);

const form = reactive({
  boardId: null,
  title: '',
  content: '',
  imageUrls: []
});

const rules = {
  boardId: [{ required: true, message: '请选择板块', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 31, message: '标题 5-31 字符', trigger: 'blur' }
  ],
  content: [{ required: true, message: '请输入正文', trigger: 'change' }]
};

// 锁定板块时自动填充
watch(() => editorStore.lockedBoardId, (id) => {
  if (id) form.boardId = id;
}, { immediate: true });

// 打开时重置表单
watch(() => editorStore.visible, (v) => {
  if (v) {
    form.boardId = editorStore.lockedBoardId || null;
    form.title = '';
    form.content = '';
    form.imageUrls = [];
  }
});

function isDirty() {
  return form.title.trim() || (form.content && form.content.replace(/<[^>]+>/g, '').trim()) || form.imageUrls.length;
}

async function handleBeforeClose(done) {
  if (isDirty()) {
    try {
      await ElMessageBox.confirm('内容未保存，确认关闭？', '提示', { type: 'warning' });
    } catch {
      return;
    }
  }
  done();
}

function handleClose() {
  if (isDirty()) {
    ElMessageBox.confirm('内容未保存，确认关闭？', '提示', { type: 'warning' })
      .then(() => editorStore.close())
      .catch(() => {});
  } else {
    editorStore.close();
  }
}

function insertEmoji(emoji) {
  form.content += emoji;
}

async function handleImageUpload({ file }) {
  if (form.imageUrls.length >= 9) {
    ElMessage.warning('最多上传 9 张图片');
    return;
  }
  try {
    const data = await uploadPostImage(file);
    form.imageUrls.push(data.url);
  } catch (e) {
    ElMessage.error(e.message || '图片上传失败');
  }
}

async function handleSubmit() {
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
  if (!form.content || form.content.replace(/<[^>]+>/g, '').trim() === '') {
    ElMessage.error('正文不能为空');
    return;
  }
  submitting.value = true;
  try {
    const data = await createPost({
      boardId: form.boardId,
      title: form.title,
      content: form.content,
      imageUrls: form.imageUrls
    });
    ElMessage.success('发布成功');
    editorStore.close();
    router.push(`/post/${data.id}`);
  } catch (e) {
    ElMessage.error(e.message || '发布失败');
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.post-editor-modal :deep(.el-dialog__header) {
  padding: 16px 20px 12px;
  margin: 0;
  border-bottom: 1px solid #f0f0f0;
}
.post-editor-modal :deep(.el-dialog__body) {
  padding: 16px 20px 0;
}
.post-editor-modal :deep(.el-dialog__footer) {
  display: none;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.modal-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.header-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1;
}
.header-board-select {
  width: 180px;
}
.header-board-select :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: #f5f5f5;
  border-radius: 16px;
  height: 28px;
}
.header-board-select :deep(.el-input__inner) {
  font-size: 12px;
  height: 28px;
  line-height: 28px;
}
.editor-form {
  margin-top: 4px;
}
.title-item {
  margin-bottom: 8px;
}
.title-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  border-bottom: 1px solid #e8e8e8;
  border-radius: 0;
  padding: 0;
}
.title-input :deep(.el-input__inner) {
  font-size: 18px;
  font-weight: 600;
  padding: 12px 0;
  height: auto;
  line-height: 1.4;
}
.content-item {
  margin-bottom: 0;
}
.content-item :deep(.el-form-item__content) {
  min-height: 200px;
}
.modal-bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  margin: 0 -20px;
}
.bottom-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}
.bottom-actions .el-button {
  border: none;
  background: transparent;
}
.bottom-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.image-count {
  font-size: 12px;
  color: #999;
  margin-right: 4px;
}
.publish-btn {
  min-width: 80px;
}
</style>
