<template>
  <div class="post-editor">
    <Toolbar
      class="editor-toolbar"
      :editor="editorRef"
      :default-config="toolbarConfig"
      mode="default"
    />
    <Editor
      class="editor-content"
      v-model="valueHtml"
      :default-config="editorConfig"
      mode="default"
      @on-created="handleCreated"
    />
  </div>
</template>

<script setup>
import '@wangeditor/editor/dist/css/style.css';
import { onBeforeUnmount, ref, shallowRef, watch } from 'vue';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { uploadPostImage } from '@/api/post';
import { ElMessage } from 'element-plus';

const props = defineProps({
  modelValue: { type: String, default: '' }
});
const emit = defineEmits(['update:modelValue']);

const editorRef = shallowRef();
const valueHtml = ref(props.modelValue);

watch(() => props.modelValue, (v) => {
  if (v !== valueHtml.value) valueHtml.value = v;
});

watch(valueHtml, (v) => emit('update:modelValue', v));

const toolbarConfig = {
  excludeKeys: ['fullScreen', 'group-video']
};

const editorConfig = {
  placeholder: '请输入正文...',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file, insertFn) {
        try {
          const data = await uploadPostImage(file);
          insertFn(data.url, file.name, '');
        } catch (e) {
          ElMessage.error(e.message || '图片上传失败');
        }
      }
    }
  }
};

function handleCreated(editor) {
  editorRef.value = editor;
}

onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor) editor.destroy();
});
</script>

<style scoped>
.post-editor {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  z-index: 1;
}
.editor-toolbar {
  border-bottom: 1px solid #f0f0f0 !important;
  background: #fafafa;
}
.editor-toolbar :deep(.w-e-bar) {
  background: transparent;
}
.editor-content {
  height: 300px;
  overflow-y: auto;
}
.editor-content :deep(.w-e-text-container) {
  background: #fff;
}
.editor-content :deep(.w-e-text-placeholder) {
  color: #bbb;
  font-style: normal;
}
</style>
