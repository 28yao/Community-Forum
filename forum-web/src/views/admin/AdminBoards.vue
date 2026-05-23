<template>
  <el-card shadow="never">
    <div class="toolbar">
      <h2 style="margin:0;">版块管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建版块</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe style="margin-top:16px;">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="版块名称" width="200" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="sortWeight" label="排序权重" width="100" />
      <el-table-column prop="postCount" label="帖子数" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">启用</el-tag>
          <el-tag v-else type="info">已禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button
            v-if="row.status === 1"
            size="small"
            type="warning"
            @click="toggleStatus(row, 0)"
          >禁用</el-button>
          <el-button v-else size="small" type="success" @click="toggleStatus(row, 1)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑版块' : '新建版块'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="版块名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="版块描述" />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="form.sortWeight" :min="0" :max="9999" />
          <span style="color:#999; margin-left:8px; font-size:12px;">越大越靠前</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import {
  adminListBoards, adminCreateBoard, adminUpdateBoard, adminSetBoardStatus
} from '@/api/admin';
import { useAppStore } from '@/stores/app';

const appStore = useAppStore();

const list = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const submitting = ref(false);
const editingId = ref(null);
const form = ref({ name: '', description: '', sortWeight: 0 });

async function fetchData() {
  loading.value = true;
  try {
    list.value = await adminListBoards();
  } catch (e) {
    ElMessage.error(e.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editingId.value = null;
  form.value = { name: '', description: '', sortWeight: 0 };
  dialogVisible.value = true;
}

function openEdit(row) {
  editingId.value = row.id;
  form.value = { name: row.name, description: row.description, sortWeight: row.sortWeight };
  dialogVisible.value = true;
}

async function submit() {
  if (!form.value.name?.trim()) {
    ElMessage.warning('请输入版块名称');
    return;
  }
  submitting.value = true;
  try {
    if (editingId.value) {
      await adminUpdateBoard(editingId.value, form.value);
      ElMessage.success('已更新');
    } else {
      await adminCreateBoard(form.value);
      ElMessage.success('已创建');
    }
    dialogVisible.value = false;
    fetchData();
    // 让前台 sidebar 下次刷新拉新数据
    appStore.boardsLoaded = false;
  } catch (e) {
    ElMessage.error(e.message || '保存失败');
  } finally {
    submitting.value = false;
  }
}

async function toggleStatus(row, status) {
  try {
    await ElMessageBox.confirm(
      status === 0 ? `禁用 ${row.name} 后，前台不再展示该版块` : `重新启用 ${row.name}？`,
      status === 0 ? '禁用版块' : '启用版块',
      { type: 'warning' }
    );
    await adminSetBoardStatus(row.id, status);
    ElMessage.success('状态已更新');
    fetchData();
    appStore.boardsLoaded = false;
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message);
  }
}

onMounted(fetchData);
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
