<template>
  <div class="emoji-picker-wrapper">
    <el-popover
      placement="bottom-start"
      :width="350"
      trigger="click"
      :visible="show"
      @update:visible="show = $event"
    >
      <template #reference>
        <el-button text size="small" @click="show = !show" title="表情">
          <span style="font-size: 18px;">&#x1F600;</span>
        </el-button>
      </template>
      <Picker
        :native="true"
        :hide-search="false"
        :disable-skin-tones="true"
        @select="onSelect"
      />
    </el-popover>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import Picker from 'vue3-emoji-picker';
import 'vue3-emoji-picker/css';

const emit = defineEmits(['select']);
const show = ref(false);

function onSelect(emoji) {
  emit('select', emoji.i);
  show.value = false;
}
</script>

<style scoped>
.emoji-picker-wrapper {
  display: inline-block;
}
</style>
