<template>
  <div class="smart-grid-container">
    <!-- 1. 顶部栏 -->
    <div class="view-header">
      <div class="header-left">
        <div class="back-btn" @click="$emit('back')">
          <el-icon><Back /></el-icon>
        </div>
        <div class="table-info">
          <div class="icon-wrapper" :style="{ backgroundColor: activeModule.themeColor || '#e6f7ff' }">
            <el-icon :style="{ color: '#1890ff' }">
              <component :is="activeModule.icon || 'List'" />
            </el-icon>
          </div>
          <div class="info-text">
            <span class="table-name">{{ activeModule.moduleName }}</span>
            <span class="db-name">{{ activeModule.tableName }}</span>
          </div>
        </div>
      </div>
      <div class="view-tabs">
        <div class="tab-item active"><el-icon><Menu /></el-icon> 网格视图</div>
        <div class="tab-item"><el-icon><Picture /></el-icon> 画廊视图</div>
      </div>
      <div class="header-right">
        <!-- 顶部导出入口 -->
        <el-button link @click="openExportDrawer">
          <el-icon><Download /></el-icon> 导出 Excel
        </el-button>
      </div>
    </div>

    <!-- 2. 工具栏 -->
    <div class="grid-toolbar">
      <!-- A. 批量操作模式 (当有选中行时显示) -->
      <div v-if="selectedRows.length > 0" class="batch-actions">
        <span class="selected-count">已选中 {{ selectedRows.length }} 项</span>
        <div class="divider-vertical"></div>
        <el-button type="danger" bg size="small" @click="batchDelete">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
        <el-button type="success" bg size="small" @click="openExportDrawer">
          <el-icon><Download /></el-icon> 导出选中
        </el-button>
        <el-button type="default" link size="small" @click="clearSelection">取消选择</el-button>
      </div>

      <!-- B. 常规工具栏 -->
      <div v-else class="tool-group">
        <!-- 筛选 -->
        <el-popover placement="bottom-start" :width="460" trigger="click">
          <template #reference>
            <div class="tool-btn" :class="{ active: filterList.length > 0 }">
              <el-icon><Filter /></el-icon> 筛选
              <span v-if="filterList.length" class="badge">{{ filterList.length }}</span>
            </div>
          </template>
          <div class="filter-panel">
            <div v-if="filterList.length === 0" class="empty-filter">暂无筛选条件</div>
            <div v-for="(item, index) in filterList" :key="index" class="filter-row">
              <div class="logic">{{ index === 0 ? 'Where' : 'And' }}</div>
              <el-select v-model="item.col" size="small" placeholder="字段" style="width:140px">
                <el-option v-for="c in columns" :key="c.prop" :label="c.label" :value="c.prop"/>
              </el-select>
              <el-select v-model="item.op" size="small" placeholder="条件" style="width:100px">
                <el-option label="包含" value="like" />
                <el-option label="等于" value="eq" />
              </el-select>
              <el-input v-model="item.val" size="small" placeholder="值" style="flex:1" />
              <el-button link type="danger" :icon="Close" @click="filterList.splice(index, 1)" />
            </div>
            <div class="filter-footer">
              <el-button link type="primary" size="small" :icon="Plus" @click="addFilter">添加条件</el-button>
              <el-button type="primary" size="small" @click="fetchData">应用</el-button>
            </div>
          </div>
        </el-popover>

        <!-- 排序 -->
        <el-popover placement="bottom-start" :width="320" trigger="click">
          <template #reference>
            <div class="tool-btn" :class="{ active: sortState.field }">
              <el-icon><Sort /></el-icon> 排序
            </div>
          </template>
          <div class="sort-panel">
            <div class="sort-row">
              <span>字段</span>
              <el-select v-model="sortState.field" size="small" clearable>
                <el-option v-for="c in columns" :key="c.prop" :label="c.label" :value="c.prop"/>
              </el-select>
            </div>
            <div class="sort-row">
              <span>方向</span>
              <el-radio-group v-model="sortState.order" size="small">
                <el-radio-button label="ASC">升序</el-radio-button>
                <el-radio-button label="DESC">降序</el-radio-button>
              </el-radio-group>
            </div>
            <div style="text-align:right; margin-top:10px;">
               <el-button type="primary" size="small" @click="fetchData">应用排序</el-button>
            </div>
          </div>
        </el-popover>

        <!-- 显示列 -->
        <el-popover placement="bottom-start" :width="200" trigger="click">
          <template #reference>
            <div class="tool-btn"><el-icon><Operation /></el-icon> 显示列</div>
          </template>
          <el-scrollbar max-height="300px">
            <div v-for="col in columns" :key="col.prop" style="padding: 5px 10px;">
              <el-checkbox v-model="col.visible">{{ col.label }}</el-checkbox>
            </div>
          </el-scrollbar>
        </el-popover>

        <div class="divider"></div>
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索..." 
          prefix-icon="Search" 
          size="small" 
          class="search-input" 
          clearable 
          @input="handleSearch"
        />
      </div>

      <div class="tool-group">
        <el-button type="primary" size="small" @click="openRowDrawer(null)">
          <el-icon class="mr-1"><Plus /></el-icon> 添加记录
        </el-button>
      </div>
    </div>

    <!-- 3. 表格主体 -->
    <div class="grid-body" v-loading="tableLoading">
      <el-table 
        ref="tableRef"
        :data="tableData" 
        border 
        height="100%" 
        style="width: 100%" 
        class="nocodb-table" 
        :header-cell-style="headerCellStyle" 
        :cell-style="cellStyle"
        @selection-change="handleSelectionChange"
      >
        <!-- A. 多选列 -->
        <el-table-column type="selection" width="50" align="center" fixed="left" />

        <!-- 动态列渲染 -->
        <template v-for="(col, index) in visibleColumns" :key="col.prop">
          <el-table-column 
            :prop="col.prop" 
            :width="col.width || 180" 
            resizable 
            show-overflow-tooltip
          >
            <!-- 表头 -->
            <template #header>
              <div class="custom-header">
                <el-icon class="header-icon" :class="col.uiType">
                  <component :is="getIconByUiType(col.uiType)" />
                </el-icon>
                <span class="header-title" :title="col.label">{{ col.label }}</span>
                
                <!-- 字段菜单 -->
                <el-dropdown trigger="click" class="header-menu" @command="(cmd) => handleColumnCommand(cmd, col)">
                  <span class="header-trigger"><el-icon><ArrowDown /></el-icon></span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit"><el-icon><EditPen /></el-icon> 编辑字段</el-dropdown-item>
                      <el-dropdown-item v-if="col.prop !== 'id'" command="delete" style="color:#f56c6c" divided><el-icon><Delete /></el-icon> 删除字段</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>

            <!-- 单元格内容 -->
            <template #default="{ row }">
              <div class="cell-wrapper" :class="{ 'primary-cell': index === 0 }">
                
                <!-- 1. 附件/图片 -->
                <div v-if="col.uiType === 'Attachment'" class="cell-attachment" @click.stop>
                   <div v-if="row[col.prop]" class="file-wrapper">
                      <el-image 
                        v-if="isImage(row[col.prop])" 
                        :src="getFileUrl(row[col.prop], 'view')" 
                        :preview-src-list="[getFileUrl(row[col.prop], 'view')]" 
                        class="table-thumb"
                        fit="cover"
                        preview-teleported
                        @click.stop
                      />
                      <div v-else class="file-chip" @click.stop="downloadFile(row[col.prop])">
                        <el-icon><Document /></el-icon> <span>文件</span>
                      </div>
                   </div>
                   <span v-else class="no-data">-</span>
                </div>

                <!-- 2. 标签 (自动颜色) -->
                <div v-else-if="col.uiType === 'Select'" class="cell-tag">
                  <span v-if="row[col.prop]" class="noco-tag" :style="getColorHash(row[col.prop])">
                    {{ row[col.prop] }}
                  </span>
                </div>

                <!-- 3. 开关 -->
                <div v-else-if="col.uiType === 'Switch'" class="cell-center">
                  <el-switch v-model="row[col.prop]" :active-value="1" :inactive-value="0" size="small" disabled />
                </div>

                <!-- 4. 评分 -->
                <div v-else-if="col.uiType === 'Rating'" class="cell-center">
                  <el-rate v-model="row[col.prop]" disabled size="small" />
                </div>

                <!-- 5. 链接 -->
                <div v-else-if="col.uiType === 'URL'" class="cell-link">
                  <a :href="row[col.prop]" target="_blank" v-if="row[col.prop]">{{ row[col.prop] }}</a>
                </div>

                <!-- 6. 货币 -->
                <span v-else-if="col.uiType === 'Currency'" style="font-family: monospace; color:#16a34a;">
                  {{ row[col.prop] ? '¥ ' + row[col.prop] : '-' }}
                </span>
                
                <!-- 7. 默认 -->
                <span v-else class="cell-text">{{ row[col.prop] }}</span>
              </div>
            </template>
          </el-table-column>
        </template>

        <!-- B. 操作列 (固定右侧) -->
        <el-table-column label="操作" width="100" fixed="right" align="center" header-align="center">
          <template #default="{ row }">
            <div class="row-ops-fixed">
              <el-tooltip content="编辑" placement="top" :show-after="500">
                <el-button link type="primary" size="small" @click.stop="openRowDrawer(row)">
                  <el-icon size="16"><EditPen /></el-icon>
                </el-button>
              </el-tooltip>
              <el-popconfirm title="确定删除?" @confirm="deleteRow(row)">
                <template #reference>
                  <el-button link type="danger" size="small" @click.stop>
                    <el-icon size="16"><Delete /></el-icon>
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>

        <!-- C. 添加列按钮 -->
        <el-table-column width="50" fixed="right" align="center" header-align="center">
          <template #header>
             <div class="add-col-trigger" @click="openColDrawer(false)">
               <el-icon><Plus /></el-icon>
             </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页栏 -->
    <div class="grid-pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
        small
        background
      />
    </div>

    <!-- 4. 数据编辑抽屉 -->
    <el-drawer 
      v-model="rowDrawerVisible" 
      :title="currentRow ? '编辑记录' : '新增记录'" 
      size="500px" 
      destroy-on-close
    >
      <div class="drawer-body">
        <el-form :model="rowData" label-position="top">
          <div v-for="col in columns" :key="col.prop" class="form-field-wrapper">
             <template v-if="col.prop === 'id'">
               <div class="field-label">ID</div>
               <el-input v-model="rowData[col.prop]" disabled />
             </template>
             <template v-else>
               <div class="field-label">
                 <el-icon><component :is="getIconByUiType(col.uiType)"/></el-icon>{{ col.label }}
               </div>
               
               <!-- 附件上传 -->
               <div v-if="col.uiType === 'Attachment'">
                  <el-upload
                    class="upload-box-wrapper"
                    drag
                    :action="UPLOAD_ACTION_URL"
                    :show-file-list="false"
                    :on-success="(res) => handleUploadSuccess(res, col.prop)"
                    :before-upload="(file) => beforeUpload(file, col.options)"
                  >
                    <div v-if="rowData[col.prop]" class="uploaded-card">
                       <div class="card-left" @click.stop>
                           <img v-if="isImage(rowData[col.prop])" :src="getFileUrl(rowData[col.prop], 'view')" class="mini-preview"/>
                           <el-icon v-else size="24" color="#909399"><Document /></el-icon>
                       </div>
                       <div class="card-center">
                          <div class="file-title" :title="rowData[col.prop]">{{ rowData[col.prop].split('/').pop() }}</div>
                          <div class="file-tip">点击此处更换文件</div>
                       </div>
                       <div class="card-right" @click.stop="rowData[col.prop] = null">
                          <el-icon class="delete-icon"><CircleCloseFilled /></el-icon>
                       </div>
                    </div>
                    <div v-else class="upload-empty-state">
                      <el-icon class="cloud-icon"><UploadFilled /></el-icon>
                      <div class="upload-text">点击或拖拽文件到这里</div>
                      <div class="limit-tip" v-if="col.options">支持格式: {{ col.options }}</div>
                    </div>
                  </el-upload>
               </div>

               <!-- 动态输入组件 -->
               <el-input v-else-if="col.uiType === 'Input'" v-model="rowData[col.prop]" />
               <el-input-number v-else-if="col.uiType === 'InputNumber' || col.uiType === 'Currency'" v-model="rowData[col.prop]" style="width:100%" controls-position="right" />
               <el-date-picker v-else-if="col.uiType === 'DatePicker'" v-model="rowData[col.prop]" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
               <div v-else-if="col.uiType === 'Switch'" class="switch-wrapper">
                  <el-switch v-model="rowData[col.prop]" :active-value="1" :inactive-value="0" />
               </div>
               <el-select v-else-if="col.uiType === 'Select'" v-model="rowData[col.prop]" style="width:100%" clearable filterable allow-create>
                  <el-option v-for="opt in parseOptions(col.options)" :key="opt" :label="opt" :value="opt" />
               </el-select>
               <el-rate v-else-if="col.uiType === 'Rating'" v-model="rowData[col.prop]" clearable />
               <el-input v-else type="textarea" :rows="3" v-model="rowData[col.prop]" />
             </template>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="rowDrawerVisible=false">取消</el-button>
          <el-button type="primary" @click="saveRow" :loading="submitting">保存</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 5. 字段编辑抽屉 -->
    <el-drawer 
      v-model="colDrawerVisible" 
      :title="isEditColumn ? '编辑字段' : '添加新字段'" 
      size="420px" 
      append-to-body
    >
       <el-form :model="newColData" label-position="top">
          <el-form-item label="显示名称">
             <el-input v-model="newColData.label" placeholder="例如：状态" />
          </el-form-item>
          
          <el-form-item label="字段名 (英文)">
             <el-input v-model="newColData.prop" placeholder="status" :disabled="false">
                <template #prepend v-if="!isEditColumn">col_</template>
             </el-input>
             <div v-if="isEditColumn" class="form-tip warning">
                <el-icon><InfoFilled /></el-icon> 注意：修改字段名或类型可能导致数据丢失
             </div>
          </el-form-item>
          
          <el-form-item label="字段类型">
             <div class="type-grid">
                <div v-for="t in fieldTypes" :key="t.value" class="type-item" :class="{active: newColData.uiType === t.value}" @click="newColData.uiType = t.value">
                   <el-icon><component :is="t.icon"/></el-icon><span>{{ t.label }}</span>
                </div>
             </div>
          </el-form-item>

          <el-form-item v-if="newColData.uiType === 'Select'" label="预设选项">
             <el-select
                v-model="tempOptions"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="输入后按回车 Enter"
                style="width: 100%"
             ></el-select>
          </el-form-item>

          <el-form-item v-if="newColData.uiType === 'Attachment'" label="文件后缀限制">
             <el-input v-model="newColData.options" placeholder=".jpg,.png,.pdf" />
             <div class="form-tip">留空则不限制类型</div>
          </el-form-item>
       </el-form>
       <template #footer>
          <el-button type="primary" style="width: 100%" @click="submitColumn" :loading="submitting">
             {{ isEditColumn ? '保存修改' : '确认创建' }}
          </el-button>
       </template>
    </el-drawer>

    <!-- [新增] 6. 导出配置抽屉 -->
    <el-drawer v-model="exportDrawerVisible" title="导出到 Excel" size="400px" append-to-body>
       <div class="export-config">
         <div class="config-section">
           <div class="section-title">1. 选择数据范围</div>
           <el-radio-group v-model="exportConfig.scope" class="scope-radio">
             <el-radio label="all" border>所有结果 ({{ pagination.total }})</el-radio>
             <el-radio label="selected" border :disabled="selectedRows.length === 0">
               仅选中行 ({{ selectedRows.length }})
             </el-radio>
           </el-radio-group>
         </div>

         <div class="config-section">
           <div class="section-title">
             2. 选择导出字段
             <el-checkbox v-model="exportConfig.checkAll" :indeterminate="isIndeterminate" @change="handleCheckAllChange" size="small" style="float:right">全选</el-checkbox>
           </div>
           <el-scrollbar max-height="300px" class="col-list">
             <el-checkbox-group v-model="exportConfig.checkedColumns" @change="handleCheckedColumnsChange">
               <div v-for="col in columns" :key="col.prop" class="col-item">
                 <el-checkbox :label="col.prop">{{ col.label }}</el-checkbox>
               </div>
             </el-checkbox-group>
           </el-scrollbar>
         </div>
       </div>
       <template #footer>
          <el-button @click="exportDrawerVisible = false">取消</el-button>
          <el-button type="primary" @click="handleExport" :loading="exportLoading">
             <el-icon class="mr-1"><Download /></el-icon> 确认导出
          </el-button>
       </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue';
