<template>
  <div class="post-list">
    <div v-if="loading" v-loading="true" style="min-height:300px;"></div>
    <template v-else>
      <EmptyState v-if="!posts.length" description="暂无帖子" />
      <PostCard v-for="p in posts" :key="p.id" :post="p" />
      <Pagination
        v-if="total > size"
        :page="page"
        :size="size"
        :total="total"
        @change="handlePageChange"
      />
    </template>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import PostCard from './PostCard.vue';
import Pagination from '@/components/common/Pagination.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import { listPosts, getFeed } from '@/api/post';

const props = defineProps({
  boardId: { type: [Number, String], default: null },
  /** list=按板块列表（默认），feed=混合信息流 */
  mode: { type: String, default: 'list' }
});

const posts = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const loading = ref(false);

async function fetchData() {
  loading.value = true;
  try {
    const params = { page: page.value, size: size.value };
    let res;
    if (props.mode === 'feed' && !props.boardId) {
      res = await getFeed(params);
    } else {
      if (props.boardId) params.boardId = props.boardId;
      res = await listPosts(params);
    }
    posts.value = res.list || [];
    total.value = res.total || 0;
  } catch (e) {
    posts.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function handlePageChange(p) {
  page.value = p;
  fetchData();
}

watch(() => props.boardId, () => {
  page.value = 1;
  fetchData();
});

onMounted(fetchData);

defineExpose({ refresh: fetchData });
</script>
