<template>
  <div />
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useUserSettingsStore } from '@/stores/userSettings';

const router = useRouter();
const userStore = useUserStore();
const settingsStore = useUserSettingsStore();

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.replace({ path: '/login', query: { redirect: '/settings' } });
    return;
  }
  settingsStore.open();
  router.replace('/');
});
</script>
