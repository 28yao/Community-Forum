<template>
  <div class="post-detail-page" v-loading="loading">
    <el-card v-if="post">
      <div class="post-header">
        <h1 class="post-title">
          <el-tag v-if="post.isPinned" type="danger" size="small" effect="dark">置顶</el-tag>
          {{ post.title }}
          <el-tag v-if="post.isEdited" type="info" size="small">已编辑</el-tag>
        </h1>
        <div class="post-meta">
          <el-avatar :size="32" :src="post.author?.avatar || ''">
            {{ post.author?.nickname?.charAt(0) }}
          </el-avatar>
          <span class="author-name">{{ post.author?.nickname || '已注销' }}</span>
          <span class="meta-sep">·</span>
          <span>{{ formatTime(post.createdAt) }}</span>
          <span class="meta-sep">·</span>
          <router-link v-if="post.board?.id" :to="`/board/${post.board.id}`">{{ post.board.name }}</router-link>
          <span class="meta-sep">·</span>
          <span>{{ post.viewCount }} 浏览</span>
        </div>
        <div v-if="isAuthor" class="post-actions">
          <el-button size="small" @click="$router.push(`/post/${post.id}/edit`)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete">删除</el-button>
        </div>
      </div>

      <el-divider />

      <div class="post-content" v-html="post.content"></div>

      <div v-if="post.images?.length" class="post-images">
        <el-image
          v-for="(url, idx) in post.images"
          :key="idx"
          :src="url"
          fit="cover"
          :preview-src-list="post.images"
          :initial-index="idx"
          class="post-img"
        />
      </div>

      <el-divider />

      <div class="post-footer">
        <el-button size="default" :icon="Pointer" disabled>点赞 {{ post.likeCount || 0 }}（M4 实现）</el-button>
        <el-button size="default" :icon="Star" disabled>收藏（M4 实现）</el-button>
        <span class="comment-hint">评论功能将在 M4 阶段开放</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Pointer, Star } from '@element-plus/icons-vue';
import { getPostById, deletePost } from '@/api/post';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const postId = computed(() => Number(route.params.id));
const post = ref(null);
const loading = ref(true);

const isAuthor = computed(() =>
  userStore.isLoggedIn &&
  post.value &&
  post.value.author &&
  post.value.author.id === userStore.info?.id
);

async function load() {
  loading.value = true;
  try {
    post.value = await getPostById(postId.value);
  } catch (e) {
    ElMessage.error(e.message || '帖子不存在');
    router.replace('/');
  } finally {
    loading.value = false;
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这个帖子吗？删除后不可恢复', '删除帖子', {
      type: 'warning'
    });
    await deletePost(postId.value);
    ElMessage.success('已删除');
    router.push('/');
  } catch (e) {
    if (e?.message) ElMessage.error(e.message);
  }
}

function formatTime(t) {
  if (!t) return '';
  const d = new Date(typeof t === 'string' ? t.replace(' ', 'T') : t);
  return Number.isNaN(d.getTime()) ? t : d.toLocaleString('zh-CN', { hour12: false });
}

watch(postId, load);
onMounted(load);
</script>

<style scoped>
.post-detail-page {
  max-width: 900px;
  margin: 0 auto;
}
.post-title {
  margin: 0 0 12px 0;
  font-size: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #888;
  font-size: 13px;
  flex-wrap: wrap;
}
.author-name {
  color: #333;
  font-weight: 500;
}
.meta-sep {
  color: #ddd;
}
.post-actions {
  margin-top: 12px;
}
.post-content {
  font-size: 15px;
  line-height: 1.8;
  color: #222;
  word-break: break-word;
}
.post-content :deep(img) {
  max-width: 100%;
}
.post-images {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 8px;
}
.post-img {
  width: 100%;
  height: 150px;
  border-radius: 4px;
}
.post-footer {
  display: flex;
  align-items: center;
  gap: 12px;
}
.comment-hint {
  margin-left: auto;
  color: #999;
  font-size: 13px;
}
</style>