import axios from 'axios';
import { 
  Back, Plus, Search, EditPen, Delete, Document, Sort, Calendar, ArrowDownBold, Select,
  Menu, Filter, Download, MoreFilled, ArrowDown, InfoFilled,
  Message, Link, Star, Money, Paperclip, UploadFilled, Close, CircleCloseFilled, Picture, DataBoard
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const props = defineProps({ activeModule: { type: Object, required: true } });
const emit = defineEmits(['back']);

const BASE_SERVER = 'http://localhost:8080';
const API_PREFIX = '/api'; 
// 上传地址必须绝对路径
const UPLOAD_ACTION_URL = `${BASE_SERVER}/api/file/upload`; 

// --- State ---
const tableLoading = ref(false);
const submitting = ref(false);
const searchKeyword = ref('');
const columns = ref<any[]>([]);
const tableData = ref<any[]>([]);
const rowDrawerVisible = ref(false);
const currentRow = ref<any>(null);
const rowData = ref<any>({});
const tableRef = ref();

// Export State
const exportDrawerVisible = ref(false);
const exportLoading = ref(false);
const exportConfig = reactive({
  scope: 'all',
  checkAll: true,
  checkedColumns: [] as string[]
});
const isIndeterminate = computed(() => exportConfig.checkedColumns.length > 0 && exportConfig.checkedColumns.length < columns.value.length);

// Selection
const selectedRows = ref<any[]>([]);

// Field Edit
const colDrawerVisible = ref(false);
const isEditColumn = ref(false);
const newColData = ref({ id: null, label: '', prop: '', uiType: 'Input', options: '', oldProp: '' });
const tempOptions = ref<string[]>([]);
const filterList = ref<any[]>([]);
const sortState = reactive({ field: '', order: 'DESC' });
const pagination = reactive({ page: 1, size: 20, total: 0 });

const fieldTypes = [
  { label: '单行文本', value: 'Input', icon: Document },
  { label: '数字', value: 'InputNumber', icon: Sort },
  { label: '下拉单选', value: 'Select', icon: ArrowDownBold },
  { label: '附件/文件', value: 'Attachment', icon: Paperclip }, 
  { label: '开关', value: 'Switch', icon: Select },
  { label: '日期', value: 'DatePicker', icon: Calendar },
  { label: '评分', value: 'Rating', icon: Star },
  { label: '货币', value: 'Currency', icon: Money },
  { label: '多行文本', value: 'Textarea', icon: Document },
];

const visibleColumns = computed(() => columns.value.filter(c => c.visible));

// --- Helpers ---
const getColorHash = (str: string) => {
  if (!str) return {};
  let hash = 0;
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash);
  const c = (hash & 0x00ffffff).toString(16).toUpperCase();
  const hex = "#" + "00000".substring(0, 6 - c.length) + c;
  return { backgroundColor: hex + '20', color: hex, border: `1px solid ${hex}40` };
};

