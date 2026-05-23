<template>
  <el-card shadow="never">
    <div class="toolbar">
      <h2 style="margin:0;">用户管理</h2>
      <el-input
        v-model="keyword"
        placeholder="按邮箱或昵称搜索"
        clearable
        style="width:280px;"
        @keyup.enter="fetchData"
        @clear="fetchData"
      />
    </div>

    <el-table v-loading="loading" :data="list" stripe style="margin-top:16px;">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="头像" width="60">
        <template #default="{ row }">
          <el-avatar :size="32" :src="row.avatar || ''">{{ row.nickname?.charAt(0) }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.role === 'admin'" type="danger">管理员</el-tag>
          <el-tag v-else type="info">用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">正常</el-tag>
          <el-tooltip v-else :content="row.banReason || ''" placement="top">
            <el-tag type="danger">已封禁</el-tag>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="邮箱验证" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.emailVerified === 1" type="success" size="small">已验证</el-tag>
          <el-tag v-else type="warning" size="small">未验证</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1 && row.role !== 'admin'" size="small" type="danger" @click="handleBan(row)">封禁</el-button>
          <el-button v-if="row.status === 0" size="small" type="success" @click="handleUnban(row)">解封</el-button>
          <el-button size="small" @click="handleReset(row)">重置密码</el-button>
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
  adminListUsers, adminBanUser, adminUnbanUser, adminResetPassword
} from '@/api/admin';

const keyword = ref('');
const list = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const loading = ref(false);

async function fetchData() {
  loading.value = true;
  try {
    const res = await adminListUsers({
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

async function handleBan(row) {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `确认封禁 ${row.nickname}？封禁原因将记录但不展示给用户。`,
      '封禁用户',
      { confirmButtonText: '封禁', cancelButtonText: '取消', inputPlaceholder: '封禁原因（可选）' }
    );
    await adminBanUser(row.id, reason || '');
    ElMessage.success('已封禁');
    fetchData();
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message);
  }
}

async function handleUnban(row) {
  try {
    await ElMessageBox.confirm(`确认解封 ${row.nickname}？`, '解封用户', { type: 'warning' });
    await adminUnbanUser(row.id);
    ElMessage.success('已解封');
    fetchData();
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message);
  }
}

async function handleReset(row) {
  try {
    const { value: newPwd } = await ElMessageBox.prompt(
      `为 ${row.nickname} 重置密码（至少 6 位）。重置后该用户当前会话立即失效。`,
      '重置密码',
      { confirmButtonText: '重置', cancelButtonText: '取消', inputPattern: /^.{6,}$/, inputErrorMessage: '至少 6 位' }
    );
    await adminResetPassword(row.id, newPwd);
    ElMessage.success('密码已重置');
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
  gap: 16px;
}
</style>
