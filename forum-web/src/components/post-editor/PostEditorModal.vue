<template>
  <el-dialog
    v-model="editorStore.visible"
    :show-close="false"
    width="720px"
    :close-on-click-modal="false"
    :before-close="handleBeforeClose"
    destroy-on-close
    class="post-editor-modal"
  >
    <template #header>
      <div class="modal-header">
        <div class="modal-tabs">
          <span class="tab-item active">发贴</span>
        </div>
        <el-button :icon="Close" circle text @click="handleClose" />
      </div>
    </template>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="left" label-width="80px" class="editor-form">
      <el-form-item prop="boardId" label="发布到吧" class="board-item">
        <BoardSelectDropdown
          v-model="form.boardId"
          :disabled="!!editorStore.lockedBoardId"
          class="board-select"
          placeholder="选择吧"
        />
      </el-form-item>

      <el-form-item prop="title" class="title-item">
        <el-input
          v-model="form.title"
          placeholder="请输入完整贴子标题(5-31个字)"
          maxlength="31"
          class="title-input"
        >
          <template #suffix>
            <span class="required-tag">必填</span>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item prop="content" class="content-item">
        <el-input
          ref="contentRef"
          v-model="form.content"
          type="textarea"
          :rows="10"
          resize="none"
          placeholder="请输入正文（建议200-2000字）"
          class="content-textarea"
        />
      </el-form-item>

      <div v-if="form.imageUrls.length" class="upload-thumbs">
        <div v-for="(url, idx) in form.imageUrls" :key="idx" class="upload-thumb">
          <img :src="url" :alt="`图${idx + 1}`" />
          <span class="thumb-remove" @click="handleRemoveImage(idx)">×</span>
        </div>
      </div>

      <div class="quick-actions">
        <EmojiPicker class="qa-item" @select="insertText">
          <template #default="{ toggle }">
            <span class="qa-btn" @click="toggle"><span class="qa-ico">😊</span>表情</span>
          </template>
        </EmojiPicker>

        <span class="qa-btn" @click="handleMentionClick">
          <span class="qa-ico">@</span>用户
        </span>

        <span class="qa-btn" @click="handleTopicClick">
          <span class="qa-ico">#</span>话题
        </span>

        <el-upload
          :show-file-list="false"
          :http-request="handleImageUpload"
          accept="image/*"
          multiple
          class="qa-item"
        >
          <span class="qa-btn">
            <el-icon class="qa-ico"><Picture /></el-icon>图片
          </span>
        </el-upload>
      </div>
    </el-form>

    <div class="modal-bottom-bar">
      <div class="bottom-left">
        <span v-if="form.imageUrls.length" class="image-count">{{ form.imageUrls.length }}/9 图片</span>
      </div>
      <div class="bottom-right">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit" class="publish-btn">
          发布
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Close, Picture } from '@element-plus/icons-vue';
import BoardSelectDropdown from '@/components/board/BoardSelectDropdown.vue';
import EmojiPicker from './EmojiPicker.vue';
import { usePostEditorStore } from '@/stores/postEditor';
import { useUserStore } from '@/stores/user';
import { createPost, uploadPostImage } from '@/api/post';

const router = useRouter();
const editorStore = usePostEditorStore();
const userStore = useUserStore();

const formRef = ref(null);
const contentRef = ref(null);
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

watch(() => editorStore.lockedBoardId, (id) => {
  if (id) form.boardId = id;
}, { immediate: true });

watch(() => editorStore.visible, (v) => {
  if (v) {
    form.boardId = editorStore.lockedBoardId || null;
    form.title = '';
    form.content = '';
    form.imageUrls = [];
  }
});

