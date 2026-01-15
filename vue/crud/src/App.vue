<template>
  <div class="app-container">
    <!-- 1. 显示工作台 -->
    <Transition name="fade" mode="out-in">
      <ModuleDashboard 
        v-if="currentView === 'dashboard'" 
        @open-module="handleOpenModule" 
      />

      <!-- 2. 显示通用 CRUD 编辑器 -->
      <SmartGridView 
        v-else-if="currentView === 'grid'" 
        :active-module="activeModuleData"
        @back="handleBackToDashboard"
      />
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
// 确保路径正确，根据你的实际文件位置调整
import ModuleDashboard from './views/ModuleDashboard.vue'; 
import SmartGridView from './views/SmartGridView.vue';

// --- State ---
const currentView = ref<'dashboard' | 'grid'>('dashboard');
const activeModuleData = ref<any>(null);

// --- Actions ---

// 从工作台接收“打开模块”信号
const handleOpenModule = (moduleInfo: any) => {
  console.log("打开模块:", moduleInfo);
  activeModuleData.value = moduleInfo; // 保存当前选中的模块信息 (包含 id, tableName, moduleName 等)
  currentView.value = 'grid';          // 切换视图
};

// 从编辑器接收“返回”信号
const handleBackToDashboard = () => {
  currentView.value = 'dashboard';
  activeModuleData.value = null; // 清空选中状态
};
</script>

<style>
/* 全局重置 */
html, body, #app {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.app-container {
  width: 100%;
  height: 100%;
}

/* 简单的淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>