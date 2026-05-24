<template>
  <el-dialog
    v-model="visible"
    title="板块申请"
    width="680px"
    destroy-on-close
    @close="$emit('close')"
  >
    <div class="application-dialog">
      <!-- 申请表单区 -->
      <div class="apply-section">
        <h4 class="section-title">申请新板块</h4>
        <div v-if="checkingEligibility" v-loading="true" style="height: 80px;"></div>
        <template v-else>
          <EligibilityHint :eligibility="eligibility" />
          <div v-if="eligibility?.eligible" style="margin-top: 12px;">
            <ApplicationForm @submitted="onSubmitted" @cancel="visible = false" />
          </div>
        </template>
      </div>

      <el-divider />

      <!-- 历史申请列表 -->
      <div class="history-section">
        <h4 class="section-title">我的申请记录</h4>
        <div v-loading="loadingList">
          <ApplicationList :list="applications" />
          <el-empty v-if="!loadingList && !applications.length" description="暂无申请记录" :image-size="40" />
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue';
import ApplicationForm from './ApplicationForm.vue';
import ApplicationList from './ApplicationList.vue';
import EligibilityHint from './EligibilityHint.vue';
import { listMyApplications, getEligibility } from '@/api/boardApplication';

const props = defineProps({
  modelValue: { type: Boolean, default: false }
});
const emit = defineEmits(['update:modelValue', 'close']);

const visible = ref(props.modelValue);

watch(() => props.modelValue, (v) => {
  visible.value = v;
  if (v) init();
});
watch(visible, (v) => emit('update:modelValue', v));

const checkingEligibility = ref(false);
const eligibility = ref(null);
const loadingList = ref(false);
const applications = ref([]);

async function init() {
  checkingEligibility.value = true;
  loadingList.value = true;
  try {
    const [elig, listData] = await Promise.all([
      getEligibility(),
      listMyApplications(1, 50)
    ]);
    eligibility.value = elig;
    applications.value = listData.records || [];
  } catch {
    // handled by interceptor
  } finally {
    checkingEligibility.value = false;
    loadingList.value = false;
  }
}

function onSubmitted() {
  visible.value = false;
}
</script>

<style scoped>
.application-dialog {
  max-height: 70vh;
  overflow-y: auto;
}
.section-title {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
</style>
