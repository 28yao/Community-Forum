<template>
  <div v-if="displayImages.length" :class="['post-image-grid', `grid-${displayImages.length}`]">
    <div
      v-for="(url, idx) in displayImages"
      :key="idx"
      class="grid-cell"
      @click.stop="preview(idx)"
    >
      <el-image :src="url" fit="cover" lazy />
      <div v-if="idx === displayImages.length - 1 && hasMore" class="more-overlay">
        +{{ images.length - displayImages.length }}
      </div>
    </div>

    <!-- 预览（只在被点击时挂载） -->
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
 * 贴吧风列表卡片图片网格（P2-M4）
 *
 * - 1 张：单张大图 4:3
 * - 2 张：两列 1:1
 * - 3+ 张：三列 1:1:1，超出第 3 张在最后一格显示 +N 角标
 *
 * 点击图片用 el-image-viewer 全屏预览（覆盖卡片本身的跳详情点击）
 */
import { ref, computed } from 'vue';

const props = defineProps({
  images: { type: Array, default: () => [] },
  /** 列表卡片场景最多显示 3 张 */
  maxDisplay: { type: Number, default: 3 }
});

const displayImages = computed(() => (props.images || []).slice(0, props.maxDisplay));
const hasMore = computed(() => (props.images || []).length > props.maxDisplay);

const viewerVisible = ref(false);
const viewerIndex = ref(0);
function preview(idx) {
  viewerIndex.value = idx;
  viewerVisible.value = true;
}
</script>

<style scoped>
.post-image-grid {
  display: grid;
  gap: 4px;
  margin-top: 8px;
  border-radius: 6px;
  overflow: hidden;
}
.grid-1 {
  grid-template-columns: 1fr;
}
.grid-1 .grid-cell {
  aspect-ratio: 16 / 9;
  max-height: 400px;
}
.grid-2 {
  grid-template-columns: 1fr 1fr;
}
.grid-2 .grid-cell {
  aspect-ratio: 1 / 1;
}
.grid-3 {
  grid-template-columns: repeat(3, 1fr);
}
.grid-3 .grid-cell {
  aspect-ratio: 1 / 1;
}
.grid-cell {
  position: relative;
  cursor: zoom-in;
  background: #f5f7fa;
  overflow: hidden;
}
.grid-cell :deep(.el-image),
.grid-cell :deep(.el-image img) {
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
