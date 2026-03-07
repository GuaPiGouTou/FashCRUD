<template>
  <div class="workspace-container">
    <!-- 1. 顶部工具栏 -->
    <div class="workspace-toolbar">
      <div class="toolbar-left">
        <h2 class="workspace-title">数据中心 / Data Center</h2>
        <div 
          class="analytics-toggle" 
          :class="{ active: showAnalytics }"
          @click="showAnalytics = !showAnalytics"
        >
          <el-icon><DataLine /></el-icon>
          <span>{{ showAnalytics ? '隐藏仪表盘' : '显示仪表盘' }}</span>
        </div>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" plain @click="emit('open-system-dashboard')">
          <el-icon><DataLine /></el-icon>
          系统统计
        </el-button>
        
        <el-input
          v-model="searchQuery"
          placeholder="搜索表名或描述..."
          prefix-icon="Search"
          clearable
          class="search-input"
          @input="handleSearchInput" 
          @clear="fetchModules"
        >
          <template #append>
            <el-button :icon="Search" @click="fetchModules" />
          </template>
        </el-input>

        <!-- 视图切换 -->
        <div class="view-toggle">
          <el-tooltip content="网格视图" placement="top">
            <div class="toggle-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
              <el-icon><Menu /></el-icon>
            </div>
          </el-tooltip>
          <el-tooltip content="列表视图" placement="top">
            <div class="toggle-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
              <el-icon><Tickets /></el-icon>
            </div>
          </el-tooltip>
        </div>

        <!-- 用户信息和登出 -->
        <div class="user-info">
          <el-dropdown trigger="click">
            <div class="user-avatar">
              <el-icon><User /></el-icon>
              <span>{{ userName }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <el-scrollbar>
      <div class="workspace-content">
        
        <!-- 2. 数据可视化 (保持不变) -->
        <el-collapse-transition>
          <div v-if="showAnalytics" class="analytics-dashboard">
            <div class="kpi-row">
              <div class="kpi-card"><div class="kpi-icon blue"><el-icon><Files /></el-icon></div><div class="kpi-data"><div class="kpi-value">{{ dashboardStats.totalTables || 0 }}</div><div class="kpi-label">总数据表 (Tables)</div></div></div>
              <div class="kpi-card"><div class="kpi-icon green"><el-icon><Coin /></el-icon></div><div class="kpi-data"><div class="kpi-value">{{ (dashboardStats.totalRecords || 0).toLocaleString() }}</div><div class="kpi-label">总记录数 (Rows)</div></div></div>
              <div class="kpi-card"><div class="kpi-icon orange"><el-icon><Odometer /></el-icon></div><div class="kpi-data"><div class="kpi-value">{{ dashboardStats.storageUsed || '0 KB' }}</div><div class="kpi-label">存储占用 (Storage)</div></div></div>
              <div class="kpi-card"><div class="kpi-icon purple"><el-icon><TrendCharts /></el-icon></div><div class="kpi-data"><div class="kpi-value">+{{ dashboardStats.newThisWeek || 0 }}</div><div class="kpi-label">本周新增 (New)</div></div></div>
            </div>
            <div class="charts-row">
              <div class="chart-panel flex-2">
                <div class="panel-header"><span class="p-title">数据量排行 Top 5</span><el-tag size="small" effect="plain">Top 5</el-tag></div>
                <div class="bar-chart-container">
                  <div v-for="item in dashboardStats.topModules" :key="item.id" class="bar-row">
                    <div class="bar-label"><el-icon :style="{color: item.themeColor}"><component :is="item.icon || 'List'"/></el-icon><span class="text-ellipsis" :title="item.moduleName">{{ item.moduleName }}</span></div>
                    <div class="bar-track"><div class="bar-fill" :style="{ width: getBarWidth(item.recordCount) + '%', backgroundColor: item.themeColor || '#409EFF' }"></div></div>
                    <div class="bar-value">{{ item.recordCount }}</div>
                  </div>
                </div>
              </div>
              <div class="chart-panel flex-1">
                 <div class="panel-header"><span class="p-title">系统配额 / Quota</span></div>
                 <div class="quota-box">
                   <div class="quota-item"><div class="q-info"><span>Database Limit</span><span>{{ dashboardStats.totalTables }} / 50</span></div><el-progress :percentage="Math.min((dashboardStats.totalTables/50)*100, 100)" :show-text="false" status="success" /></div>
                   <div class="api-tip"><el-icon><InfoFilled /></el-icon> 实时数据已连接</div>
                 </div>
              </div>
            </div>
          </div>
        </el-collapse-transition>

        <!-- 3. 快速构建 -->
        <div class="section-label" :class="{ 'mt-20': showAnalytics }">
          <span>快速构建 / BUILD</span>
          <div class="divider"></div>
        </div>
        <div class="creation-grid">
          <div class="create-card excel" @click="openCreateModal('excel')">
            <div class="c-icon-bg green"><el-icon><DocumentAdd /></el-icon></div>
            <div class="c-info"><span class="c-title">Import Excel</span><span class="c-desc">解析 .xlsx 自动建表</span></div>
          </div>
          <div class="create-card csv" @click="openCreateModal('csv')">
            <div class="c-icon-bg orange"><el-icon><Document /></el-icon></div>
            <div class="c-info"><span class="c-title">Import CSV</span><span class="c-desc">解析 .csv 自动建表</span></div>
          </div>
          <div class="create-card json" @click="openCreateModal('json')">
            <div class="c-icon-bg purple"><el-icon><Tickets /></el-icon></div>
            <div class="c-info"><span class="c-title">Import JSON</span><span class="c-desc">解析 .json 自动建表</span></div>
          </div>
          <div class="create-card database" @click="openCreateModal('database')">
            <div class="c-icon-bg cyan"><el-icon><Coin /></el-icon></div>
            <div class="c-info"><span class="c-title">Import Database</span><span class="c-desc">从现有表导入</span></div>
          </div>
          <div class="create-card blank" @click="openCreateModal('blank')">
            <div class="c-icon-bg blue"><el-icon><Plus /></el-icon></div>
            <div class="c-info"><span class="c-title">Create Blank</span><span class="c-desc">手动设计字段结构</span></div>
          </div>
        </div>

        <!-- 4. 正常数据表列表 -->
        <div class="section-label mt-30">
          <span>数据表 / TABLES ({{ modules.length }})</span>
          <div class="divider"></div>
        </div>

        <!-- 网格视图 -->
        <div v-if="viewMode === 'grid' && modules.length > 0" class="modules-grid">
          <div v-for="mod in modules" :key="mod.id" class="module-card" @click="emitNavigate(mod)">
            <div class="m-card-header">
              <div class="m-icon" :style="{ backgroundColor: mod.themeColor }">
                <el-icon><component :is="mod.icon" /></el-icon>
              </div>
              <div @click.stop class="m-action-btn">
                <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, mod)">
                  <el-icon class="m-more-icon"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit"><el-icon><EditPen /></el-icon> 编辑配置</el-dropdown-item>
                      <el-dropdown-item command="softDelete" style="color:#F56C6C" divided><el-icon><Delete /></el-icon> 移入回收站</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <div class="m-card-body">
              <div class="m-name">{{ mod.moduleName }}</div>
              <div class="m-meta">{{ mod.tableName }}</div>
            </div>
            <div class="m-card-footer">
              <span><el-icon style="vertical-align: -1px"><DataLine/></el-icon> {{ mod.recordCount }}</span>
              <span>{{ mod.timeAgoStr }}</span>
            </div>
          </div>
        </div>

        <!-- 列表视图 -->
        <div v-else-if="viewMode === 'list' && modules.length > 0" class="modules-list">
          <div class="list-header">
            <div class="col-name">Name</div><div class="col-db">DB Table</div><div class="col-recs">Records</div><div class="col-date">Updated</div><div class="col-act"></div>
          </div>
          <div v-for="mod in modules" :key="mod.id" class="list-item" @click="emitNavigate(mod)">
             <div class="col-name">
               <div class="list-icon" :style="{ backgroundColor: mod.themeColor }"><el-icon><component :is="mod.icon" /></el-icon></div>
               <span>{{ mod.moduleName }}</span>
             </div>
             <div class="col-db">{{ mod.tableName }}</div>
             <div class="col-recs">{{ mod.recordCount }}</div>
             <div class="col-date">{{ mod.timeAgoStr }}</div>
             <div class="col-act" @click.stop>
                <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, mod)">
                   <el-button link size="small"><el-icon><MoreFilled /></el-icon></el-button>
                   <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="softDelete" style="color:#F56C6C">删除</el-dropdown-item>
                      </el-dropdown-menu>
                   </template>
                </el-dropdown>
             </div>
          </div>
        </div>
        
        <!-- 修改点 2：数据表区域的空状态提示 -->
        <el-empty 
          v-if="modules.length === 0" 
          description="未找到相关数据表" 
          :image-size="100" 
          style="padding: 40px 0;"
        />

        <!-- 5. 回收站分区 -->
        <div v-if="deletedModules.length > 0" class="section-label mt-30">
          <span style="color: #909399;">
            <el-icon style="vertical-align: -2px; margin-right: 4px;"><Delete /></el-icon>
            回收站 / RECYCLE BIN ({{ filteredDeletedModules.length }})
          </span>
          <div class="divider"></div>
        </div>

        <!-- 回收站：网格视图 -->
        <div class="modules-grid" v-if="deletedModules.length > 0 && viewMode === 'grid' && filteredDeletedModules.length > 0">
          <div v-for="mod in filteredDeletedModules" :key="mod.id" class="module-card deleted-card">
            <div class="m-card-header">
              <div class="m-icon grayscale"><el-icon><component :is="mod.icon" /></el-icon></div>
              <div class="deleted-actions">
                <el-tooltip content="恢复"><el-button circle size="small" type="success" plain @click.stop="handleRecover(mod)"><el-icon><RefreshLeft /></el-icon></el-button></el-tooltip>
                <el-tooltip content="彻底删除"><el-button circle size="small" type="danger" plain @click.stop="handleHardDelete(mod)"><el-icon><Delete /></el-icon></el-button></el-tooltip>
              </div>
            </div>
            <div class="m-card-body grayscale">
              <div class="m-name">{{ mod.moduleName }}</div>
              <div class="m-meta">{{ mod.tableName }}</div>
            </div>
            <div class="m-card-footer grayscale"><span>已删除</span><span>{{ mod.timeAgoStr }}</span></div>
          </div>
        </div>

        <!-- 回收站：列表视图 -->
        <div class="modules-list" v-else-if="deletedModules.length > 0 && viewMode === 'list' && filteredDeletedModules.length > 0">
          <div class="list-header">
            <div class="col-name">Name</div><div class="col-db">DB Table</div><div class="col-recs">Status</div><div class="col-date">Deleted At</div><div class="col-act">Actions</div>
          </div>
          <div v-for="mod in filteredDeletedModules" :key="mod.id" class="list-item deleted-list-item">
             <div class="col-name grayscale">
               <div class="list-icon" style="background: #e0e0e0; color: #999;"><el-icon><component :is="mod.icon" /></el-icon></div>
               <span style="color: #999; text-decoration: line-through;">{{ mod.moduleName }}</span>
             </div>
             <div class="col-db grayscale">{{ mod.tableName }}</div>
             <div class="col-recs"><el-tag size="small" type="info">已删除</el-tag></div>
             <div class="col-date grayscale">{{ mod.timeAgoStr }}</div>
             <div class="col-act">
                <div class="deleted-actions">
                  <el-tooltip content="恢复"><el-button circle size="small" type="success" plain @click.stop="handleRecover(mod)"><el-icon><RefreshLeft /></el-icon></el-button></el-tooltip>
                  <el-tooltip content="彻底删除"><el-button circle size="small" type="danger" plain @click.stop="handleHardDelete(mod)"><el-icon><Delete /></el-icon></el-button></el-tooltip>
                </div>
             </div>
          </div>
        </div>

        <!-- 回收站搜索为空提示 -->
        <div v-if="deletedModules.length > 0 && filteredDeletedModules.length === 0" style="text-align:center; color:#999; padding:20px; font-size: 13px;">
           <el-icon style="vertical-align: middle; margin-right: 5px;"><Search /></el-icon> 回收站中未找到匹配项
        </div>

      </div>
    </el-scrollbar>

    <!-- 弹窗：新建/编辑 -->
    <el-dialog v-model="createDialogVisible" width="600px" destroy-on-close :title="getDialogTitle()">
       <el-form label-position="top" :model="createForm">
          <!-- Excel导入 -->
          <div v-if="createType==='excel' && !isEditMode" class="excel-upload-section">
             <el-upload
               ref="uploadRef"
               class="upload-demo"
               drag
               :auto-upload="false"
               :limit="1"
               accept=".xlsx,.xls"
               :on-change="handleFileChange"
               :on-exceed="handleExceed"
               :file-list="fileList"
             >
               <el-icon class="el-icon--upload"><upload-filled /></el-icon>
               <div class="el-upload__text">将 Excel 文件拖到此处，或<em>点击上传</em></div>
               <template #tip><div class="el-upload__tip">只能上传 xlsx/xls 文件，且不超过 10MB</div></template>
             </el-upload>
             <el-divider></el-divider>
             <el-form-item label="模块名称（可选）"><el-input v-model="excelForm.moduleName" placeholder="留空将使用文件名" /></el-form-item>
             <el-form-item label="表名（可选）"><el-input v-model="excelForm.tableName" placeholder="留空将自动生成"><template #prepend>tb_</template></el-input></el-form-item>
          </div>
          
          <!-- CSV导入 -->
          <div v-else-if="createType==='csv' && !isEditMode" class="excel-upload-section">
             <el-radio-group v-model="csvForm.inputMode" style="margin-bottom: 15px">
               <el-radio-button label="file">文件上传</el-radio-button>
               <el-radio-button label="text">文本输入</el-radio-button>
             </el-radio-group>
             
             <div v-if="csvForm.inputMode === 'file'">
               <el-upload
                 ref="uploadRef"
                 class="upload-demo"
                 drag
                 :auto-upload="false"
                 :limit="1"
                 accept=".csv"
                 :on-change="handleFileChange"
                 :on-exceed="handleExceed"
                 :file-list="fileList"
               >
                 <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                 <div class="el-upload__text">将 CSV 文件拖到此处，或<em>点击上传</em></div>
                 <template #tip><div class="el-upload__tip">只能上传 csv 文件，且不超过 10MB</div></template>
               </el-upload>
             </div>
             
             <div v-else>
               <el-input
                 v-model="csvForm.textContent"
                 type="textarea"
                 :rows="10"
                 placeholder="请粘贴CSV内容，例如：&#10;姓名,年龄,部门&#10;张三,25,技术部&#10;李四,30,市场部"
               />
             </div>
             
             <el-divider></el-divider>
             <el-form-item label="分隔符"><el-input v-model="csvForm.delimiter" placeholder="默认为逗号(,)" style="width: 100px" /></el-form-item>
             <el-form-item label="模块名称（可选）"><el-input v-model="csvForm.moduleName" placeholder="留空将自动生成" /></el-form-item>
             <el-form-item label="表名（可选）"><el-input v-model="csvForm.tableName" placeholder="留空将自动生成"><template #prepend>tb_</template></el-input></el-form-item>
          </div>
          
          <!-- JSON导入 -->
          <div v-else-if="createType==='json' && !isEditMode" class="excel-upload-section">
             <el-radio-group v-model="jsonForm.inputMode" style="margin-bottom: 15px">
               <el-radio-button label="file">文件上传</el-radio-button>
               <el-radio-button label="text">文本输入</el-radio-button>
             </el-radio-group>
             
             <div v-if="jsonForm.inputMode === 'file'">
               <el-upload
                 ref="uploadRef"
                 class="upload-demo"
                 drag
                 :auto-upload="false"
                 :limit="1"
                 accept=".json"
                 :on-change="handleFileChange"
                 :on-exceed="handleExceed"
                 :file-list="fileList"
               >
                 <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                 <div class="el-upload__text">将 JSON 文件拖到此处，或<em>点击上传</em></div>
                 <template #tip><div class="el-upload__tip">支持对象数组或包含data字段的对象</div></template>
               </el-upload>
             </div>
             
             <div v-else>
               <el-input
                 v-model="jsonForm.textContent"
                 type="textarea"
                 :rows="10"
                 placeholder="请粘贴JSON内容，例如：&#10;[&#10;  {&quot;name&quot;: &quot;张三&quot;, &quot;age&quot;: 25},&#10;  {&quot;name&quot;: &quot;李四&quot;, &quot;age&quot;: 30}&#10;]"
               />
             </div>
             
             <el-divider></el-divider>
             <el-form-item label="模块名称（可选）"><el-input v-model="jsonForm.moduleName" placeholder="留空将自动生成" /></el-form-item>
             <el-form-item label="表名（可选）"><el-input v-model="jsonForm.tableName" placeholder="留空将自动生成"><template #prepend>tb_</template></el-input></el-form-item>
          </div>
          
          <!-- 数据库表导入 -->
          <div v-else-if="createType==='database' && !isEditMode" class="database-import-section">
             <el-alert type="info" :closable="false" style="margin-bottom: 20px">
               <template #title>从现有数据库表导入</template>
               选择数据库中已存在的表，系统将自动生成对应的模块和字段配置
             </el-alert>
             <el-form-item label="选择数据库表">
               <el-select v-model="databaseForm.selectedTable" placeholder="请选择要导入的表" style="width: 100%" filterable>
                 <el-option v-for="table in existingTables" :key="table.tableName" :label="table.tableName + (table.tableComment ? ' - ' + table.tableComment : '')" :value="table.tableName">
                   <span>{{ table.tableName }}</span>
                   <span style="color: #8492a6; font-size: 13px; margin-left: 8px">{{ table.tableComment || '无注释' }}</span>
                   <span style="float: right; color: #8492a6; font-size: 12px">{{ table.tableRows }} 行</span>
                 </el-option>
               </el-select>
             </el-form-item>
             <el-form-item label="模块名称（可选）"><el-input v-model="databaseForm.moduleName" placeholder="留空将使用表名" /></el-form-item>
          </div>
          
          <!-- 手动创建 -->
          <template v-else-if="createType==='blank'">
            <el-form-item label="模块名称 (显示名)"><el-input v-model="createForm.moduleName" placeholder="例如：员工信息" /></el-form-item>
            <el-form-item label="数据库表名 (英文)">
              <el-input v-model="createForm.tableName" placeholder="employee" :disabled="isEditMode"><template #prepend v-if="!isEditMode">tb_</template></el-input>
              <div v-if="isEditMode" style="font-size: 12px; color: #E6A23C; margin-top: 5px;"><el-icon><InfoFilled /></el-icon> 物理表名创建后不可修改</div>
            </el-form-item>
            <el-form-item label="图标">
               <div class="icon-selector">
                  <div v-for="i in iconOptions" :key="i" class="icon-opt" :class="{ active: createForm.icon === i }" @click="createForm.icon = i"><el-icon size="20"><component :is="i"/></el-icon></div>
               </div>
            </el-form-item>
            <el-form-item label="主题色"><el-color-picker v-model="createForm.themeColor" show-alpha :predefine="predefineColors" /></el-form-item>
          </template>
       </el-form>
       <template #footer>
          <div class="dialog-footer">
            <el-button @click="createDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="loading">
              {{ isEditMode ? '保存修改' : (createType === 'excel' ? '开始导入' : '立即创建') }}
            </el-button>
          </div>
       </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import axios from 'axios';
