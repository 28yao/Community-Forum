<template>
  <div class="my-applications-page">
    <el-card shadow="hover" style="max-width: 900px; margin: 20px auto;">
      <template #header>
        <div class="page-header">
          <h2 style="margin:0;">我的板块申请</h2>
          <el-button type="primary" @click="openApply">+ 申请新板块</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <ApplicationList :list="list" />
        <el-pagination
          v-if="total > size"
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          @current-change="load"
          style="justify-content: center; margin-top: 16px;"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="申请创建板块" width="600px" :close-on-click-modal="false">
      <div v-if="checkingEligibility" v-loading="true" style="height: 100px;"></div>
      <template v-else>
        <EligibilityHint :eligibility="eligibility" />
        <div v-if="eligibility?.eligible" style="margin-top: 16px;">
          <ApplicationForm @submitted="onSubmitted" @cancel="dialogVisible = false" />
        </div>
        <div v-else style="margin-top: 16px; text-align: center;">
          <el-button @click="dialogVisible = false">我知道了</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 我的板块申请页（P2-M2）
 *
 * 列表 + "申请新板块"按钮（点击先 check 资格 → 不达标显示提示 / 达标显示表单）
 */
import { ref, onMounted } from 'vue';
import ApplicationList from '@/components/board-application/ApplicationList.vue';
import ApplicationForm from '@/components/board-application/ApplicationForm.vue';
import EligibilityHint from '@/components/board-application/EligibilityHint.vue';
import { listMyApplications, getEligibility } from '@/api/boardApplication';

const list = ref([]);
const page = ref(1);
const size = ref(20);
const total = ref(0);
const loading = ref(false);

const dialogVisible = ref(false);
const checkingEligibility = ref(false);
const eligibility = ref(null);

async function load() {
  loading.value = true;
  try {
    const data = await listMyApplications(page.value, size.value);
    list.value = data.records || [];
    total.value = data.total || 0;
  } finally {
    loading.value = false;
  }
}

async function openApply() {
  dialogVisible.value = true;
  checkingEligibility.value = true;
  try {
    eligibility.value = await getEligibility();
  } finally {
    checkingEligibility.value = false;
  }
}

function onSubmitted() {
  dialogVisible.value = false;
  page.value = 1;
  load();
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