const getFileUrl = (fileName: string, type: 'view' | 'download') => {
  if (!fileName) return '';
  if (fileName.startsWith('http')) return fileName;
  const pureName = fileName.replace('/files/', '');
  return `${BASE_SERVER}/api/file/${type}?fileName=${encodeURIComponent(pureName)}`;
};

const downloadFile = (fileName: string) => {
  if (!fileName) return;
  window.open(getFileUrl(fileName, 'download'), '_blank');
};

const isImage = (fileName: string) => /\.(jpg|jpeg|png|gif|webp|bmp)$/i.test(fileName);

const parseOptions = (optStr: string) => {
  if (!optStr) return [];
  try {
    const arr = JSON.parse(optStr);
    return Array.isArray(arr) ? arr : [];
  } catch (e) {
    return optStr.split(/[,，]/).filter(s => s);
  }
};

const getIconByUiType = (type: string) => {
  const found = fieldTypes.find(t => t.value === type);
  return found ? found.icon : Document;
};

// --- API Calls ---
const fetchSchema = async () => {
  try {
    const res = await axios.get(`${API_PREFIX}/crud/${props.activeModule.tableName}/schema`);
    if(res.data.code === 200) {
      columns.value = res.data.data.map((c: any) => ({ ...c, visible: c.isShowInList === 1 }));
    }
  } catch (e) { console.error(e); }
};