import { 
  Search, Menu, Tickets, DocumentAdd, Plus, MoreFilled, 
  UploadFilled, Delete, User, Goods, List, Setting, DataLine, 
  Briefcase, Folder, Files, Coin, Odometer, TrendCharts, InfoFilled,
  RefreshLeft, EditPen, SwitchButton
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const emit = defineEmits(['open-module', 'logout', 'open-system-dashboard']);

// --- User Info ---
const userName = ref('');

// 从本地存储获取用户名
const getUserInfo = () => {
  const user = localStorage.getItem('user');
  if (user) {
    try {
      const userObj = JSON.parse(user);
      userName.value = userObj.username || '用户';
    } catch (error) {
      console.error('解析用户信息失败', error);
      userName.value = '用户';
    }
  } else {
    userName.value = '用户';
  }
};

// --- State ---
const showAnalytics = ref(true);
const searchQuery = ref('');
const viewMode = ref<'grid' | 'list'>('grid');
const createDialogVisible = ref(false);
const isEditMode = ref(false);
const createType = ref('blank');
const loading = ref(false);

const modules = ref<any[]>([]);        
const deletedModules = ref<any[]>([]); 
const dashboardStats = ref<any>({      
  totalTables: 0,
  totalRecords: 0,
  newThisWeek: 0,
  storageUsed: '0 KB',
  topModules: []
});

const iconOptions = ['List', 'User', 'Goods', 'Briefcase', 'Folder', 'DataLine', 'Files'];
const predefineColors = ['#e6f7ff', '#f6ffed', '#fff7e6', '#f9f0ff', '#fff0f6'];

const createForm = reactive({ 
  id: null as number | null,
  moduleName: '', 
  tableName: '', 
  icon: 'List',
  themeColor: '#e6f7ff'
});

const excelForm = reactive({
  moduleName: '',
  tableName: ''
});

const csvForm = reactive({
  inputMode: 'file',
  delimiter: ',',
  moduleName: '',
  tableName: '',
  textContent: ''
});

const jsonForm = reactive({
  inputMode: 'file',
  moduleName: '',
  tableName: '',
  textContent: ''
});

const databaseForm = reactive({
  selectedTable: '',
  moduleName: ''
});

const existingTables = ref<any[]>([]);
const fileList = ref<any[]>([]);
const selectedFile = ref<File | null>(null);
const uploadRef = ref();

// --- Computed ---
const filteredDeletedModules = computed(() => {
  if (!searchQuery.value) return deletedModules.value;
  const q = searchQuery.value.toLowerCase();
  return deletedModules.value.filter(m => 
    m.moduleName.toLowerCase().includes(q) || 
    m.tableName.toLowerCase().includes(q)
  );
});

// --- Helpers ---
const timeAgo = (dateString: string) => {
  if(!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);
  
  if (seconds < 60) return "刚刚";
  let interval = seconds / 31536000;
  if (interval > 1) return Math.floor(interval) + " 年前";
  interval = seconds / 2592000;
  if (interval > 1) return Math.floor(interval) + " 个月前";
  interval = seconds / 86400;
  if (interval > 1) return Math.floor(interval) + " 天前";
  interval = seconds / 3600;
  if (interval > 1) return Math.floor(interval) + " 小时前";
  interval = seconds / 60;
  if (interval > 1) return Math.floor(interval) + " 分钟前";
  return "刚刚";
};

const getBarWidth = (count: number) => {
  const max = dashboardStats.value.topModules?.[0]?.recordCount || 1;
  return Math.round((count / max) * 100);
};

const refreshAll = () => {
  fetchModules();
  fetchRecycleBin();
  fetchStats();
};

// --- API Calls ---

let debounceTimer: any = null;

// 修改点 3: 实时搜索 + 防抖处理
const handleSearchInput = () => {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    fetchModules();
  }, 300); // 300ms 延迟
};

