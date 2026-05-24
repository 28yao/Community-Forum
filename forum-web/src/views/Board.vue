<template>
  <div class="board-page">
    <div class="three-col-layout">
      <aside class="left-col">
        <UnifiedSidebar />
      </aside>
      <main class="main-content">
        <div class="board-header">
          <div>
            <h2 style="margin:0;">{{ board?.name || '加载中' }}</h2>
            <p style="margin:4px 0 0 0; color:#666;">{{ board?.description }}</p>
            <p v-if="board?.followerCount != null" style="margin:4px 0 0 0; color:#999; font-size: 12px;">
              关注 {{ board.followerCount }} · 帖子 {{ board.postCount }}
            </p>
          </div>
          <div class="board-actions">
            <el-button v-if="isOwnerOrAdmin" @click="showEdit = true">编辑板块</el-button>
            <BoardFollowButton v-if="board" :board="board" />
            <el-button v-if="userStore.isLoggedIn" type="primary" @click="goCreate">发帖</el-button>
          </div>
        </div>
        <el-divider />
        <PostList ref="listRef" :board-id="boardId" />

        <BoardEditForm
          v-if="showEdit && board"
          :board="board"
          @close="showEdit = false"
          @saved="onEditSaved"
        />
      </main>
      <aside class="right-col">
        <div class="announcement-widget" v-if="board">
          <div class="widget-header">
            <span class="widget-title">
              <el-icon><Bell /></el-icon> 板块公告
            </span>
            <el-button v-if="isOwnerOrAdmin" type="primary" text size="small" @click="openAnnouncementDialog(null)">
              发布公告
            </el-button>
          </div>
          <el-skeleton v-if="announcementsLoading" :rows="3" animated />
          <template v-else-if="announcements.length">
            <div
              v-for="a in announcements"
              :key="a.id"
              class="announcement-item"
              @click="handleAnnouncementClick(a)"
            >
              <div class="announcement-head">
                <el-tag v-if="a.pinned" size="small" type="warning" class="pin-tag">置顶</el-tag>
                <span class="announcement-title">{{ a.title }}</span>
              </div>
              <p class="announcement-content">{{ a.content }}</p>
              <span class="announcement-date">{{ formatDate(a.updatedAt) }}</span>
            </div>
          </template>
          <el-empty v-else description="暂无公告" :image-size="40" />
        </div>
      </aside>
    </div>

    <!-- 公告创建/编辑弹窗 -->
    <el-dialog
      v-model="announcementDialogVisible"
      :title="editingAnnouncement ? '编辑公告' : '发布公告'"
      width="520px"
      destroy-on-close
      @close="resetAnnouncementForm"
    >
      <el-form :model="announcementForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="announcementForm.title" maxlength="50" show-word-limit placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="announcementForm.content" type="textarea" :rows="6" maxlength="500" show-word-limit placeholder="公告内容" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="announcementForm.pinned" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <div>
            <el-button v-if="editingAnnouncement" type="danger" text @click="handleDeleteAnnouncement(editingAnnouncement)">
              删除
            </el-button>
          </div>
          <div>
            <el-button @click="announcementDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="announcementSubmitting" @click="handleAnnouncementSubmit">
              {{ editingAnnouncement ? '保存' : '发布' }}
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Bell } from '@element-plus/icons-vue';
import UnifiedSidebar from '@/components/layout/UnifiedSidebar.vue';
import PostList from '@/components/post/PostList.vue';
import BoardFollowButton from '@/components/board/BoardFollowButton.vue';
import BoardEditForm from '@/components/board/BoardEditForm.vue';
import { useUserStore } from '@/stores/user';
import { useBoardFollowStore } from '@/stores/boardFollow';
import { usePostEditorStore } from '@/stores/postEditor';
import { getBoardById } from '@/api/board';
import { listBoardAnnouncements, createBoardAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/announcement';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const followStore = useBoardFollowStore();
const postEditorStore = usePostEditorStore();

const boardId = computed(() => Number(route.params.id));
const board = ref(null);
const listRef = ref(null);
const showEdit = ref(false);

const isOwnerOrAdmin = computed(() => {
  if (!userStore.isLoggedIn || !board.value) return false;
  return userStore.isAdmin || board.value.ownerUserId === userStore.info?.id;
});

// 公告数据
const announcements = ref([]);
const announcementsLoading = ref(false);

async function loadAnnouncements() {
  announcementsLoading.value = true;
  try {
    const res = await listBoardAnnouncements(boardId.value, { page: 1, size: 20 });
    announcements.value = res?.records || [];
  } catch {
    announcements.value = [];
  } finally {
    announcementsLoading.value = false;
  }
}

function handleAnnouncementClick(a) {
  if (isOwnerOrAdmin.value) {
    openAnnouncementDialog(a);
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  const now = new Date();
  const diffMs = now - d;
  const diffMin = Math.floor(diffMs / 60000);
  const diffHour = Math.floor(diffMs / 3600000);
  const diffDay = Math.floor(diffMs / 86400000);
  if (diffMin < 1) return '刚刚';
  if (diffMin < 60) return diffMin + ' 分钟前';
  if (diffHour < 24) return diffHour + ' 小时前';
  if (diffDay < 30) return diffDay + ' 天前';
  return d.toLocaleDateString('zh-CN');
}

async function handleDeleteAnnouncement(a) {
  try {
    await ElMessageBox.confirm('确定删除这条公告？', '删除公告', { type: 'warning' });
    await deleteAnnouncement(a.id);
    ElMessage.success('已删除');
    announcementDialogVisible.value = false;
    await loadAnnouncements();
  } catch {
    // cancelled
  }
}

// 公告弹窗状态
const announcementDialogVisible = ref(false);
const editingAnnouncement = ref(null);
const announcementSubmitting = ref(false);
const announcementForm = ref({ title: '', content: '', pinned: 0 });

function openAnnouncementDialog(announcement) {
  editingAnnouncement.value = announcement;
  if (announcement) {
    announcementForm.value = { title: announcement.title, content: announcement.content, pinned: announcement.pinned };
  } else {
    announcementForm.value = { title: '', content: '', pinned: 0 };
  }
  announcementDialogVisible.value = true;
}

function resetAnnouncementForm() {
  editingAnnouncement.value = null;
  announcementForm.value = { title: '', content: '', pinned: 0 };
}

async function handleAnnouncementSubmit() {
  if (!announcementForm.value.title.trim() || !announcementForm.value.content.trim()) {
    ElMessage.warning('标题和内容不能为空');
    return;
  }
  announcementSubmitting.value = true;
  try {
    if (editingAnnouncement.value) {
      await updateAnnouncement(editingAnnouncement.value.id, announcementForm.value);
      ElMessage.success('已更新');
    } else {
      await createBoardAnnouncement(boardId.value, announcementForm.value);
      ElMessage.success('已发布');
    }
    announcementDialogVisible.value = false;
    await loadAnnouncements();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  } finally {
    announcementSubmitting.value = false;
  }
}

async function load() {
  try {
    board.value = await getBoardById(boardId.value);
    if (userStore.isLoggedIn && !followStore.followedLoaded) {
      await followStore.loadFollowed();
    }
    await loadAnnouncements();
  } catch (e) {
    ElMessage.error(e.message || '版块不存在');
    router.replace('/');
  }
}

watch(boardId, load);
onMounted(load);

function goCreate() {
  postEditorStore.open({ boardId: boardId.value });
}

async function onEditSaved() {
  showEdit.value = false;
  await load();
}
</script>

<style scoped>
.board-page {
  padding: 0;
}
.main-content {
  background: #fff;
  border-radius: 6px;
  padding: 24px;
}
.board-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.board-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.announcement-widget {
  background: #fff;
  border-radius: 6px;
  padding: 12px;
}
.widget-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.widget-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #666;
}
.announcement-item {
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}
.announcement-item:hover {
  background: #f5f7fa;
}
.announcement-head {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}
.pin-tag {
  flex-shrink: 0;
}
.announcement-title {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.announcement-content {
  font-size: 12px;
  color: #888;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.announcement-date {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  display: block;
}
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
