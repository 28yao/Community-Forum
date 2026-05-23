<template>
  <div class="pagination-wrapper" v-if="total > 0">
    <el-pagination
      background
      layout="prev, pager, next, total"
      :current-page="page"
      :page-size="size"
      :total="total"
      @current-change="handleChange"
    />
  </div>
</template>

<script setup>
const props = defineProps({
  page: { type: Number, default: 1 },
  size: { type: Number, default: 20 },
  total: { type: Number, default: 0 }
});
const emit = defineEmits(['update:page', 'change']);

function handleChange(p) {
  emit('update:page', p);
  emit('change', p);
}
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
