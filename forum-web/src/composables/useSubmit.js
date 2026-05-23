/**
 * useSubmit - 提交按钮的统一 loading + 防抖 hook
 *
 * 用法：
 *   const { loading, run } = useSubmit();
 *   <el-button :loading="loading" @click="run(async () => { await api.xxx(); })">提交</el-button>
 *
 * 特性：
 *   - run 调用期间 loading=true，按钮自动禁用，避免多次提交
 *   - run 调用期间再次点击直接忽略（不排队）
 *
 * 项目内大多数提交按钮已经手写过 loading 状态防抖；本 hook 用于后续新增或需统一行为的场景。
 */
import { ref } from 'vue';

export function useSubmit() {
  const loading = ref(false);

  async function run(fn) {
    if (loading.value) return;
    loading.value = true;
    try {
      return await fn();
    } finally {
      loading.value = false;
    }
  }

  return { loading, run };
}
