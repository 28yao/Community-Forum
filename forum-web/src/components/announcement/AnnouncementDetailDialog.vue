<template>
  <el-dialog
    v-model="visible"
    :title="announcement?.title"
    width="500px"
    append-to-body
    destroy-on-close
    class="announcement-detail-dialog"
  >
    <div class="announcement-detail-scroll">
      <div class="announcement-detail-body" v-html="formatRichContent(announcement?.content)" />
    </div>
    <p v-if="showDate && announcement?.updatedAt" class="announcement-detail-date">
      {{ formatDate(announcement.updatedAt) }}
    </p>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue';
import { formatRichContent } from '@/utils/richText';

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  announcement: { type: Object, default: null },
  showDate: { type: Boolean, default: false }
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
});

function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  const now = new Date();
  const diffMs = now - d;
  const diffMin = Math.floor(diffMs / 60000);
  const diffHour = Math.floor(diffMs / 3600000);
  const diffDay = Math.floor(diffMs / 86400000);
  if (diffMin < 1) return '刚刚';
  if (diffMin < 60) return diffMin + ' 分钟前';
  if (diffHour < 24) return diffHour + ' 小时前';
  if (diffDay < 30) return diffDay + ' 天前';
  return d.toLocaleDateString('zh-CN');
}
</script>

<style scoped>
.announcement-detail-scroll {
  max-height: 600px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #e0e0e0 transparent;
}
.announcement-detail-scroll::-webkit-scrollbar {
  width: 6px;
}
.announcement-detail-scroll::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 3px;
}
.announcement-detail-body {
  font-size: 14px;
  line-height: 1.7;
  color: #333;
  word-break: break-word;
}
.announcement-detail-body :deep(p) {
  margin: 0 0 8px;
}
.announcement-detail-body :deep(p:last-child) {
  margin-bottom: 0;
}
.announcement-detail-body :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}
.announcement-detail-date {
  margin: 12px 0 0;
  font-size: 11px;
  color: #bbb;
}
</style>

<style>
.announcement-detail-dialog {
  width: 500px;
  max-width: 500px;
}
.announcement-detail-dialog .el-dialog__body {
  padding-top: 8px;
}
</style>
