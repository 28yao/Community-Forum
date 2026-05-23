<template>
  <el-card shadow="never">
    <div class="toolbar">
      <h2 style="margin:0;">帖子管理</h2>
      <el-space>
        <el-select v-model="boardId" placeholder="所有版块" clearable style="width:160px;" @change="onFilter">
          <el-option v-for="b in appStore.boards" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="标题关键词"
          clearable
          style="width:240px;"
          @keyup.enter="onFilter"
          @clear="onFilter"
        />
      </el-space>
    </div>

    <el-table v-loading="loading" :data="list" stripe style="margin-top:16px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link :href="`/post/${row.id}`" target="_blank" :underline="false">
            <el-tag v-if="row.isPinned" type="danger" size="small" effect="dark" style="margin-right:6px;">置顶</el-tag>
            {{ row.title }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="作者" width="120">
        <template #default="{ row }">{{ row.author?.nickname }}</template>
      </el-table-column>
      <el-table-column prop="likeCount" label="赞" width="70" />
      <el-table-column prop="commentCount" label="评" width="70" />
      <el-table-column prop="viewCount" label="阅" width="70" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.deleted === 1" type="danger">已删除</el-tag>
          <el-tag v-else type="success">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="发布时间" width="180" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <template v-if="row.deleted === 0">
            <el-button
              size="small"
              :type="row.isPinned ? '' : 'warning'"
              @click="handlePin(row, !row.isPinned)"
            >{{ row.isPinned ? '取消置顶' : '置顶' }}</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
          <el-button v-else size="small" type="success" @click="handleRestore(row)">恢复</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      v-if="total > size"
      :page="page"
      :size="size"
      :total="total"
      @change="(p) => { page = p; fetchData(); }"
      style="margin-top:16px;"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import Pagination from '@/components/common/Pagination.vue';
import {
  adminListPosts, adminDeletePost, adminRestorePost, adminPinPost
} from '@/api/admin';
import { useAppStore } from '@/stores/app';

const appStore = useAppStore();

const boardId = ref(null);
const keyword = ref('');
const list = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const loading = ref(false);

async function fetchData() {
  loading.value = true;
  try {
    const res = await adminListPosts({
      boardId: boardId.value || undefined,
      keyword: keyword.value || undefined,
      page: page.value,
      size: size.value
    });
    list.value = res.list || [];
    total.value = res.total || 0;
  } catch (e) {
    ElMessage.error(e.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

function onFilter() {
  page.value = 1;
  fetchData();
}

async function handlePin(row, pinned) {
  try {
    await adminPinPost(row.id, pinned);
    ElMessage.success(pinned ? '已置顶' : '已取消置顶');
    fetchData();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除帖子《${row.title}》？`, '删除帖子', { type: 'warning' });
    await adminDeletePost(row.id);
    ElMessage.success('已删除');
    fetchData();
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message);
  }
}

async function handleRestore(row) {
  try {
    await adminRestorePost(row.id);
    ElMessage.success('已恢复');
    fetchData();
  } catch (e) {
    ElMessage.error(e.message || '恢复失败');
  }
}

onMounted(async () => {
  if (!appStore.boardsLoaded) await appStore.loadBoards();
  fetchData();
});
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
</style>
