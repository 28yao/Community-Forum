<template>
  <div class="comment-item" :class="{ 'is-child': comment.depth === 2 }">
    <el-avatar :size="32" :src="comment.author?.avatar || ''">
      {{ comment.author?.nickname?.charAt(0) }}
    </el-avatar>
    <div class="comment-body">
      <div class="comment-meta">
        <span class="author">{{ comment.author?.nickname || '已注销' }}</span>
        <template v-if="comment.replyTo">
          <span class="reply-arrow">▸</span>
          <span class="reply-to">@{{ comment.replyTo.nickname }}</span>
        </template>
        <span class="time">{{ formatTime(comment.createdAt) }}</span>
      </div>
      <div class="comment-content">{{ comment.content }}</div>
      <div class="comment-actions">
        <el-button
          v-if="userStore.isLoggedIn"
          text
          size="small"
          @click="$emit('reply', comment)"
        >回复</el-button>
        <el-button
          v-if="canDelete"
          text
          size="small"
          type="danger"
          @click="handleDelete"
        >删除</el-button>
      </div>

      <!-- 内嵌回复输入框 -->
      <CommentForm
        v-if="replying"
        :post-id="comment.postId"
        :parent-id="comment.id"
        :reply-to-user-id="comment.author?.id"
        :placeholder="`回复 @${comment.author?.nickname}`"
        :rows="2"
        cancellable
        @submitted="onReplySubmitted"
        @cancel="replying = false"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { deleteComment } from '@/api/comment';
import { useUserStore } from '@/stores/user';
import CommentForm from './CommentForm.vue';

const props = defineProps({
  comment: { type: Object, required: true },
  replyingTo: { type: [Number, String, null], default: null }
});

const emit = defineEmits(['reply', 'deleted', 'replied']);

const userStore = useUserStore();
const replying = computed({
  get: () => props.replyingTo === props.comment.id,
  set: (v) => {
    if (!v) emit('reply', null);
  }
});

const canDelete = computed(() =>
  userStore.isLoggedIn && (
    userStore.info?.id === props.comment.author?.id ||
    userStore.isAdmin
  )
);

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除这条评论？', '删除评论', { type: 'warning' });
    await deleteComment(props.comment.id);
    ElMessage.success('已删除');
    emit('deleted');
  } catch (e) {
    if (e?.message && e !== 'cancel') ElMessage.error(e.message);
  }
}

function onReplySubmitted() {
  emit('replied');
}

function formatTime(t) {
  if (!t) return '';
  const d = new Date(typeof t === 'string' ? t.replace(' ', 'T') : t);
  return Number.isNaN(d.getTime()) ? t : d.toLocaleString('zh-CN', { hour12: false });
}
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-item.is-child {
  border-bottom: none;
  padding: 8px 0;
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #888;
}
.author {
  color: #333;
  font-weight: 500;
}
.reply-arrow {
  color: #ccc;
}
.reply-to {
  color: #1677ff;
}
.time {
  margin-left: auto;
  color: #aaa;
  font-size: 12px;
}
.comment-content {
  margin: 6px 0;
  color: #222;
  line-height: 1.6;
  word-break: break-word;
}
.comment-actions {
  display: flex;
  gap: 4px;
}
</style>