const fetchData = async () => {
  tableLoading.value = true;
  try {
    const res = await axios.get(`${API_PREFIX}/crud/${props.activeModule.tableName}/list`, {
      params: { 
        keyword: searchKeyword.value,
        filters: JSON.stringify(filterList.value),
        sortField: sortState.field,
        sortOrder: sortState.order,
        page: pagination.page,
        size: pagination.size
      }
    });
    if (res.data.code === 200) {
      tableData.value = res.data.data.list;
      pagination.total = res.data.data.total;
    }
  } catch (e) { ElMessage.error("数据加载失败"); } 
  finally { tableLoading.value = false; }
};

const saveRow = async () => {
  submitting.value = true;
  try {
    const res = await axios.post(`${API_PREFIX}/crud/${props.activeModule.tableName}/save`, rowData.value);
    if(res.data.code === 200) {
      ElMessage.success("保存成功");
      rowDrawerVisible.value = false;
      fetchData();
    } else { ElMessage.error(res.data.message); }
  } catch (e) { ElMessage.error("保存失败"); }
  finally { submitting.value = false; }
};

const deleteRow = async (row: any) => {
  try {
    const res = await axios.delete(`${API_PREFIX}/crud/${props.activeModule.tableName}/${row.id}`);
    if(res.data.code === 200) { ElMessage.success("删除成功"); fetchData(); }
  } catch(e) { ElMessage.error("删除失败"); }
};

