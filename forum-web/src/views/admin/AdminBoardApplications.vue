<template>
  <div class="admin-board-applications">
    <div class="page-header">
      <h2 style="margin:0;">板块申请审核</h2>
      <el-radio-group v-model="statusFilter" @change="refresh">
        <el-radio-button :label="1">待审核</el-radio-button>
        <el-radio-button :label="2">已通过</el-radio-button>
        <el-radio-button :label="3">已驳回</el-radio-button>
        <el-radio-button :label="null">全部</el-radio-button>
      </el-radio-group>
    </div>

    <el-table v-loading="loading" :data="list" stripe style="margin-top: 16px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="板块名" width="180" />
      <el-table-column prop="applicantUserId" label="申请人 ID" width="120" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">详情</el-button>
          <template v-if="row.status === 1">
            <el-button size="small" type="success" @click="approve(row)">通过</el-button>
            <el-button size="small" type="danger" @click="openReject(row)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > size"
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      layout="prev, pager, next, total"
      @current-change="load"
      style="justify-content: center; margin-top: 16px;"
    />

    <!-- 详情 Drawer -->
    <el-drawer v-model="detailVisible" title="申请详情" size="480px">
      <div v-if="current">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请 ID">{{ current.id }}</el-descriptions-item>
          <el-descriptions-item label="申请人 user_id">{{ current.applicantUserId }}</el-descriptions-item>
          <el-descriptions-item label="板块名">{{ current.name }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ current.description }}</el-descriptions-item>
          <el-descriptions-item label="口号">{{ current.slogan || '—' }}</el-descriptions-item>
          <el-descriptions-item label="标签">{{ current.tags || '—' }}</el-descriptions-item>
          <el-descriptions-item label="头像">{{ current.icon || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(current.status)" size="small">{{ statusText(current.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatTime(current.createdAt) }}</el-descriptions-item>
          <template v-if="current.status !== 1">
            <el-descriptions-item label="审核人 admin_id">{{ current.reviewerAdminId || '—' }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ formatTime(current.reviewedAt) }}</el-descriptions-item>
          </template>
          <el-descriptions-item v-if="current.status === 2" label="生成板块 ID">{{ current.boardId }}</el-descriptions-item>
          <el-descriptions-item v-if="current.status === 3" label="驳回原因">{{ current.rejectReason }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="current.status === 1" style="margin-top: 20px; text-align: right;">
          <el-button type="success" @click="approve(current)">通过并创建板块</el-button>
          <el-button type="danger" @click="openReject(current)">驳回</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectVisible" title="驳回申请" width="500px">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-position="top">
        <el-form-item label="驳回原因" prop="rejectReason">
          <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="3" placeholder="向申请人说明原因，最多 200 字" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/** 管理员板块申请审核（P2-M2） */
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  adminListApplications,
  adminApproveApplication,
  adminRejectApplication
} from '@/api/boardApplication';

const list = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const loading = ref(false);
const statusFilter = ref(1);

const detailVisible = ref(false);
const current = ref(null);

const rejectVisible = ref(false);
const rejecting = ref(false);
const rejectFormRef = ref(null);
const rejectForm = ref({ rejectReason: '' });
const rejectRules = {
  rejectReason: [
    { required: true, message: '请填写驳回原因', trigger: 'blur' },
    { max: 200, message: '最多 200 字', trigger: 'blur' }
  ]
};

async function load() {
  loading.value = true;
  try {
    const data = await adminListApplications(statusFilter.value, page.value, size.value);
    list.value = data.records || [];
    total.value = data.total || 0;
  } finally {
    loading.value = false;
  }
}
function refresh() {
  page.value = 1;
  load();
}
function openDetail(row) {
  current.value = row;
  detailVisible.value = true;
}

async function approve(row) {
  try {
    await ElMessageBox.confirm(
      `确认通过申请「${row.name}」？通过后将自动创建板块，申请人成为吧主。`,
      '审核通过',
      { type: 'success' }
    );
  } catch {
    return;
  }
  try {
    await adminApproveApplication(row.id);
    ElMessage.success('已通过');
    detailVisible.value = false;
    load();
  } catch (e) {
    if (e?.message) ElMessage.error(e.message);
  }
}

function openReject(row) {
  current.value = row;
  rejectForm.value.rejectReason = '';
  rejectVisible.value = true;
}

async function confirmReject() {
  await rejectFormRef.value.validate();
  rejecting.value = true;
  try {
    await adminRejectApplication(current.value.id, rejectForm.value.rejectReason);
    ElMessage.success('已驳回');
    rejectVisible.value = false;
    detailVisible.value = false;
    load();
  } catch (e) {
    if (e?.message) ElMessage.error(e.message);
  } finally {
    rejecting.value = false;
  }
}

function statusText(s) {
  if (s === 1) return '待审核';
  if (s === 2) return '已通过';
  if (s === 3) return '已驳回';
  return '未知';
}
function statusTagType(s) {
  if (s === 1) return 'warning';
  if (s === 2) return 'success';
  if (s === 3) return 'danger';
  return '';
}
function formatTime(t) {
  if (!t) return '';
  if (typeof t === 'string') return t.replace('T', ' ').slice(0, 16);
  return new Date(t).toLocaleString();
}

onMounted(load);
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
