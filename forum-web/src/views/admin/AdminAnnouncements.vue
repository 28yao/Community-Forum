<template>
  <el-card shadow="never">
    <div class="toolbar">
      <h2 style="margin:0;">公告管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建公告</el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterScope" placeholder="作用域" clearable style="width:120px;" @change="load">
        <el-option label="全部" value="" />
        <el-option label="站点" value="site" />
        <el-option label="板块" value="board" />
      </el-select>
    </div>

    <el-table v-loading="loading" :data="list" stripe style="margin-top:16px;">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="作用域" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.scope === 'site'" type="primary">站点</el-tag>
          <el-tag v-else type="success">板块</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column label="置顶" width="70">
        <template #default="{ row }">
          <el-tag v-if="row.pinned" type="warning" size="small">置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortWeight" label="权重" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" size="small">显示</el-tag>
          <el-tag v-else type="info" size="small">隐藏</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '隐藏' : '显示' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > pageSize"
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      style="margin-top:16px; justify-content:center;"
      @current-change="p => { currentPage = p; load(); }"
    />

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑公告' : '新建公告'" width="560px" destroy-on-close @close="resetForm">
      <el-form :model="form" label-width="80px">
        <el-form-item label="作用域">
          <el-radio-group v-model="form.scope" :disabled="!!editingId">
            <el-radio value="site">站点</el-radio>
            <el-radio value="board">板块</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.scope === 'board'" label="板块">
          <el-select v-model="form.boardId" placeholder="选择板块" :disabled="!!editingId" style="width:100%;">
            <el-option v-for="b in boards" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="50" show-word-limit placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" maxlength="500" show-word-limit placeholder="公告内容（支持 HTML）" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.pinned" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="form.sortWeight" :min="0" :max="9999" />
          <span style="color:#999; margin-left:8px; font-size:12px;">越大越靠前</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ editingId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { adminListAnnouncements, adminCreateAnnouncement, adminUpdateAnnouncement, adminDeleteAnnouncement } from '@/api/announcement';
import { listBoards } from '@/api/board';

const loading = ref(false);
const list = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(15);
const filterScope = ref('');

const dialogVisible = ref(false);
const editingId = ref(null);
const submitting = ref(false);
const boards = ref([]);

const form = ref({
  scope: 'site',
  boardId: null,
  title: '',
  content: '',
  pinned: 0,
  sortWeight: 0
});

async function load() {
  loading.value = true;
  try {
    const res = await adminListAnnouncements({
      scope: filterScope.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    });
    list.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } catch {
    list.value = [];
  } finally {
    loading.value = false;
  }
}

async function loadBoards() {
  try {
    const res = await listBoards();
    boards.value = res.data || [];
  } catch { boards.value = []; }
}

function openCreate() {
  editingId.value = null;
  form.value = { scope: 'site', boardId: null, title: '', content: '', pinned: 0, sortWeight: 0 };
  dialogVisible.value = true;
}

function openEdit(row) {
  editingId.value = row.id;
  form.value = {
    scope: row.scope,
    boardId: row.boardId,
    title: row.title,
    content: row.content,
    pinned: row.pinned,
    sortWeight: row.sortWeight
  };
  dialogVisible.value = true;
}

function resetForm() {
  editingId.value = null;
}

async function handleSubmit() {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    ElMessage.warning('标题和内容不能为空');
    return;
  }
  submitting.value = true;
  try {
    if (editingId.value) {
      await adminUpdateAnnouncement(editingId.value, {
        title: form.value.title,
        content: form.value.content,
        pinned: form.value.pinned,
        sortWeight: form.value.sortWeight
      });
      ElMessage.success('已更新');
    } else {
      await adminCreateAnnouncement(form.value);
      ElMessage.success('已创建');
    }
    dialogVisible.value = false;
    await load();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  } finally {
    submitting.value = false;
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1;
  try {
    await adminUpdateAnnouncement(row.id, { status: newStatus });
    ElMessage.success(newStatus === 1 ? '已显示' : '已隐藏');
    await load();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除这条公告？', '提示', { type: 'warning' });
  } catch { return; }
  try {
    await adminDeleteAnnouncement(row.id);
    ElMessage.success('已删除');
    await load();
  } catch (e) {
    ElMessage.error(e.message || '删除失败');
  }
}

onMounted(() => {
  load();
  loadBoards();
});
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter-bar {
  margin-top: 12px;
  display: flex;
  gap: 12px;
}
</style>
