<template>
  <div class="post-edit-page">
    <el-card v-loading="loading">
      <template #header>
        <h2 style="margin:0;">编辑帖子</h2>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <PostEditor v-model="form.content" />
        </el-form-item>
        <el-form-item label="图片">
          <ImageUpload v-model="form.imageUrls" :max="9" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import PostEditor from '@/components/post/PostEditor.vue';
import ImageUpload from '@/components/common/ImageUpload.vue';
import { getPostById, updatePost } from '@/api/post';

const route = useRoute();
const router = useRouter();

const postId = computed(() => Number(route.params.id));
const formRef = ref(null);
const loading = ref(true);
const submitting = ref(false);

const form = reactive({
  title: '',
  content: '',
  imageUrls: []
});

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题 2-100 字符', trigger: 'blur' }
  ],
  content: [{ required: true, message: '请输入正文', trigger: 'change' }]
};

onMounted(async () => {
  try {
    const data = await getPostById(postId.value);
    form.title = data.title;
    form.content = data.content;
    form.imageUrls = data.images || [];
  } catch (e) {
    ElMessage.error(e.message || '帖子不存在');
    router.replace('/');
  } finally {
    loading.value = false;
  }
});

async function handleSubmit() {
  await formRef.value.validate();
  submitting.value = true;
  try {
    await updatePost(postId.value, form);
    ElMessage.success('保存成功');
    router.push(`/post/${postId.value}`);
  } catch (e) {
    ElMessage.error(e.message || '保存失败');
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.post-edit-page {
  max-width: 900px;
  margin: 0 auto;
}
</style>