const fetchModules = async () => {
  try {
    const res = await axios.get('/api/module/list', { params: { keyword: searchQuery.value } });
    if (res.data.code === 200) {
      modules.value = res.data.data.map((item: any) => ({
        ...item,
        timeAgoStr: timeAgo(item.updatedAt)
      }));
    }
  } catch (error) { console.error(error); }
};

const fetchRecycleBin = async () => {
  try {
    const res = await axios.get('/api/module/recycle/list');
    if (res.data.code === 200) {
      deletedModules.value = res.data.data.map((item: any) => ({
        ...item,
        timeAgoStr: timeAgo(item.updatedAt)
      }));
    }
  } catch (error) { console.error(error); }
};

const fetchStats = async () => {
  try {
    const res = await axios.get('/api/module/stats');
    if (res.data.code === 200) {
        dashboardStats.value = res.data.data || { totalTables: 0, totalRecords: 0, topModules: [] };
    }
  } catch (error) { console.error(error); }
};

// --- Operations ---

const openCreateModal = async (type: string) => {
  createType.value = type;
  isEditMode.value = false;
  createForm.id = null;
  createForm.moduleName = '';
  createForm.tableName = '';
  createForm.icon = 'List';
  createForm.themeColor = '#e6f7ff';
  excelForm.moduleName = '';
  excelForm.tableName = '';
  csvForm.inputMode = 'file';
  csvForm.delimiter = ',';
  csvForm.moduleName = '';
  csvForm.tableName = '';
  csvForm.textContent = '';
  jsonForm.inputMode = 'file';
  jsonForm.moduleName = '';
  jsonForm.tableName = '';
  jsonForm.textContent = '';
  databaseForm.selectedTable = '';
  databaseForm.moduleName = '';
  fileList.value = [];
  selectedFile.value = null;
  
  if (type === 'database') {
    await fetchExistingTables();
  }
  
  createDialogVisible.value = true;
};

