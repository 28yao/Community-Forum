<template>
  <div class="eligibility-hint">
    <el-result
      :icon="eligibility.eligible ? 'success' : 'warning'"
      :title="eligibility.eligible ? '你已具备申请资格' : '尚未满足申请资格'"
      :sub-title="eligibility.reason || '满足条件后可创建自己的板块'"
    />
    <el-descriptions :column="1" border style="margin-top: 12px;">
      <el-descriptions-item label="注册天数">
        {{ eligibility.registeredDays }} 天 / 需 ≥ {{ eligibility.requiredDays }} 天
      </el-descriptions-item>
      <el-descriptions-item label="已发帖数">
        {{ eligibility.postCount }} / 需 ≥ {{ eligibility.requiredPosts }}
      </el-descriptions-item>
      <el-descriptions-item label="待审核申请">
        {{ eligibility.hasPending ? '已有 1 个待审核' : '无' }}
      </el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
/** 资格状态展示组件（P2-M2） */
defineProps({
  eligibility: {
    type: Object,
    required: true
  }
});
</script>

<style scoped>
.eligibility-hint {
  padding: 8px 0;
}
</style>
