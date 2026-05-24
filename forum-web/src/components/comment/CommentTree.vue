<template>
  <div class="comment-tree">
    <div v-if="loading" v-loading="true" class="loading-box"></div>
    <template v-else>
      <EmptyState v-if="!comments.length" description="还没有评论，来抢沙发吧" />
      <div v-for="top in comments" :key="top.id" class="comment-thread">
        <CommentItem
          :comment="top"
          :level="1"
          :replying-to="replyingTo"
          @reply="onReply"
          @deleted="$emit('refresh')"
          @replied="onReplied"
        />

        <div v-if="top.children?.length" class="replies-wrap">
          <CommentItem
            v-for="child in visibleChildren(top)"
            :key="child.id"
            :comment="child"
            :level="2"
            :replying-to="replyingTo"
            @reply="onReply"
            @deleted="$emit('refresh')"
            @replied="onReplied"
          />
          <button
            v-if="hiddenCount(top) > 0"
            type="button"
            class="expand-btn"
            @click="expandThread(top.id)"
          >
            展开 {{ hiddenCount(top) }} 条回复
          </button>
          <button
            v-else-if="isExpanded(top.id) && top.children.length > PREVIEW_COUNT"
            type="button"
            class="expand-btn"
            @click="collapseThread(top.id)"
          >
            收起回复
          </button>
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

const PREVIEW_COUNT = 2;
const replyingTo = ref(null);
const expandedThreads = ref(new Set());

function isExpanded(topId) {
  return expandedThreads.value.has(topId);
}

function visibleChildren(top) {
  const children = top.children || [];
  if (children.length <= PREVIEW_COUNT || isExpanded(top.id)) {
    return children;
  }
  return children.slice(0, PREVIEW_COUNT);
}

function hiddenCount(top) {
  const children = top.children || [];
  if (isExpanded(top.id) || children.length <= PREVIEW_COUNT) return 0;
  return children.length - PREVIEW_COUNT;
}

function expandThread(topId) {
  expandedThreads.value.add(topId);
  expandedThreads.value = new Set(expandedThreads.value);
}

function collapseThread(topId) {
  expandedThreads.value.delete(topId);
  expandedThreads.value = new Set(expandedThreads.value);
}

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
.loading-box {
  min-height: 120px;
}
.comment-thread:last-child .comment-item.level-1 {
  border-bottom: none;
}
.replies-wrap {
  margin: 0 0 8px 52px;
  padding: 4px 12px 8px;
  background: #f7f8fa;
  border-radius: 8px;
}
.expand-btn {
  display: block;
  width: 100%;
  padding: 8px 0 4px;
  border: none;
  background: none;
  color: #4080ff;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
}
.expand-btn:hover {
  color: #5a91ff;
}
</style>