const fetchExistingTables = async () => {
  try {
    const res = await axios.get('/api/module/existing-tables');
    if (res.data.code === 200) {
      existingTables.value = res.data.data || [];
    }
  } catch (error) {
    console.error(error);
    ElMessage.error('获取数据库表列表失败');
  }
};

const getDialogTitle = () => {
  if (isEditMode.value) return '编辑模块配置';
  switch (createType.value) {
    case 'excel': return '导入 Excel 创建表';
    case 'csv': return '导入 CSV 创建表';
    case 'json': return '导入 JSON 创建表';
    case 'database': return '从数据库表导入';
    case 'blank': return '新建数据表';
    default: return '新建数据表';
  }
};

const handleEdit = async (mod: any) => {
  loading.value = true;
  try {
    const res = await axios.get(`/api/module/${mod.id}`);
    if (res.data.code === 200) {
      const data = res.data.data;
      createForm.id = data.id;
      createForm.moduleName = data.moduleName;
      createForm.tableName = data.tableName;
      createForm.icon = data.icon;
      createForm.themeColor = data.themeColor;
      isEditMode.value = true;
      createType.value = 'blank';
      createDialogVisible.value = true;
    } else {
      ElMessage.error("获取详情失败");
    }
  } catch (e) {
    ElMessage.error("网络错误");
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  if(createType.value === 'excel' && !isEditMode.value) {
    if (!selectedFile.value) {
      ElMessage.warning("请选择要上传的Excel文件");
      return;
    }
    
    loading.value = true;
    try {
      const formData = new FormData();
      formData.append('file', selectedFile.value);
      if (excelForm.moduleName) {
        formData.append('moduleName', excelForm.moduleName);
      }
      if (excelForm.tableName) {
        formData.append('tableName', excelForm.tableName);
      }
      
      const res = await axios.post('/api/module/import-excel', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      if (res.data.code === 200) {
        const result = res.data.data;
        ElMessage.success(`导入成功！共导入 ${result.successRows} 条数据`);
        createDialogVisible.value = false;
        refreshAll();
      } else {
        ElMessage.error(res.data.message || "导入失败");
      }
    } catch (error: any) {
      console.error(error);
      ElMessage.error(error.response?.data?.message || "导入失败，请检查文件格式");
    } finally {
      loading.value = false;
    }
    return;
  }
  
  if(createType.value === 'csv' && !isEditMode.value) {
    if (csvForm.inputMode === 'file') {
      if (!selectedFile.value) {
        ElMessage.warning("请选择要上传的CSV文件");
        return;
      }
      
      loading.value = true;
      try {
        const formData = new FormData();
        formData.append('file', selectedFile.value);
        if (csvForm.delimiter) {
          formData.append('delimiter', csvForm.delimiter);
        }
        if (csvForm.moduleName) {
          formData.append('moduleName', csvForm.moduleName);
        }
        if (csvForm.tableName) {
          formData.append('tableName', csvForm.tableName);
        }
        
        const res = await axios.post('/api/module/import-csv', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        
        if (res.data.code === 200) {
          const result = res.data.data;
          ElMessage.success(`导入成功！共导入 ${result.successRows} 条数据`);
          createDialogVisible.value = false;
          refreshAll();
        } else {
          ElMessage.error(res.data.message || "导入失败");
        }
      } catch (error: any) {
        console.error(error);
        ElMessage.error(error.response?.data?.message || "导入失败，请检查文件格式");
      } finally {
        loading.value = false;
      }
      return;
    } else {
      if (!csvForm.textContent || !csvForm.textContent.trim()) {
        ElMessage.warning("请输入CSV内容");
        return;
      }
      
      loading.value = true;
      try {
        const res = await axios.post('/api/module/import-csv-text', {
          csvContent: csvForm.textContent,
          delimiter: csvForm.delimiter,
          moduleName: csvForm.moduleName,
          tableName: csvForm.tableName
        });
        
        if (res.data.code === 200) {
          const result = res.data.data;
          ElMessage.success(`导入成功！共导入 ${result.successRows} 条数据`);
          createDialogVisible.value = false;
          refreshAll();
        } else {
          ElMessage.error(res.data.message || "导入失败");
        }
      } catch (error: any) {
        console.error(error);
        ElMessage.error(error.response?.data?.message || "导入失败，请检查格式");
      } finally {
        loading.value = false;
      }
      return;
    }
  }
  
  if(createType.value === 'json' && !isEditMode.value) {
    if (jsonForm.inputMode === 'file') {
      if (!selectedFile.value) {
        ElMessage.warning("请选择要上传的JSON文件");
        return;
      }
      
      loading.value = true;
      try {
        const formData = new FormData();
        formData.append('file', selectedFile.value);
        if (jsonForm.moduleName) {
          formData.append('moduleName', jsonForm.moduleName);
        }
        if (jsonForm.tableName) {
          formData.append('tableName', jsonForm.tableName);
        }
        
        const res = await axios.post('/api/module/import-json', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        
        if (res.data.code === 200) {
          const result = res.data.data;
          ElMessage.success(`导入成功！共导入 ${result.successRows} 条数据`);
          createDialogVisible.value = false;
          refreshAll();
        } else {
          ElMessage.error(res.data.message || "导入失败");
        }
      } catch (error: any) {
        console.error(error);
        ElMessage.error(error.response?.data?.message || "导入失败，请检查文件格式");
      } finally {
        loading.value = false;
      }
      return;
    } else {
      if (!jsonForm.textContent || !jsonForm.textContent.trim()) {
        ElMessage.warning("请输入JSON内容");
        return;
      }
      
      loading.value = true;
      try {
        const res = await axios.post('/api/module/import-json-text', {
          jsonContent: jsonForm.textContent,
          moduleName: jsonForm.moduleName,
          tableName: jsonForm.tableName
        });
        
        if (res.data.code === 200) {
          const result = res.data.data;
          ElMessage.success(`导入成功！共导入 ${result.successRows} 条数据`);
          createDialogVisible.value = false;
          refreshAll();
        } else {
          ElMessage.error(res.data.message || "导入失败");
        }
      } catch (error: any) {
        console.error(error);
        ElMessage.error(error.response?.data?.message || "导入失败，请检查格式");
      } finally {
        loading.value = false;
      }
      return;
    }
  }
  
  if(createType.value === 'database' && !isEditMode.value) {
    if (!databaseForm.selectedTable) {
      ElMessage.warning("请选择要导入的数据库表");
      return;
    }
    
    loading.value = true;
    try {
      const res = await axios.post('/api/module/import-database', null, {
        params: {
          tableName: databaseForm.selectedTable,
          moduleName: databaseForm.moduleName || undefined
        }
      });
      
      if (res.data.code === 200) {
        const result = res.data.data;
        ElMessage.success(`导入成功！共 ${result.totalRows} 条数据`);
        createDialogVisible.value = false;
        refreshAll();
      } else {
        ElMessage.error(res.data.message || "导入失败");
      }
    } catch (error: any) {
      console.error(error);
      ElMessage.error(error.response?.data?.message || "导入失败");
    } finally {
      loading.value = false;
    }
    return;
  }
  
  if(!createForm.moduleName || !createForm.tableName) {
    ElMessage.warning("请填写完整信息");
    return;
  }

  loading.value = true;
  try {
    if (isEditMode.value) {
      if (!createForm.id) {
         ElMessage.error("ID 丢失，请刷新重试");
         loading.value = false;
         return;
      }
      const payload = {
        id: createForm.id,
        moduleName: createForm.moduleName,
        icon: createForm.icon,
        themeColor: createForm.themeColor
      };
      const res = await axios.put('/api/module/update', payload);
      if (res.data.code === 200) {
        ElMessage.success("更新成功");
        createDialogVisible.value = false;
        refreshAll();
      } else {
        ElMessage.error(res.data.message || "更新失败");
      }
    } else {
      const payload = {
        moduleName: createForm.moduleName,
        tableName: createForm.tableName.startsWith('tb_') ? createForm.tableName : 'tb_' + createForm.tableName,
        icon: createForm.icon,
        themeColor: createForm.themeColor
      };
      const res = await axios.post('/api/module/create', payload);
      if (res.data.code === 200) {
        ElMessage.success("创建成功");
        createDialogVisible.value = false;
        refreshAll();
      } else {
        ElMessage.error(res.data.message || "创建失败");
      }
    }
  } catch (error) {
    console.error(error);
    ElMessage.error("系统错误");
  } finally {
    loading.value = false;
  }
};

const handleFileChange = (file: any, fileList: any[]) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!');
    return false;
  }
  
  const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls');
  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件!');
    return false;
  }
  
  selectedFile.value = file.raw;
  return true;
};

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件');
};

