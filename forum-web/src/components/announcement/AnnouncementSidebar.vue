<template>
  <aside class="announcement-sidebar">
    <h3 class="sidebar-title">
      <el-icon><Bell /></el-icon>
      <span>{{ scope === 'site' ? '站点公告' : '板块公告' }}</span>
    </h3>
    <el-skeleton v-if="loading" :rows="3" animated />
    <template v-else-if="announcements.length">
      <div
        v-for="a in announcements"
        :key="a.id"
        class="announcement-item"
        @click="openDetail(a)"
      >
        <el-tag v-if="a.pinned" size="small" type="warning" class="pin-tag">置顶</el-tag>
        <span class="announcement-title">{{ a.title }}</span>
      </div>
    </template>
    <el-empty v-else description="暂无公告" :image-size="40" />

    <!-- 公告详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="currentAnnouncement?.title"
      width="520px"
      destroy-on-close
    >
      <div class="announcement-content" v-html="currentAnnouncement?.content" />
    </el-dialog>
  </aside>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { Bell } from '@element-plus/icons-vue';
import { listSiteAnnouncements, listBoardAnnouncements } from '@/api/announcement';

const props = defineProps({
  scope: { type: String, required: true, validator: v => ['site', 'board'].includes(v) },
  boardId: { type: Number, default: null }
});

const loading = ref(false);
const announcements = ref([]);
const detailVisible = ref(false);
const currentAnnouncement = ref(null);

async function load() {
  loading.value = true;
  try {
    const res = props.scope === 'site'
      ? await listSiteAnnouncements({ page: 1, size: 20 })
      : await listBoardAnnouncements(props.boardId, { page: 1, size: 20 });
    announcements.value = res.data?.records || [];
  } catch {
    announcements.value = [];
  } finally {
    loading.value = false;
  }
}

function openDetail(a) {
  currentAnnouncement.value = a;
  detailVisible.value = true;
}

onMounted(load);
watch(() => props.boardId, load);
</script>

<style scoped>
.announcement-sidebar {
  background: #fff;
  border-radius: 6px;
  padding: 12px;
  position: sticky;
  top: 70px;
}
.sidebar-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #666;
}
.announcement-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}
.announcement-item:hover {
  background: #f5f7fa;
}
.pin-tag {
  flex-shrink: 0;
}
.announcement-title {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.announcement-content {
  font-size: 14px;
  line-height: 1.7;
  color: #333;
}
.announcement-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}
</style>
