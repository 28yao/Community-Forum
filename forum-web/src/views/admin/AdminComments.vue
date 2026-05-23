<template>
  <el-card shadow="never">
    <div class="toolbar">
      <h2 style="margin:0;">评论管理</h2>
      <el-input-number
        v-model="postId"
        :min="1"
        placeholder="按帖子 ID 筛选"
        controls-position="right"
        style="width:200px;"
        @change="onFilter"
      />
    </div>

    <el-table v-loading="loading" :data="list" stripe style="margin-top:16px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="所属帖子" width="100">
        <template #default="{ row }">
          <el-link :href="`/post/${row.postId}`" target="_blank">#{{ row.postId }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="作者" width="120" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column label="层级" width="70">
        <template #default="{ row }">L{{ row.depth }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.deleted === 1" type="danger">已删除</el-tag>
          <el-tag v-else type="success">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="评论时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.deleted === 0" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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
  adminListComments, adminDeleteComment, adminRestoreComment
} from '@/api/admin';

const postId = ref(null);
const list = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const loading = ref(false);

async function fetchData() {
  loading.value = true;
  try {
    const res = await adminListComments({
      postId: postId.value || undefined,
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

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该评论？', '删除评论', { type: 'warning' });
    await adminDeleteComment(row.id);
    ElMessage.success('已删除');
    fetchData();
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message);
  }
}

async function handleRestore(row) {
  try {
    await adminRestoreComment(row.id);
    ElMessage.success('已恢复');
    fetchData();
  } catch (e) {
    ElMessage.error(e.message || '恢复失败');
  }
}

onMounted(fetchData);
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
</style>
