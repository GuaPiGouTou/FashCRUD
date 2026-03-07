<template>
  <div class="app-container">
    <!-- 1. 显示登录页面 -->
    <Transition name="fade" mode="out-in">
      <Login 
        v-if="!isLoggedIn" 
        @login-success="handleLoginSuccess" 
      />

      <!-- 2. 显示数据中心工作台 -->
      <ModuleDashboard 
        v-else-if="currentView === 'dashboard' " 
        @open-module="handleOpenModule"
        @open-system-dashboard="handleOpenSystemDashboard"
        @logout="handleLogout"
      />

      <!-- 3. 显示系统统计仪表盘 -->
      <Dashboard 
        v-else-if="currentView === 'system-dashboard' " 
        @back="handleBackToDashboard"
        @logout="handleLogout"
      />

      <!-- 4. 显示通用 CRUD 编辑器 -->
      <SmartGridView 
        v-else-if="currentView === 'grid' " 
        :active-module="activeModuleData"
        @back="handleBackToDashboard"
      />
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import ModuleDashboard from './views/ModuleDashboard.vue'; 
import SmartGridView from './views/SmartGridView.vue';
import Login from './views/Login.vue';
import Dashboard from './views/Dashboard.vue';

// --- State ---
const isLoggedIn = ref(false);
const currentView = ref<'dashboard' | 'grid' | 'system-dashboard'>('dashboard');
const activeModuleData = ref<any>(null);

// --- Lifecycle ---
onMounted(() => {
  const user = localStorage.getItem('user');
  isLoggedIn.value = !!user;
});

// --- Actions ---

const handleLoginSuccess = () => {
  isLoggedIn.value = true;
  currentView.value = 'dashboard';
};

const handleLogout = () => {
  localStorage.removeItem('user');
  isLoggedIn.value = false;
};

const handleOpenModule = (moduleInfo: any) => {
  console.log("打开模块:", moduleInfo);
  activeModuleData.value = moduleInfo;
  currentView.value = 'grid';
};

const handleOpenSystemDashboard = () => {
  currentView.value = 'system-dashboard';
};

const handleBackToDashboard = () => {
  currentView.value = 'dashboard';
  activeModuleData.value = null;
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