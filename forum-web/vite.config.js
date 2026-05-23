import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath, URL } from 'node:url';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    host: '127.0.0.1',
    proxy: {
      // 后端接口走 /api 前缀，dev 时由 Vite 反代到后端 8080
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 上传图片访问基址也反代过去，开发体验更顺
      '/static': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
