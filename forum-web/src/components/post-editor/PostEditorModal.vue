<template>
  <el-dialog
    v-model="editorStore.visible"
    title="发贴"
    width="680px"
    :close-on-click-modal="false"
    :before-close="handleBeforeClose"
    destroy-on-close
    class="post-editor-modal"
  >
    <div class="modal-header-bar">
      <el-avatar :size="28" :src="userStore.info?.avatar || ''">
        {{ userStore.info?.nickname?.charAt(0) || '?' }}
      </el-avatar>
      <span class="header-name">{{ userStore.info?.nickname }}</span>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="editor-form">
      <el-form-item label="发布到" prop="boardId">
        <BoardSelectDropdown
          v-model="form.boardId"
          :disabled="!!editorStore.lockedBoardId"
        />
      </el-form-item>

      <el-form-item label="标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="标题，5-31 字"
          maxlength="31"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="正文" prop="content">
        <PostEditor v-if="editorStore.visible" v-model="form.content" />
      </el-form-item>

      <el-form-item label="图片">
        <ImageUpload v-model="form.imageUrls" :max="9" />
      </el-form-item>

      <div class="modal-toolbar">
        <EmojiPicker @select="insertEmoji" />
      </div>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">发布</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import PostEditor from '@/components/post/PostEditor.vue';
import ImageUpload from '@/components/common/ImageUpload.vue';
import BoardSelectDropdown from '@/components/board/BoardSelectDropdown.vue';
import EmojiPicker from './EmojiPicker.vue';
import { usePostEditorStore } from '@/stores/postEditor';
import { useUserStore } from '@/stores/user';
import { createPost } from '@/api/post';

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
.modal-header-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}
.header-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.editor-form {
  margin-top: 8px;
}
.modal-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
