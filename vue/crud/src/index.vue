<template>
  <div style="height: 100vh; padding: 20px; background: #333;">
    <!-- 模拟一个嵌入环境，比如白色卡片容器 -->
    <div style="height: 100%; background: #fff; border-radius: 8px; overflow: hidden;">
      
      <!-- 只有当 mode 为 dashboard 时显示工作台 -->
      <ModuleDashboard 
        v-if="currentMode === 'dashboard'"
        @open-module="handleOpenModule" 
      />

      <!-- 当 mode 为 editor 时显示之前的 CRUD 表格 -->
      <SmartGridView 
        v-else 
        :table-name="activeModule.name" 
        @back="currentMode = 'dashboard'"
      />
      
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import ModuleDashboard from '@/views/ModuleDashboard.vue';
import SmartGridView from '@/views/SmartGridView.vue';

const currentMode = ref('dashboard');
const activeModule = ref({});

const handleOpenModule = (moduleData) => {
  console.log("父组件收到了打开请求:", moduleData);
  activeModule.value = moduleData;
  currentMode.value = 'editor';
};
</script>