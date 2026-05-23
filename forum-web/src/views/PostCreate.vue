<template>
  <div class="post-create-page">
    <el-card>
      <template #header>
        <h2 style="margin:0;">发布新帖</h2>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="版块" prop="boardId">
          <el-select v-model="form.boardId" placeholder="请选择版块" style="width:240px;">
            <el-option
              v-for="b in appStore.boards"
              :key="b.id"
              :label="b.name"
              :value="b.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="标题，2-100 字" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <PostEditor v-model="form.content" />
        </el-form-item>
        <el-form-item label="图片">
          <ImageUpload v-model="form.imageUrls" :max="9" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">发布</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import PostEditor from '@/components/post/PostEditor.vue';
import ImageUpload from '@/components/common/ImageUpload.vue';
import { useAppStore } from '@/stores/app';
import { createPost } from '@/api/post';

const route = useRoute();
const router = useRouter();
const appStore = useAppStore();

const formRef = ref(null);
const submitting = ref(false);

const form = reactive({
  boardId: null,
  title: '',
  content: '',
  imageUrls: []
});

const rules = {
  boardId: [{ required: true, message: '请选择版块', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题 2-100 字符', trigger: 'blur' }
  ],
  content: [{ required: true, message: '请输入正文', trigger: 'change' }]
};

onMounted(async () => {
  if (!appStore.boardsLoaded) await appStore.loadBoards();
  // 从 query 预填版块
  const qb = Number(route.query.boardId);
  if (qb) form.boardId = qb;
});

async function handleSubmit() {
  await formRef.value.validate();
  if (!form.content || form.content.replace(/<[^>]+>/g, '').trim() === '') {
    ElMessage.error('正文不能为空');
    return;
  }
  submitting.value = true;
  try {
    const data = await createPost(form);
    ElMessage.success('发布成功');
    router.push(`/post/${data.id}`);
  } catch (e) {
    ElMessage.error(e.message || '发布失败');
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.post-create-page {
  max-width: 900px;
  margin: 0 auto;
}
</style>
