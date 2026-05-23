<template>
  <div class="search-page">
    <el-card shadow="never">
      <h2 style="margin:0 0 8px 0;">
        搜索：<span class="kw">{{ keyword }}</span>
      </h2>
      <p style="color:#888; margin:0 0 16px 0;">
        共 {{ total }} 条结果
      </p>

      <div v-if="loading" v-loading="true" style="min-height:200px;"></div>
      <EmptyState v-else-if="!results.length" description="未找到相关帖子，换个关键词试试" />
      <div v-else class="result-list">
        <el-card
          v-for="r in results"
          :key="r.id"
          shadow="hover"
          class="result-card"
          @click="goDetail(r.id)"
        >
          <div class="result-main">
            <h3 class="result-title" v-html="highlightHtml(r.title)"></h3>
            <p class="result-summary" v-html="highlightHtml(r.summary)"></p>
            <div class="result-meta">
              <span>{{ r.author?.nickname || '已注销' }}</span>
              <span class="sep">·</span>
              <router-link :to="`/board/${r.board?.id}`" @click.stop>{{ r.board?.name }}</router-link>
              <span class="sep">·</span>
              <span>{{ formatTime(r.createdAt) }}</span>
              <span class="sep">·</span>
              <span>{{ r.likeCount || 0 }} 赞</span>
              <span class="sep">·</span>
              <span>{{ r.commentCount || 0 }} 评论</span>
            </div>
          </div>
          <el-image
            v-if="r.firstImage"
            :src="r.firstImage"
            fit="cover"
            class="result-thumb"
          />
        </el-card>
      </div>

      <Pagination
        v-if="total > size"
        :page="page"
        :size="size"
        :total="total"
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
import { searchPosts } from '@/api/search';
import { highlight } from '@/utils/highlight';

const route = useRoute();
const router = useRouter();

const keyword = computed(() => route.query.q || '');
const results = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const loading = ref(false);

async function fetchData() {
  if (!keyword.value) {
    results.value = [];
    total.value = 0;
    return;
  }
  loading.value = true;
  try {
    const res = await searchPosts(keyword.value, { page: page.value, size: size.value });
    results.value = res.list || [];
    total.value = res.total || 0;
  } catch (e) {
    results.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function handlePageChange(p) {
  page.value = p;
  fetchData();
}

function goDetail(id) {
  router.push(`/post/${id}`);
}

function highlightHtml(text) {
  return highlight(text, keyword.value);
}

function formatTime(t) {
  if (!t) return '';
  const d = new Date(typeof t === 'string' ? t.replace(' ', 'T') : t);
  return Number.isNaN(d.getTime()) ? t : d.toLocaleDateString('zh-CN');
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
.result-card {
  cursor: pointer;
  margin-bottom: 12px;
}
.result-card :deep(.el-card__body) {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.result-main {
  flex: 1;
  min-width: 0;
}
.result-title {
  margin: 0 0 8px 0;
  font-size: 16px;
}
.result-summary {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.result-meta {
  color: #999;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.sep {
  color: #ddd;
}
.result-thumb {
  width: 100px;
  height: 80px;
  border-radius: 4px;
  flex-shrink: 0;
}
</style>

<style>
/* 关键词高亮全局样式（非 scoped，否则 v-html 内部 <mark> 无法命中） */
mark.hl {
  background: #fff3a8;
  color: inherit;
  padding: 0 2px;
  border-radius: 2px;
}
</style>
