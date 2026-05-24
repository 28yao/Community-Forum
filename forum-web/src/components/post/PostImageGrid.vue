<template>
  <div
    v-if="displayImages.length"
    :class="['post-image-grid', isSingle ? 'grid-single' : 'grid-multi']"
  >
    <div
      v-for="(url, idx) in displayImages"
      :key="idx"
      class="grid-cell"
      @click.stop="preview(idx)"
    >
      <el-image :src="url" :fit="isSingle ? 'contain' : 'cover'" lazy />
      <div v-if="!isSingle && idx === displayImages.length - 1 && hasMore" class="more-overlay">
        +{{ images.length - displayImages.length }}
      </div>
    </div>

    <el-image-viewer
      v-if="viewerVisible"
      :url-list="images"
      :initial-index="viewerIndex"
      :hide-on-click-modal="true"
      @close="viewerVisible = false"
    />
  </div>
</template>

<script setup>
/**
 * 帖子图片网格（列表卡片 + 详情页共用）
 *
 * - 1 张：原比例，最大宽度 = 容器宽度的 1/3
 * - 2+ 张：1:1 方格横向排列，每格占容器 1/3 宽，一行最多 3 张
 */
import { ref, computed } from 'vue';

const props = defineProps({
  images: { type: Array, default: () => [] },
  /** list：列表最多展示 maxDisplay 张；detail：详情页展示全部 */
  variant: { type: String, default: 'list', validator: (v) => ['list', 'detail'].includes(v) },
  maxDisplay: { type: Number, default: 3 }
});

const images = computed(() => props.images || []);
const isSingle = computed(() => images.value.length === 1);
const displayImages = computed(() => {
  if (props.variant === 'detail' || isSingle.value) {
    return images.value;
  }
  return images.value.slice(0, props.maxDisplay);
});
const hasMore = computed(() =>
  props.variant === 'list' && images.value.length > props.maxDisplay
);

const viewerVisible = ref(false);
const viewerIndex = ref(0);
function preview(idx) {
  viewerIndex.value = idx;
  viewerVisible.value = true;
}
</script>

<style scoped>
.post-image-grid {
  --cell-max-width: calc(100% / 3);
  margin-top: 8px;
}

/* 单图：原比例，宽不超过容器 1/3 */
.grid-single {
  display: block;
}
.grid-single .grid-cell {
  display: inline-block;
  max-width: var(--cell-max-width);
  vertical-align: top;
  cursor: zoom-in;
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}
.grid-single .grid-cell :deep(.el-image) {
  display: block;
  width: 100%;
  height: auto;
}
.grid-single .grid-cell :deep(.el-image img) {
  width: 100%;
  height: auto;
  display: block;
  object-fit: contain;
}

/* 多图：1:1 方格，左对齐紧挨排列，每格宽 (100% - gap) / 3 */
.grid-multi {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: flex-start;
  align-content: flex-start;
}
.grid-multi .grid-cell {
  position: relative;
  flex: 0 0 calc((100% - 8px) / 3);
  width: calc((100% - 8px) / 3);
  aspect-ratio: 1 / 1;
  cursor: zoom-in;
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}
.grid-multi .grid-cell :deep(.el-image),
.grid-multi .grid-cell :deep(.el-image img) {
  width: 100%;
  height: 100%;
  display: block;
}

.more-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  pointer-events: none;
}
</style>