const handleCommand = (cmd: string, mod: any) => {
  if (cmd === 'edit') {
    handleEdit(mod);
  } else if (cmd === 'softDelete') {
    ElMessageBox.confirm(`确定将 "${mod.moduleName}" 移入回收站吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '移入回收站'
    }).then(async () => {
      const res = await axios.delete(`/api/module/${mod.id}`);
      if(res.data.code === 200) {
        ElMessage.success('已移入回收站');
        refreshAll();
      } else {
        ElMessage.error(res.data.message);
      }
    });
  }
};

const handleRecover = async (mod: any) => {
  try {
    const res = await axios.post(`/api/module/recover/${mod.id}`);
    if (res.data.code === 200) {
      ElMessage.success('恢复成功');
      refreshAll();
    }
  } catch (e) { ElMessage.error('恢复失败'); }
};

const handleHardDelete = async (mod: any) => {
  ElMessageBox.confirm(`确定彻底删除 "${mod.moduleName}" 吗？物理表将被 DROP，此操作不可恢复！`, '高危操作', {
      type: 'warning',
      confirmButtonText: '彻底粉碎',
      confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/module/hard/${mod.id}`);
      if (res.data.code === 200) {
        ElMessage.success('彻底删除成功');
        refreshAll();
      }
    } catch (e) { ElMessage.error('删除失败'); }
  });
};

