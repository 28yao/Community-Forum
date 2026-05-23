<template>
  <div class="search-page">
    <el-card shadow="never">
      <h2 style="margin:0 0 8px 0;">
        搜索：<span class="kw">{{ keyword }}</span>
      </h2>

      <el-tabs v-model="activeType" @tab-change="handleTypeChange">
        <el-tab-pane label="帖子" name="post">
          <template #label>
            <span>帖子 <el-tag v-if="activeType === 'post'" size="small" type="info">{{ postTotal }}</el-tag></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="板块" name="board">
          <template #label>
            <span>板块 <el-tag v-if="activeType === 'board'" size="small" type="info">{{ boardTotal }}</el-tag></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="用户" name="user">
          <template #label>
            <span>用户 <el-tag v-if="activeType === 'user'" size="small" type="info">{{ userTotal }}</el-tag></span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- 帖子 Tab 的 scope 切换 -->
      <div v-if="activeType === 'post'" class="scope-toggle">
        <el-radio-group v-model="scope" size="small" @change="handleScopeChange">
          <el-radio-button label="both">标题 + 正文</el-radio-button>
          <el-radio-button label="title">仅标题</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="loading" v-loading="true" style="min-height:200px;"></div>

      <template v-else>
        <!-- 帖子结果 -->
        <template v-if="activeType === 'post'">
          <EmptyState v-if="!postResults.length" description="未找到相关帖子，换个关键词试试" />
          <SearchResultPost v-else :posts="postResults" :keyword="keyword" />
        </template>

        <!-- 板块结果 -->
        <template v-if="activeType === 'board'">
          <EmptyState v-if="!boardResults.length" description="未找到相关板块" />
          <SearchResultBoard v-else :boards="boardResults" :keyword="keyword" />
        </template>

        <!-- 用户结果 -->
        <template v-if="activeType === 'user'">
          <EmptyState v-if="!userResults.length" description="未找到相关用户" />
          <SearchResultUser v-else :users="userResults" :keyword="keyword" />
        </template>
      </template>

      <Pagination
        v-if="currentTotal > size"
        :page="page"
        :size="size"
        :total="currentTotal"
        @change="handlePageChange"
        style="margin-top: 16px;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import EmptyState from '@/components/common/EmptyState.vue';
import Pagination from '@/components/common/Pagination.vue';
import SearchResultPost from '@/components/search/SearchResultPost.vue';
import SearchResultBoard from '@/components/search/SearchResultBoard.vue';
import SearchResultUser from '@/components/search/SearchResultUser.vue';
import { search } from '@/api/search';

const route = useRoute();
const router = useRouter();

const keyword = computed(() => route.query.q || '');
const activeType = ref(route.query.type || 'post');
const scope = ref(route.query.scope || 'both');
const page = ref(Number(route.query.page) || 1);
const size = ref(20);
const loading = ref(false);

const postResults = ref([]);
const boardResults = ref([]);
const userResults = ref([]);
const postTotal = ref(0);
const boardTotal = ref(0);
const userTotal = ref(0);

const currentTotal = computed(() => {
  if (activeType.value === 'post') return postTotal.value;
  if (activeType.value === 'board') return boardTotal.value;
  return userTotal.value;
});

async function fetchData() {
  if (!keyword.value) return;
  loading.value = true;
  try {
    const params = {
      type: activeType.value,
      page: page.value,
      size: size.value
    };
    if (activeType.value === 'post') {
      params.scope = scope.value;
    }
    const res = await search(keyword.value, params);
    const list = res.list || [];
    const total = res.total || 0;

    if (activeType.value === 'post') {
      postResults.value = list;
      postTotal.value = total;
    } else if (activeType.value === 'board') {
      boardResults.value = list;
      boardTotal.value = total;
    } else {
      userResults.value = list;
      userTotal.value = total;
    }
  } catch (e) {
    if (activeType.value === 'post') { postResults.value = []; postTotal.value = 0; }
    else if (activeType.value === 'board') { boardResults.value = []; boardTotal.value = 0; }
    else { userResults.value = []; userTotal.value = 0; }
  } finally {
    loading.value = false;
  }
}

function syncUrl() {
  const query = { q: keyword.value, type: activeType.value };
  if (activeType.value === 'post' && scope.value !== 'both') {
    query.scope = scope.value;
  }
  if (page.value > 1) query.page = page.value;
  router.replace({ query });
}

function handleTypeChange() {
  page.value = 1;
  syncUrl();
  fetchData();
}

function handleScopeChange() {
  page.value = 1;
  syncUrl();
  fetchData();
}

function handlePageChange(p) {
  page.value = p;
  syncUrl();
  fetchData();
}

watch(keyword, () => {
  page.value = 1;
  fetchData();
});

onMounted(fetchData);
</script>

<style scoped>
.search-page {
  max-width: 900px;
  margin: 0 auto;
}
.kw {
  color: #1677ff;
}
.scope-toggle {
  margin-bottom: 16px;
}
</style>

<style>
mark.hl {
  background: #fff3a8;
  color: inherit;
  padding: 0 2px;
  border-radius: 2px;
}
</style>
