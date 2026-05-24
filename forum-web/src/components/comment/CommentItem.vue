<template>
  <div class="comment-item" :class="level === 1 ? 'level-1' : 'level-2'">
    <el-avatar :size="level === 1 ? 40 : 32" :src="comment.author?.avatar || ''" class="avatar">
      {{ comment.author?.nickname?.charAt(0) }}
    </el-avatar>
    <div class="comment-body">
      <div class="comment-head">
        <span class="author">{{ comment.author?.nickname || '已注销' }}</span>
      </div>

      <div class="comment-content">
        <template v-if="level === 2 && comment.replyTo">
          <span class="reply-prefix">回复 </span>
          <span class="reply-target">{{ comment.replyTo.nickname }}</span>
          <span class="reply-colon"> : </span>
        </template>
        <span>{{ comment.content }}</span>
      </div>

      <div class="comment-foot">
        <span class="time">{{ formatCommentTime(comment.createdAt) }}</span>
        <span v-if="userStore.isLoggedIn" class="foot-btn" @click="$emit('reply', comment)">回复</span>
        <el-dropdown v-if="canDelete" trigger="click" @command="handleCommand">
          <span class="foot-btn foot-more">···</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="delete">删除</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <CommentForm
        v-if="replying"
        :post-id="comment.postId"
        :parent-id="comment.id"
        :reply-to-user-id="comment.author?.id"
        :placeholder="`回复 @${comment.author?.nickname}`"
        :rows="2"
        cancellable
        class="inline-reply-form"
        @submitted="onReplySubmitted"
        @cancel="emit('reply', null)"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { deleteComment } from '@/api/comment';
import { useUserStore } from '@/stores/user';
import CommentForm from './CommentForm.vue';

const props = defineProps({
  comment: { type: Object, required: true },
  replyingTo: { type: [Number, String, null], default: null },
  /** 1=顶级评论，2=楼中楼回复 */
  level: { type: Number, default: 1 }
});

const emit = defineEmits(['reply', 'deleted', 'replied']);

const userStore = useUserStore();

const replying = computed(() => props.replyingTo === props.comment.id);

const canDelete = computed(() =>
  userStore.isLoggedIn && (
    userStore.info?.id === props.comment.author?.id ||
    userStore.isAdmin
  )
);

function handleCommand(cmd) {
  if (cmd === 'delete') handleDelete();
}

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

function formatCommentTime(t) {
  if (!t) return '';
  const d = new Date(typeof t === 'string' ? t.replace(' ', 'T') : t);
  if (Number.isNaN(d.getTime())) return t;
  const now = new Date();
  const diffMs = now - d;
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return '刚刚';
  if (diffMin < 60) return `${diffMin}分钟前`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour}小时前`;
  const diffDay = Math.floor(diffHour / 24);
  if (diffDay < 7) return `${diffDay}天前`;
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${mm}-${dd}`;
}
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
}
.comment-item.level-1 {
  border-bottom: 1px solid #f0f0f0;
}
.comment-item.level-2 {
  padding: 10px 0;
}
.comment-item.level-2:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
}
.avatar {
  flex-shrink: 0;
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-head {
  margin-bottom: 6px;
}
.author {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}
.comment-content {
  font-size: 14px;
  line-height: 1.7;
  color: #333;
  word-break: break-word;
}
.reply-prefix {
  color: #666;
}
.reply-target {
  color: #4080ff;
  cursor: default;
}
.reply-colon {
  color: #666;
}
.comment-foot {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}
.foot-btn {
  cursor: pointer;
  user-select: none;
  transition: color 0.15s;
}
.foot-btn:hover {
  color: #4080ff;
}
.foot-more {
  font-size: 14px;
  letter-spacing: 1px;
  line-height: 1;
}
.inline-reply-form {
  margin-top: 10px;
}
</style>
