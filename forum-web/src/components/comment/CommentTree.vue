<template>
  <div class="comment-tree">
    <div v-if="loading" v-loading="true" style="min-height:120px;"></div>
    <template v-else>
      <EmptyState v-if="!comments.length" description="还没有评论，来抢沙发吧" />
      <div v-for="top in comments" :key="top.id" class="comment-thread">
        <CommentItem
          :comment="top"
          :replying-to="replyingTo"
          @reply="onReply"
          @deleted="$emit('refresh')"
          @replied="onReplied"
        />
        <div v-if="top.children?.length" class="children">
          <CommentItem
            v-for="child in top.children"
            :key="child.id"
            :comment="child"
            :replying-to="replyingTo"
            @reply="onReply"
            @deleted="$emit('refresh')"
            @replied="onReplied"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import CommentItem from './CommentItem.vue';
import EmptyState from '@/components/common/EmptyState.vue';

defineProps({
  comments: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
});

const emit = defineEmits(['refresh']);

const replyingTo = ref(null);

function onReply(comment) {
  if (comment === null) {
    replyingTo.value = null;
    return;
  }
  replyingTo.value = replyingTo.value === comment.id ? null : comment.id;
}

function onReplied() {
  replyingTo.value = null;
  emit('refresh');
}
</script>

<style scoped>
.comment-thread {
  margin-bottom: 8px;
}
.children {
  margin-left: 42px;
  padding-left: 12px;
  border-left: 2px solid #f0f0f0;
}
</style>
