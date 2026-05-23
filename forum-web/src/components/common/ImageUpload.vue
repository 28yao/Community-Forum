<template>
  <div class="image-upload">
    <div class="image-list">
      <div
        v-for="(url, idx) in modelValue"
        :key="idx"
        class="image-item"
      >
        <el-image :src="resolveUrl(url)" fit="cover" class="thumb" />
        <div class="image-mask">
          <el-icon @click="handleRemove(idx)"><Delete /></el-icon>
        </div>
      </div>

      <el-upload
        v-if="modelValue.length < max"
        :show-file-list="false"
        :before-upload="handleUpload"
        accept="image/*"
        :disabled="uploading"
        class="uploader"
      >
        <div class="uploader-trigger">
          <el-icon v-if="uploading" class="is-loading"><Loading /></el-icon>
          <el-icon v-else><Plus /></el-icon>
        </div>
      </el-upload>
    </div>
    <div class="image-tip">最多 {{ max }} 张，单图不超过 5MB；支持 JPG / PNG / GIF</div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Delete, Loading } from '@element-plus/icons-vue';
import { uploadPostImage } from '@/api/post';

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  max: { type: Number, default: 9 }
});
const emit = defineEmits(['update:modelValue']);

const uploading = ref(false);

function resolveUrl(url) {
  if (!url) return '';
  if (url.startsWith('http')) return url;
  // 后端返回 /static/uploads/... ，dev 走 vite 代理
  return url;
}

async function handleUpload(file) {
  if (props.modelValue.length >= props.max) {
    ElMessage.warning(`最多上传 ${props.max} 张`);
    return false;
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('单图大小不能超过 5MB');
    return false;
  }
  uploading.value = true;
  try {
    const data = await uploadPostImage(file);
    emit('update:modelValue', [...props.modelValue, data.url]);
  } catch (e) {
    ElMessage.error(e.message || '上传失败');
  } finally {
    uploading.value = false;
  }
  return false;
}

function handleRemove(idx) {
  const next = [...props.modelValue];
  next.splice(idx, 1);
  emit('update:modelValue', next);
}
</script>

<style scoped>
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.image-item, .uploader-trigger {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}
.thumb {
  width: 100%;
  height: 100%;
  display: block;
}
.image-mask {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
}
.image-item:hover .image-mask { opacity: 1; }
.uploader-trigger {
  border: 1px dashed #d9d9d9;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #999;
  font-size: 24px;
}
.uploader-trigger:hover { border-color: #1677ff; color: #1677ff; }
.image-tip {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}
</style>
