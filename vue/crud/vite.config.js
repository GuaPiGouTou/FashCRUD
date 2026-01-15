import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },

  // 👇 核心配置在这里
  server: {
    port: 5173, // 前端端口
    open: true, // 启动时自动打开浏览器
    proxy: {
      // 拦截所有以 '/api' 开头的请求
      '/api': {
        target: 'http://localhost:8080', // 后端接口地址
        changeOrigin: true, // 允许跨域，修改 Host 头
        
        // ⚠️ 注意：
        // 如果你的后端 Controller 是 @RequestMapping("/api/module")，则不需要 rewrite。
        // 因为请求 /api/module -> 转发到 http://localhost:8080/api/module，完全匹配。
        
        // 如果你的后端 Controller 是 @RequestMapping("/module") (没有 /api 前缀)，
        // 则需要打开下面的注释，把 /api 去掉：
        // rewrite: (path) => path.replace(/^\/api/, '') 
      }
    }
  }
})