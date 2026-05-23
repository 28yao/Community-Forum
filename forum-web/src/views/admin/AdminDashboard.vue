<template>
  <el-card shadow="never">
    <h2 style="margin:0 0 20px 0;">总览</h2>
    <el-row :gutter="16">
      <el-col :sm="12" :md="6" v-for="card in cards" :key="card.title">
        <el-card shadow="hover" class="stat-card" @click="$router.push(card.to)">
          <div class="stat-title">{{ card.title }}</div>
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-hint">{{ card.hint }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-divider />

    <el-alert type="info" :closable="false" show-icon>
      <template #title>
        管理员说明
      </template>
      <div style="margin-top:6px;">
        所有后台写操作（封禁/删帖/重置密码等）都记录到服务器日志（C4 决策）。
        封禁与重置密码会立即清除目标用户的 Token（C5 决策）。
      </div>
    </el-alert>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { adminListUsers, adminListPosts, adminListComments, adminListBoards } from '@/api/admin';

const cards = ref([
  { title: '用户数', value: '-', hint: '注册用户总数', to: '/admin/users' },
  { title: '版块数', value: '-', hint: '含已禁用', to: '/admin/boards' },
  { title: '帖子数', value: '-', hint: '含已删除', to: '/admin/posts' },
  { title: '评论数', value: '-', hint: '含已删除', to: '/admin/comments' }
]);

onMounted(async () => {
  const tasks = [
    adminListUsers({ page: 1, size: 1 }).then(r => cards.value[0].value = r.total || 0),
    adminListBoards().then(list => cards.value[1].value = list.length || 0),
    adminListPosts({ page: 1, size: 1 }).then(r => cards.value[2].value = r.total || 0),
    adminListComments({ page: 1, size: 1 }).then(r => cards.value[3].value = r.total || 0)
  ];
  await Promise.allSettled(tasks);
});
</script>

<style scoped>
.stat-card {
  cursor: pointer;
  text-align: center;
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-title {
  color: #888;
  font-size: 14px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1677ff;
  margin: 8px 0;
}
.stat-hint {
  color: #aaa;
  font-size: 12px;
}
</style>
