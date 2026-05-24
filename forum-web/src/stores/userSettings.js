import { defineStore } from 'pinia';

/**
 * 个人中心弹窗状态
 */
export const useUserSettingsStore = defineStore('userSettings', {
  state: () => ({
    visible: false
  }),
  actions: {
    open() {
      this.visible = true;
    },
    close() {
      this.visible = false;
    }
  }
});
