<template>
  <div class="board-page">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="8" :md="6" :lg="5">
        <BoardSidebar />
      </el-col>
      <el-col :xs="24" :sm="16" :md="18" :lg="19">
        <div class="main-content">
          <div class="board-header">
            <div>
              <h2 style="margin:0;">{{ board?.name || '加载中' }}</h2>
              <p style="margin:4px 0 0 0; color:#666;">{{ board?.description }}</p>
              <p v-if="board?.followerCount != null" style="margin:4px 0 0 0; color:#999; font-size: 12px;">
                关注 {{ board.followerCount }} · 帖子 {{ board.postCount }}
              </p>
            </div>
            <div class="board-actions">
              <BoardFollowButton v-if="board" :board="board" />
              <el-button v-if="userStore.isLoggedIn" type="primary" @click="goCreate">发帖</el-button>
            </div>
          </div>
          <el-divider />
          <PostList ref="listRef" :board-id="boardId" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AppSidebar from '@/components/layout/AppSidebar.vue';
import BoardSidebar from '@/components/layout/BoardSidebar.vue';
import PostList from '@/components/post/PostList.vue';
import BoardFollowButton from '@/components/board/BoardFollowButton.vue';
import { useUserStore } from '@/stores/user';
import { useBoardFollowStore } from '@/stores/boardFollow';
import { usePostEditorStore } from '@/stores/postEditor';
import { getBoardById } from '@/api/board';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const followStore = useBoardFollowStore();
const postEditorStore = usePostEditorStore();

const boardId = computed(() => Number(route.params.id));
const board = ref(null);
const listRef = ref(null);

async function load() {
  try {
    board.value = await getBoardById(boardId.value);
    // 登录态首次进板块页时拉关注列表，便于按钮显示正确状态
    if (userStore.isLoggedIn && !followStore.followedLoaded) {
      await followStore.loadFollowed();
    }
  } catch (e) {
    ElMessage.error(e.message || '版块不存在');
    router.replace('/');
  }
}

watch(boardId, load);
onMounted(load);

function goCreate() {
  postEditorStore.open({ boardId: boardId.value });
}
</script>

<style scoped>
.main-content {
  background: #fff;
  border-radius: 6px;
  padding: 24px;
}
.board-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.board-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>