// --- Export Logic ---
const openExportDrawer = () => {
  exportConfig.checkedColumns = columns.value.map(c => c.prop);
  exportConfig.checkAll = true;
  exportConfig.scope = selectedRows.value.length > 0 ? 'selected' : 'all';
  exportDrawerVisible.value = true;
};
const handleCheckAllChange = (val: boolean) => {
  exportConfig.checkedColumns = val ? columns.value.map(c => c.prop) : [];
};
const handleCheckedColumnsChange = (value: string[]) => {
  exportConfig.checkAll = value.length === columns.value.length;
};
const handleExport = async () => {
  if (exportConfig.checkedColumns.length === 0) return ElMessage.warning("请至少选择一列");
  exportLoading.value = true;
  try {
    const selectedColsInfo = columns.value.filter(c => exportConfig.checkedColumns.includes(c.prop)).map(c => ({ prop: c.prop, label: c.label }));
    const ids = exportConfig.scope === 'selected' ? selectedRows.value.map(r => r.id) : [];
    const payload = {
      ids: ids,
      keyword: searchKeyword.value,
      filters: JSON.stringify(filterList.value),
      columns: selectedColsInfo
    };
    const res = await axios.post(`${API_PREFIX}/crud/${props.activeModule.tableName}/export`, payload, { responseType: 'blob' });
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    const timestamp = new Date().toISOString().slice(0,10);
    link.setAttribute('download', `${props.activeModule.moduleName}_${timestamp}.xlsx`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    ElMessage.success("导出成功");
    exportDrawerVisible.value = false;
  } catch (e) { ElMessage.error("导出失败"); }
  finally { exportLoading.value = false; }
};

// --- Column Ops ---
const handleColumnCommand = (cmd: string, col: any) => {
  if (cmd === 'edit') openColDrawer(true, col);
};

const openColDrawer = (isEdit: boolean, colData?: any) => {
  isEditColumn.value = isEdit;
  colDrawerVisible.value = true;
  if (isEdit && colData) {
    newColData.value = { ...colData, oldProp: colData.prop };
    tempOptions.value = colData.uiType === 'Select' ? parseOptions(colData.options) : [];
  } else {
    newColData.value = { id: null, label: '', prop: '', uiType: 'Input', options: '', oldProp: '' };
    tempOptions.value = [];
  }
};

const submitColumn = async () => {
  if(!newColData.value.label || !newColData.value.prop) return ElMessage.warning("请填写完整");
  if (newColData.value.uiType === 'Select') newColData.value.options = JSON.stringify(tempOptions.value);
  submitting.value = true;
  try {
    const method = isEditColumn.value ? 'put' : 'post';
    const payload = isEditColumn.value ? { ...newColData.value } : { ...newColData.value, prop: 'col_' + newColData.value.prop };
    const res = await axios[method](`${API_PREFIX}/crud/${props.activeModule.tableName}/column`, payload);
    if(res.data.code === 200) {
       ElMessage.success("操作成功");
       colDrawerVisible.value = false;
       fetchSchema(); fetchData();
    } else { ElMessage.error(res.data.message); }
  } catch (e) { ElMessage.error("操作失败"); } 
  finally { submitting.value = false; }
};

// --- Upload ---
const handleUploadSuccess = (res: any, prop: string) => {
  if (res.code === 200) { rowData.value[prop] = res.data; ElMessage.success("上传成功"); } 
  else { ElMessage.error(res.message); }
};
const beforeUpload = (file: any, accept: string) => {
  if (file.size / 1024 / 1024 > 50) return false;
  return true;
};

// --- Others ---
const handleSelectionChange = (val: any[]) => selectedRows.value = val;
const clearSelection = () => tableRef.value?.clearSelection();
const batchDelete = () => {
  ElMessageBox.confirm(`确定删除 ${selectedRows.value.length} 条记录？`, '批量删除', { type: 'warning' }).then(async () => {
      for (const row of selectedRows.value) {
        try { await axios.delete(`${API_PREFIX}/crud/${props.activeModule.tableName}/${row.id}`); } catch(e){}
      }
      ElMessage.success(`删除完成`);
      clearSelection(); fetchData();
  });
};

const addFilter = () => filterList.value.push({ col: '', op: 'like', val: '' });

const openRowDrawer = (row: any) => {
  currentRow.value = row;
  rowData.value = row ? { ...row } : {};
  if (!row) columns.value.forEach(c => rowData.value[c.prop] = c.uiType === 'Switch' ? 0 : null);
  rowDrawerVisible.value = true;
};

let debounceTimer: any = null;
const handleSearch = () => {
  if(debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => { pagination.page = 1; fetchData(); }, 300);
};

const headerCellStyle = { background: '#f7f9fd', color: '#666', fontWeight: '600', height: '44px', padding: '0', fontSize: '13px', borderRight: '1px solid #e0e0e0', borderBottom: '1px solid #e0e0e0' };
const cellStyle = { padding: '0', height: '44px', borderRight: '1px solid #eee', fontSize: '13px', color: '#333' };

watch(() => props.activeModule, () => {
  if(props.activeModule) { 
    columns.value = []; tableData.value = []; filterList.value = []; pagination.page = 1;
    fetchSchema(); fetchData(); 
  }
}, { immediate: true });
</script>

<style scoped>
/* 核心容器 */
.smart-grid-container { height: 100%; display: flex; flex-direction: column; background: #fff; font-family: 'Inter', -apple-system, sans-serif; }
.view-header { height: 50px; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center; padding: 0 16px; background: #fff; user-select: none; }
.header-left { display: flex; align-items: center; gap: 12px; }
.back-btn { cursor: pointer; padding: 6px; border-radius: 4px; color: #666; display: flex; align-items: center; }
.back-btn:hover { background: #f0f0f0; color: #333; }
.table-info { display: flex; align-items: center; gap: 10px; }
.icon-wrapper { width: 32px; height: 32px; border-radius: 6px; display: flex; align-items: center; justify-content: center; }
.info-text { display: flex; flex-direction: column; line-height: 1.2; }
.table-name { font-weight: 700; font-size: 14px; color: #333; }
.db-name { font-family: monospace; font-size: 11px; color: #999; }
.view-tabs { display: flex; height: 100%; align-items: flex-end; gap: 20px; margin-left: 20px; }
.tab-item { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #666; padding-bottom: 14px; cursor: pointer; border-bottom: 2px solid transparent; }
.tab-item:hover { color: #1890ff; }
.tab-item.active { color: #1890ff; border-bottom-color: #1890ff; font-weight: 500; }
.grid-toolbar { height: 44px; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center; padding: 0 16px; background: #fff; }
.tool-group { display: flex; align-items: center; gap: 8px; }
.tool-btn { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #555; cursor: pointer; padding: 4px 8px; border-radius: 4px; position: relative; }
.tool-btn:hover { background: #f0f0f0; }
.tool-btn.active { background: #e6f7ff; color: #1890ff; font-weight: 500; }
.badge { background: #1890ff; color: #fff; font-size: 10px; padding: 0 4px; border-radius: 4px; margin-left: 4px; height: 16px; line-height: 16px; }
.divider { width: 1px; height: 16px; background: #ddd; margin: 0 4px; }
.divider-vertical { width: 1px; height: 16px; background: #ddd; margin: 0 10px; }
.search-input { width: 220px; }
.batch-actions { display: flex; align-items: center; background: #fff1f0; padding: 4px 12px; border-radius: 4px; border: 1px solid #ffa39e; }
.selected-count { font-size: 13px; color: #f56c6c; font-weight: 500; margin-right: 5px; }

.grid-body { flex: 1; overflow: hidden; display: flex; flex-direction: column; }
.grid-pagination { padding: 10px 16px; border-top: 1px solid #e0e0e0; display: flex; justify-content: flex-end; background: #fff; }

/* Header & Cell Styles */
.custom-header { display: flex; align-items: center; width: 100%; height: 100%; cursor: pointer; padding-left: 8px; }
.custom-header:hover { background: #eceff5; }
.custom-header:hover .header-menu { opacity: 1; }
.header-menu { margin-left: auto; margin-right: 8px; opacity: 0; transition: opacity 0.2s; color: #999; }
.header-trigger { padding: 2px; border-radius: 4px; display: flex; opacity: 0; transition: opacity 0.2s; } /* Fixed opacity transition */
.custom-header:hover .header-trigger { opacity: 1; }
.header-trigger:hover { background: #dcdfe6; }
.header-icon { margin-right: 6px; font-size: 14px; color: #999; }
.header-title { font-size: 13px; color: #333; flex: 1; font-weight: 600; }

/* Cell Content Styles */
.cell-wrapper { display: flex; align-items: center; width: 100%; height: 100%; padding: 0 8px; }
.primary-cell { font-weight: 600; color: #303133; }
.cell-text { display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cell-center { display: flex; justify-content: center; width: 100%; }
.cell-link a { color: #1890ff; text-decoration: none; }
.cell-currency { font-family: monospace; color: #16a34a; }
.noco-tag { padding: 2px 8px; border-radius: 12px; font-size: 12px; font-weight: 500; display: inline-block; white-space: nowrap; }
.row-ops-fixed { display: flex; justify-content: center; gap: 4px; }
.add-col-trigger { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; cursor: pointer; color: #999; }
.add-col-trigger:hover { background: #f0f0f0; color: #1890ff; }

/* Attachment & Upload */
.cell-attachment { display: flex; align-items: center; padding: 0 10px; height: 100%; width: 100%; }
.file-wrapper { display: flex; align-items: center; }
.table-thumb { width: 30px; height: 30px; border-radius: 4px; border: 1px solid #eee; cursor: zoom-in; }
.file-chip { display: flex; align-items: center; gap: 4px; background: #f5f7fa; padding: 2px 8px; border-radius: 12px; border: 1px solid #eee; cursor: pointer; font-size: 12px; color: #606266; transition: all 0.2s; }
.file-chip:hover { background: #ecf5ff; color: #1890ff; border-color: #b3d8ff; }
.no-data { color: #eee; }
.upload-box-wrapper { width: 100%; }
.upload-box-wrapper :deep(.el-upload) { width: 100%; display: block; }
.upload-box-wrapper :deep(.el-upload-dragger) { width: 100%; height: auto; min-height: 120px; padding: 0; border: 1px dashed #dcdfe6; background-color: #fafafa; display: flex; justify-content: center; align-items: center; transition: border-color 0.3s; }
.upload-box-wrapper :deep(.el-upload-dragger:hover) { border-color: #409EFF; background-color: #ecf5ff; }
.upload-empty-state { padding: 20px; display: flex; flex-direction: column; align-items: center; color: #606266; }
.cloud-icon { font-size: 32px; color: #909399; margin-bottom: 8px; }
.uploaded-card { width: 100%; height: 100%; min-height: 120px; display: flex; align-items: center; padding: 15px; box-sizing: border-box; background: #fff; gap: 15px; }
.card-left { width: 50px; height: 50px; border-radius: 4px; border: 1px solid #eee; display: flex; align-items: center; justify-content: center; background: #f9f9f9; overflow: hidden; }
.mini-preview { width: 100%; height: 100%; object-fit: cover; }
.card-center { flex: 1; overflow: hidden; text-align: left; }
.file-title { font-size: 14px; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-weight: 500; }
.file-tip { font-size: 12px; color: #909399; margin-top: 4px; }
.card-right { cursor: pointer; padding: 5px; }
.delete-icon { font-size: 20px; color: #F56C6C; transition: transform 0.2s; }
.delete-icon:hover { transform: scale(1.2); }

/* Export Drawer */
.export-config { padding: 10px; }
.config-section { margin-bottom: 25px; }
.section-title { font-weight: 600; font-size: 14px; margin-bottom: 10px; color: #303133; }
.scope-radio { display: flex; flex-direction: column; gap: 10px; align-items: stretch; }
.radio-tip { font-size: 12px; color: #909399; margin-top: 2px; margin-left: 24px; }
.col-list { border: 1px solid #ebeef5; border-radius: 4px; padding: 10px; }
.col-item { padding: 5px 0; }

/* Common Drawers */
.drawer-body { padding: 20px; }
.form-field-wrapper { margin-bottom: 20px; }
.field-label { display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 600; color: #666; margin-bottom: 6px; }
.drawer-footer { display: flex; justify-content: flex-end; gap: 10px; }
.type-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.type-item { display: flex; align-items: center; gap: 8px; padding: 10px; border: 1px solid #eee; border-radius: 6px; cursor: pointer; transition: all 0.2s; font-size: 13px; color: #555; }
.type-item:hover { border-color: #1890ff; color: #1890ff; }
.type-item.active { background: #e6f7ff; border-color: #1890ff; color: #1890ff; font-weight: bold; }
.form-tip { font-size: 12px; color: #999; margin-top: 4px; }
.form-tip.warning { color: #e6a23c; }

/* Panels */
.filter-panel, .sort-panel { padding: 12px; }
.empty-filter { text-align: center; color: #999; font-size: 12px; padding: 10px 0; }
.filter-row { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.logic { font-size: 12px; color: #999; width: 40px; text-align: center; font-weight: bold; }
.filter-footer { margin-top: 10px; padding-top: 10px; border-top: 1px dashed #eee; display: flex; justify-content: space-between; }
.sort-row { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; font-size: 13px; color: #666; }
</style>