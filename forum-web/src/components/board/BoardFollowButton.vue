<template>
  <el-button
    :type="isFollowed ? 'default' : 'primary'"
    :loading="loading"
    :size="size"
    :plain="isFollowed"
    @click="handleClick"
  >
    {{ isFollowed ? '已关注' : '+ 关注' }}
  </el-button>
</template>

<script setup>
/**
 * 板块关注按钮（P2-M3）
 *
 * Props:
 * - board: 完整 board 对象（关注时用于乐观加入 followedBoards）
 * - size: el-button size 透传
 *
 * 行为：
 * - 未登录点击 → 跳登录页
 * - 已登录点击 → 触发 store.follow/unfollow（乐观更新 + 失败回滚 + toast）
 */
import { computed, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { useBoardFollowStore } from '@/stores/boardFollow';

const props = defineProps({
  board: { type: Object, required: true },
  size: { type: String, default: 'default' }
});

const userStore = useUserStore();
const followStore = useBoardFollowStore();
const router = useRouter();
const route = useRoute();
const loading = ref(false);

const isFollowed = computed(() => followStore.isFollowed(props.board.id));

async function handleClick() {
  if (!userStore.isLoggedIn) {
    router.push('/login?redirect=' + encodeURIComponent(route.fullPath));
    return;
  }
  loading.value = true;
  try {
    if (isFollowed.value) {
      await followStore.unfollow(props.board.id);
      ElMessage.success('已取消关注');
    } else {
      await followStore.follow(props.board);
      ElMessage.success('已关注');
    }
  } catch (e) {
    // 业务错（如关注上限）由 request.js 全局 toast，本地额外提示
    if (e && e.message) ElMessage.error(e.message);
  } finally {
    loading.value = false;
  }
}
</script>
