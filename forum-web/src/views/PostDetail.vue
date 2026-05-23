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
        <el-button
          :type="post.liked ? 'primary' : 'default'"
          :icon="Pointer"
          :loading="likeLoading"
          @click="handleLikeToggle"
        >
          {{ post.liked ? '已赞' : '点赞' }} {{ post.likeCount || 0 }}
        </el-button>
        <el-button
          :type="post.favorited ? 'warning' : 'default'"
          :icon="Star"
          :loading="favLoading"
          @click="handleFavToggle"
        >
          {{ post.favorited ? '已收藏' : '收藏' }}
        </el-button>
        <span class="comment-stat">{{ post.commentCount || 0 }} 条评论</span>
      </div>
    </el-card>

    <el-card v-if="post" class="comments-card" shadow="never">
      <h3 style="margin:0 0 12px 0;">评论 ({{ post.commentCount || 0 }})</h3>
      <CommentForm
        v-if="userStore.isLoggedIn"
        :post-id="postId"
        placeholder="说说你的看法..."
        @submitted="loadComments"
      />
      <el-alert
        v-else
        type="info"
        :closable="false"
        show-icon
      >
        <template #title>
          <router-link to="/login">登录</router-link> 后即可评论
        </template>
      </el-alert>

      <el-divider />

      <CommentTree
        :comments="comments"
        :loading="commentsLoading"
        @refresh="loadComments"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Pointer, Star } from '@element-plus/icons-vue';
import { getPostById, deletePost } from '@/api/post';
import { like, unlike } from '@/api/like';
import { favorite, unfavorite } from '@/api/favorite';
import { listComments } from '@/api/comment';
import { useUserStore } from '@/stores/user';
import CommentForm from '@/components/comment/CommentForm.vue';
import CommentTree from '@/components/comment/CommentTree.vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const postId = computed(() => Number(route.params.id));
const post = ref(null);
const loading = ref(true);
const likeLoading = ref(false);
const favLoading = ref(false);

const comments = ref([]);
const commentsLoading = ref(false);

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
    loadComments();
  } catch (e) {
    ElMessage.error(e.message || '帖子不存在');
    router.replace('/');
  } finally {
    loading.value = false;
  }
}

async function loadComments() {
  commentsLoading.value = true;
  try {
    comments.value = await listComments(postId.value);
    // 重新拉一次帖子以更新评论数
    if (post.value) {
      const fresh = await getPostById(postId.value);
      post.value.commentCount = fresh.commentCount;
    }
  } catch (e) {
    comments.value = [];
  } finally {
    commentsLoading.value = false;
  }
}

function requireLogin() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return false;
  }
  return true;
}

async function handleLikeToggle() {
  if (!requireLogin() || likeLoading.value) return;
  likeLoading.value = true;
  const wasLiked = post.value.liked;
  try {
    if (wasLiked) {
      await unlike(postId.value);
      post.value.liked = false;
      post.value.likeCount = Math.max(0, (post.value.likeCount || 0) - 1);
    } else {
      await like(postId.value);
      post.value.liked = true;
      post.value.likeCount = (post.value.likeCount || 0) + 1;
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  } finally {
    likeLoading.value = false;
  }
}

async function handleFavToggle() {
  if (!requireLogin() || favLoading.value) return;
  favLoading.value = true;
  try {
    if (post.value.favorited) {
      await unfavorite(postId.value);
      post.value.favorited = false;
      ElMessage.success('已取消收藏');
    } else {
      await favorite(postId.value);
      post.value.favorited = true;
      ElMessage.success('已收藏');
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  } finally {
    favLoading.value = false;
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
    if (e?.message && e !== 'cancel') ElMessage.error(e.message);
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
.comment-stat {
  margin-left: auto;
  color: #888;
  font-size: 13px;
}
.comments-card {
  margin-top: 16px;
}
</style>