const emitNavigate = (mod: any) => emit('open-module', mod);

// 处理登出
const handleLogout = () => {
  emit('logout');
};

onMounted(() => {
  getUserInfo();
  refreshAll();
});
</script>

<style scoped>
/* 基础容器 */
.workspace-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  color: #333;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 1. Toolbar */
.workspace-toolbar {
  padding: 12px 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  z-index: 10;
}
.toolbar-left { display: flex; align-items: center; gap: 15px; }
.workspace-title { margin: 0; font-size: 18px; font-weight: 600; color: #303133; }
.analytics-toggle { 
  display: flex; align-items: center; gap: 5px; cursor: pointer; color: #606266; font-size: 14px; 
  padding: 5px 10px; border-radius: 4px; transition: background 0.2s;
}
.analytics-toggle:hover { background: #f2f6fc; color: #409EFF; }
.analytics-toggle.active { background: #ecf5ff; color: #409EFF; }

.toolbar-right { display: flex; align-items: center; gap: 15px; }
.search-input { width: 220px; }
.view-toggle { display: flex; background: #f2f6fc; padding: 3px; border-radius: 6px; }
.toggle-btn { 
  padding: 5px 10px; cursor: pointer; color: #909399; display: flex; transition: all 0.2s; border-radius: 4px;
}
.toggle-btn:hover { color: #303133; }
.toggle-btn.active { background: #fff; color: #409EFF; box-shadow: 0 1px 2px rgba(0,0,0,0.1); }

/* 用户信息样式 */
.user-info { margin-left: 10px; }
.user-avatar { 
  display: flex; align-items: center; gap: 8px; 
  padding: 6px 12px; border-radius: 20px; 
  cursor: pointer; transition: background 0.2s;
}
.user-avatar:hover { background: #f2f6fc; }
.user-avatar span { font-size: 14px; color: #606266; }

/* 内容区 */
.workspace-content {
  padding: 25px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

/* 2. 仪表盘 */
.analytics-dashboard { margin-bottom: 30px; }
.kpi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 20px; }
.kpi-card { 
  background: #fff; border-radius: 8px; padding: 20px; display: flex; align-items: center; gap: 15px; 
  box-shadow: 0 2px 6px rgba(0,0,0,0.04); border: 1px solid #ebeef5;
}
.kpi-icon { width: 48px; height: 48px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.kpi-icon.blue { background: #ecf5ff; color: #409EFF; }
.kpi-icon.green { background: #f0f9eb; color: #67C23A; }
.kpi-icon.orange { background: #fdf6ec; color: #E6A23C; }
.kpi-icon.purple { background: #f4f4f5; color: #909399; }
.kpi-data { display: flex; flex-direction: column; }
.kpi-value { font-size: 22px; font-weight: bold; color: #303133; }
.kpi-label { font-size: 12px; color: #909399; margin-top: 2px; }

.charts-row { display: flex; gap: 20px; height: 260px; }
.chart-panel { background: #fff; border-radius: 8px; padding: 20px; display: flex; flex-direction: column; border: 1px solid #ebeef5; }
.flex-2 { flex: 2; }
.flex-1 { flex: 1; }
.panel-header { display: flex; justify-content: space-between; margin-bottom: 15px; font-weight: bold; color: #303133; font-size: 15px; }

.bar-chart-container { flex: 1; display: flex; flex-direction: column; justify-content: space-around; }
.bar-row { display: flex; align-items: center; gap: 10px; font-size: 13px; }
.bar-label { width: 120px; display: flex; align-items: center; gap: 6px; color: #606266; }
.text-ellipsis { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.bar-track { flex: 1; height: 8px; background: #f2f6fc; border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; transition: width 0.6s ease; }
.bar-value { width: 50px; text-align: right; color: #909399; font-family: monospace; }

.quota-box { display: flex; flex-direction: column; gap: 25px; padding-top: 15px; }
.quota-item { display: flex; flex-direction: column; gap: 8px; font-size: 13px; }
.q-info { display: flex; justify-content: space-between; color: #606266; }
.api-tip { margin-top: auto; background: #f4f4f5; padding: 10px; border-radius: 4px; color: #909399; font-size: 12px; display: flex; align-items: center; gap: 6px; }

/* 3. 分区标题 */
.section-label { display: flex; align-items: center; gap: 12px; margin-bottom: 15px; font-size: 12px; font-weight: bold; color: #909399; text-transform: uppercase; letter-spacing: 0.5px; }
.mt-20 { margin-top: 20px; }
.mt-30 { margin-top: 30px; }
.divider { flex: 1; height: 1px; background: #ebeef5; }

/* 4. 构建网格 */
.creation-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 20px; }
.create-card { 
  background: #fff; border: 1px dashed #dcdfe6; border-radius: 8px; padding: 20px; 
  display: flex; align-items: center; gap: 15px; cursor: pointer; transition: all 0.2s; 
}
.create-card:hover { border-color: #409EFF; background: #ecf5ff; transform: translateY(-2px); }
.c-icon-bg { width: 44px; height: 44px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 22px; }
.c-icon-bg.green { background: #f0f9eb; color: #67C23A; }
.c-icon-bg.blue { background: #ecf5ff; color: #409EFF; }
.c-info { display: flex; flex-direction: column; }
.c-title { font-weight: bold; font-size: 15px; color: #303133; }
.c-desc { font-size: 12px; color: #909399; margin-top: 4px; }

/* 5. 模块网格 */
.modules-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; }
.module-card { 
  background: #fff; border: 1px solid #ebeef5; border-radius: 8px; padding: 18px; 
  height: 140px; display: flex; flex-direction: column; cursor: pointer; transition: all 0.2s; 
}
.module-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); border-color: #dcdfe6; transform: translateY(-3px); }
.m-card-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.m-icon { width: 36px; height: 36px; border-radius: 6px; display: flex; align-items: center; justify-content: center; color: #303133; font-size: 18px; }
.m-action-btn { padding: 4px; border-radius: 4px; transition: background 0.2s; }
.m-action-btn:hover { background: #f2f6fc; }
.m-more-icon { color: #909399; transform: rotate(90deg); cursor: pointer; }

.m-card-body { flex: 1; }
.m-name { font-weight: bold; font-size: 15px; color: #303133; margin-bottom: 6px; }
.m-meta { font-size: 12px; color: #909399; background: #f4f4f5; padding: 2px 6px; border-radius: 4px; display: inline-block; font-family: monospace; }
.m-card-footer { display: flex; justify-content: space-between; font-size: 12px; color: #C0C4CC; border-top: 1px solid #f9f9f9; padding-top: 10px; }

/* 6. 列表视图 */
.modules-list { background: #fff; border: 1px solid #ebeef5; border-radius: 8px; font-size: 14px; }
.list-header { background: #fafafa; color: #909399; font-weight: bold; font-size: 13px; border-bottom: 1px solid #ebeef5; padding: 12px 20px; display: grid; grid-template-columns: 2fr 1.5fr 1fr 1fr 50px; }
.list-item { padding: 12px 20px; border-bottom: 1px solid #f9f9f9; display: grid; grid-template-columns: 2fr 1.5fr 1fr 1fr 50px; align-items: center; cursor: pointer; transition: background 0.2s; }
.list-item:hover { background: #f5f7fa; }
.list-icon { width: 28px; height: 28px; border-radius: 4px; display: flex; align-items: center; justify-content: center; margin-right: 10px; font-size: 14px; }
.col-name { display: flex; align-items: center; font-weight: 500; }
.col-db { color: #606266; font-family: monospace; }

/* 7. 回收站特定样式 */
.deleted-card { border: 1px dashed #dcdfe6; background: #fafafa; opacity: 0.8; }
.deleted-card:hover { border-color: #909399; opacity: 1; }
.deleted-list-item { background: #fafafa; opacity: 0.8; }
.grayscale { filter: grayscale(100%); opacity: 0.6; }
.deleted-actions { display: flex; gap: 8px; }

/* 8. 弹窗样式 */
.excel-upload-section {
  padding: 10px 0;
}

.upload-demo {
  width: 100%;
}

.upload-demo :deep(.el-upload) {
  width: 100%;
}

.upload-demo :deep(.el-upload-dragger) {
  width: 100%;
}

.el-upload__tip {
  color: #909399;
  font-size: 12px;
  margin-top: 7px;
}

.upload-box { border: 1px dashed #dcdfe6; background: #fafafa; padding: 30px; text-align: center; border-radius: 6px; color: #909399; }
.icon-selector { display: flex; gap: 10px; flex-wrap: wrap; }
.icon-opt { width: 36px; height: 36px; border: 1px solid #dcdfe6; border-radius: 4px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; color: #606266; }
.icon-opt:hover { border-color: #409EFF; color: #409EFF; }
.icon-opt.active { background: #ecf5ff; border-color: #409EFF; color: #409EFF; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }
</style>