function isDirty() {
  return form.title.trim() || form.content.trim() || form.imageUrls.length;
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

function insertText(text) {
  const el = contentRef.value?.textarea;
  if (!el) {
    form.content += text;
    return;
  }
  const start = el.selectionStart ?? form.content.length;
  const end = el.selectionEnd ?? form.content.length;
  form.content = form.content.slice(0, start) + text + form.content.slice(end);
  nextTick(() => {
    el.focus();
    const pos = start + text.length;
    el.setSelectionRange(pos, pos);
  });
}

function handleMentionClick() {
  ElMessage.info('@用户 功能开发中');
}

function handleTopicClick() {
  ElMessage.info('#话题 功能开发中');
}

function handleRemoveImage(idx) {
  form.imageUrls.splice(idx, 1);
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
  if (!form.content.trim()) {
    ElMessage.error('正文不能为空');
    return;
  }
  submitting.value = true;
  try {
    const data = await createPost({
      boardId: form.boardId,
      title: form.title,
      content: plainToHtml(form.content),
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

function plainToHtml(text) {
  const escaped = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
  return escaped
    .split(/\n{2,}/)
    .map(p => `<p>${p.replace(/\n/g, '<br>')}</p>`)
    .join('');
}
</script>

<style scoped>
.post-editor-modal :deep(.el-dialog) {
  border-radius: 12px;
}
.post-editor-modal :deep(.el-dialog__header) {
  padding: 16px 24px 0;
  margin: 0;
}
.post-editor-modal :deep(.el-dialog__body) {
  padding: 8px 24px 16px;
}
.post-editor-modal :deep(.el-dialog__footer) {
  display: none;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 12px;
}
.modal-tabs {
  display: flex;
  gap: 24px;
}
.tab-item {
  font-size: 16px;
  color: #999;
  padding: 4px 0;
  cursor: pointer;
  position: relative;
}
.tab-item.active {
  color: #1a1a1a;
  font-weight: 600;
}
.tab-item.active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -12px;
  width: 24px;
  height: 3px;
  background: #4080ff;
  border-radius: 2px;
  transform: translateX(-50%);
}

.editor-form {
  margin-top: 16px;
}
.board-item,
.title-item,
.content-item {
  margin-bottom: 16px;
}
.board-item :deep(.el-form-item__label),
.editor-form :deep(.el-form-item__label) {
  font-size: 14px;
  color: #1a1a1a;
  padding-right: 12px;
}
.board-select {
  width: 100%;
}
.board-select :deep(.el-input__wrapper) {
  background: #f5f6f7;
  box-shadow: none !important;
  border-radius: 8px;
  height: 40px;
}

.title-item :deep(.el-form-item__content) {
  margin-left: 0 !important;
}
.title-input :deep(.el-input__wrapper) {
  background: #fff;
  box-shadow: inset 0 0 0 1px #e8e8e8 !important;
  border-radius: 8px;
  height: 48px;
  padding: 0 16px;
}
.title-input :deep(.el-input__wrapper):hover {
  box-shadow: inset 0 0 0 1px #c0c4cc !important;
}
.title-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: inset 0 0 0 1px #4080ff !important;
}
.title-input :deep(.el-input__inner) {
  font-size: 15px;
  color: #1a1a1a;
}
.required-tag {
  color: #999;
  font-size: 13px;
}

.content-item :deep(.el-form-item__content) {
  margin-left: 0 !important;
}
.content-textarea :deep(.el-textarea__inner) {
  background: #fff;
  box-shadow: inset 0 0 0 1px #e8e8e8 !important;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.7;
  color: #1a1a1a;
  min-height: 200px;
}
.content-textarea :deep(.el-textarea__inner):hover {
  box-shadow: inset 0 0 0 1px #c0c4cc !important;
}
.content-textarea :deep(.el-textarea__inner):focus {
  box-shadow: inset 0 0 0 1px #4080ff !important;
}
.content-textarea :deep(.el-textarea__inner)::placeholder {
  color: #b0b3b8;
}

.upload-thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.upload-thumb {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f6f7;
}
.upload-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.thumb-remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 18px;
  height: 18px;
  line-height: 16px;
  text-align: center;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border-radius: 50%;
  font-size: 14px;
  cursor: pointer;
  user-select: none;
}
.thumb-remove:hover {
  background: rgba(0, 0, 0, 0.75);
}

.quick-actions {
  display: flex;
  align-items: center;
  gap: 28px;
  margin-top: 12px;
  padding: 4px 0;
}
.qa-item {
  display: inline-flex;
  align-items: center;
}
.qa-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #4e5969;
  cursor: pointer;
  user-select: none;
  transition: color 0.15s;
}
.qa-btn:hover {
  color: #4080ff;
}
.qa-ico {
  font-size: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.modal-bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0 4px;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
}
.bottom-left {
  display: flex;
  align-items: center;
}
.image-count {
  font-size: 12px;
  color: #999;
}
.bottom-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.publish-btn {
  min-width: 96px;
  border-radius: 20px;
  background: #4080ff;
  border-color: #4080ff;
}
.publish-btn:hover {
  background: #5a91ff;
  border-color: #5a91ff;
}
</style>
