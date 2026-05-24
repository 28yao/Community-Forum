<template>
  <div class="board-announcement-panel">
    <div class="panel-header">
      <h3>公告管理</h3>
      <el-button type="primary" size="small" @click="showCreate = true">发布公告</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="3" animated />
    <template v-else-if="announcements.length">
      <div v-for="a in announcements" :key="a.id" class="announcement-row">
        <div class="row-info">
          <el-tag v-if="a.pinned" size="small" type="warning">置顶</el-tag>
          <span class="row-title">{{ a.title }}</span>
          <span class="row-time">{{ formatTime(a.createdAt) }}</span>
        </div>
        <div class="row-actions">
          <el-button text size="small" @click="editAnnouncement(a)">编辑</el-button>
          <el-button text size="small" type="danger" @click="handleDelete(a)">删除</el-button>
        </div>
      </div>
    </template>
    <el-empty v-else description="暂无公告" :image-size="40" />

    <!-- 创建/编辑弹窗 -->
    <el-dialog
      v-model="showCreate"
      :title="editing ? '编辑公告' : '发布公告'"
      width="520px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="50" show-word-limit placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="8" maxlength="1000" show-word-limit placeholder="公告内容" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.pinned" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ editing ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listBoardAnnouncements, createBoardAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/announcement';

const props = defineProps({
  boardId: { type: Number, required: true }
});

const loading = ref(false);
const announcements = ref([]);
const showCreate = ref(false);
const editing = ref(null);
const submitting = ref(false);

const form = ref({ title: '', content: '', pinned: 0 });

async function load() {
  loading.value = true;
  try {
    const res = await listBoardAnnouncements(props.boardId, { page: 1, size: 50 });
    announcements.value = res.data?.records || [];
  } catch {
    announcements.value = [];
  } finally {
    loading.value = false;
  }
}

function editAnnouncement(a) {
  editing.value = a;
  form.value = { title: a.title, content: a.content, pinned: a.pinned };
  showCreate.value = true;
}

function resetForm() {
  editing.value = null;
  form.value = { title: '', content: '', pinned: 0 };
}

async function handleSubmit() {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    ElMessage.warning('标题和内容不能为空');
    return;
  }
  submitting.value = true;
  try {
    if (editing.value) {
      await updateAnnouncement(editing.value.id, form.value);
      ElMessage.success('已更新');
    } else {
      await createBoardAnnouncement(props.boardId, form.value);
      ElMessage.success('已发布');
    }
    showCreate.value = false;
    await load();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(a) {
  try {
    await ElMessageBox.confirm('确认删除这条公告？', '提示', { type: 'warning' });
  } catch { return; }
  try {
    await deleteAnnouncement(a.id);
    ElMessage.success('已删除');
    await load();
  } catch (e) {
    ElMessage.error(e.message || '删除失败');
  }
}

function formatTime(t) {
  if (!t) return '';
  return t.substring(0, 10);
}

onMounted(load);
</script>

<style scoped>
.board-announcement-panel {
  background: #fff;
  border-radius: 6px;
  padding: 16px;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.panel-header h3 {
  margin: 0;
  font-size: 16px;
}
.announcement-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.row-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}
.row-title {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.row-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}
.row-actions {
  flex-shrink: 0;
  margin-left: 12px;
}
</style>
