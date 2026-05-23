<template>
  <el-select
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :disabled="disabled"
    placeholder="选择发布到哪个板块"
    filterable
    style="width: 100%;"
  >
    <el-option
      v-for="b in boards"
      :key="b.id"
      :label="b.name"
      :value="b.id"
    />
  </el-select>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useAppStore } from '@/stores/app';

const props = defineProps({
  modelValue: { type: [Number, null], default: null },
  disabled: { type: Boolean, default: false }
});

defineEmits(['update:modelValue']);

const appStore = useAppStore();
const boards = computed(() => appStore.boards.filter(b => b.status !== 0));

onMounted(() => {
  if (!appStore.boardsLoaded) appStore.loadBoards();
});
</script>
