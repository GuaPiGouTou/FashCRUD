package com.crud.fastcrud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.crud.fastcrud.entity.SysFieldConfig;
import com.crud.fastcrud.mapper.CrudMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrudService {

    @Autowired
    private CrudMapper crudMapper;

    /**
     * 1. 获取表结构
     */
    public List<SysFieldConfig> getSchema(String tableName) {
        return crudMapper.selectList(new QueryWrapper<SysFieldConfig>()
                .eq("table_name", tableName)
                .orderByAsc("sort_order"));
    }

    /**
     * 2. 获取数据 (智能搜索)
     */
    // 在 CrudService 中修改 getData 方法

    public Map<String, Object> getData(
            String tableName,
            String keyword,
            String filterJson,
            String sortField,
            String sortOrder,
            int page, // 当前页 (1开始)
            int size  // 每页条数
    ) {
        // 1. 处理搜索列 (保持不变)
        List<String> searchableColumns = new ArrayList<>();
        List<SysFieldConfig> configs = getSchema(tableName);
        if (keyword != null && !keyword.trim().isEmpty()) {
            for (SysFieldConfig config : configs) {
                String type = config.getUiType();
                if ("Input".equals(type) || "Textarea".equals(type) || "Email".equals(type) || "URL".equals(type)) {
                    searchableColumns.add(config.getProp());
                }
            }
        }

        // 2. 解析筛选条件 (JSON -> List<Map>)
        List<Map<String, Object>> filterList = new ArrayList<>();
        if (filterJson != null && !filterJson.isEmpty()) {
            try {
                // 使用 Jackson 或 FastJson 解析
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, String>> rawFilters = mapper.readValue(filterJson, new TypeReference<List<Map<String, String>>>(){});

                for (Map<String, String> f : rawFilters) {
                    Map<String, Object> sqlFilter = new HashMap<>();
                    sqlFilter.put("col", f.get("col"));
                    String op = f.get("op"); // eq, like, gt, lt
                    String val = f.get("val");

                    // 简单的操作符映射
                    switch (op) {
                        case "eq": sqlFilter.put("sqlOp", "="); sqlFilter.put("val", val); break;
                        case "ne": sqlFilter.put("sqlOp", "!="); sqlFilter.put("val", val); break;
                        case "gt": sqlFilter.put("sqlOp", ">"); sqlFilter.put("val", val); break;
                        case "lt": sqlFilter.put("sqlOp", "<"); sqlFilter.put("val", val); break;
                        case "like":
                            sqlFilter.put("sqlOp", "LIKE");
                            sqlFilter.put("val", "%" + val + "%");
                            break;
                    }
                    filterList.add(sqlFilter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 3. 计算分页
        int offset = (page - 1) * size;

        // 4. 执行查询
        List<LinkedHashMap<String, Object>> list = crudMapper.selectDynamicList(
                tableName, keyword, searchableColumns, filterList, sortField, sortOrder, offset, size
        );

        // 5. 查询总数
        Long total = crudMapper.countDynamicList(tableName, keyword, searchableColumns, filterList);

        // 6. 封装返回
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
      }

    /**
     * 3. 保存数据 (新增或修改)
     */
    @Transactional
    public void saveData(String tableName, Map<String, Object> data) {
        // 清理系统字段，防止前端回传导致报错
        data.remove("created_time");
        data.remove("updated_time");

        // 提取 ID
        Object idObj = data.get("id");
        Long id = null;
        if (idObj != null && !"".equals(idObj.toString())) {
            id = Long.parseLong(idObj.toString());
        }
        data.remove("id"); // 操作前移除 ID

        // 构建 SQL 片段
        List<String> keyList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();
        List<String> updateList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            String sqlVal;

            if (val == null || "".equals(val)) {
                sqlVal = "NULL";
            } else if (val instanceof Boolean) {
                // 将 true/false 转为 1/0 (MySQL TinyInt)
                sqlVal = ((Boolean) val) ? "1" : "0";
            } else {
                // 基础防注入：转义单引号
                sqlVal = "'" + val.toString().replace("'", "\\'") + "'";
            }

            keyList.add(key);
            valueList.add(sqlVal);
            updateList.add(key + " = " + sqlVal);
        }

        if (id != null) {
            // Update
            String setSql = String.join(", ", updateList);
            // 如果 map 为空（只传了id），则不执行更新
            if (!setSql.isEmpty()) {
                crudMapper.updateDynamic(tableName, id, setSql);
            }
        } else {
            // Insert
            String keys = String.join(", ", keyList);
            String values = String.join(", ", valueList);
            if (!keys.isEmpty()) {
                crudMapper.insertDynamic(tableName, keys, values);
            }
        }
    }

    /**
     * 4. 删除数据
     */
    public void deleteData(String tableName, Long id) {
        crudMapper.deleteDynamic(tableName, id);
    }

    /**
     * 5. 新增字段 (核心映射逻辑)
     */
    @Transactional
    public void addColumn(String tableName, Map<String, String> colDef) {
        String prop = colDef.get("prop");
        String label = colDef.get("label");
        String uiType = colDef.get("uiType");
        String options = colDef.get("options");

        // --- 核心：将前端 UI 类型映射为 MySQL 物理类型 ---
        String sqlType = "VARCHAR(255) DEFAULT NULL";

        switch (uiType) {
            case "InputNumber":
            case "Rating":      // 评分存整数 1-5
                sqlType = "INT DEFAULT 0";
                break;
            case "Currency":    // 货币存高精度小数
                sqlType = "DECIMAL(10,2) DEFAULT 0.00";
                break;
            case "Switch":      // 开关存 0/1
                sqlType = "TINYINT(1) DEFAULT 0";
                break;
            case "DatePicker":  // 日期
                sqlType = "DATETIME DEFAULT NULL";
                break;
            case "Textarea":    // 长文本
                sqlType = "TEXT";
                break;
            case "Attachment":  // [新增] 附件类型
                // 使用 TEXT 存储文件路径（如果是多文件可用 JSON 存）
                sqlType = "TEXT";
                break;
            default:
                sqlType = "VARCHAR(255) DEFAULT NULL";
                break;
        }

        // 1. 修改物理表结构
        crudMapper.addColumn(tableName, prop, sqlType);

        // 2. 插入元数据配置
        SysFieldConfig config = new SysFieldConfig();
        config.setTableName(tableName);
        config.setProp(prop);
        config.setLabel(label);
        config.setUiType(uiType);
        config.setOptions(options);

        // 默认配置
        config.setIsShowInList(1);
        config.setSortOrder(99);

        crudMapper.insert(config);
    }
    /**
     * 修改字段
     */
    @Transactional
    public void updateColumn(String tableName, Map<String, Object> colDef) {
        Long id = Long.parseLong(colDef.get("id").toString());
        String oldProp = colDef.get("oldProp").toString(); // 旧字段名(用于SQL CHANGE)
        String newProp = colDef.get("prop").toString();
        String label = colDef.get("label").toString();
        String uiType = colDef.get("uiType").toString();
        String options = colDef.get("options") != null ? colDef.get("options").toString() : null;

        // 1. 计算新的 SQL 类型
        String sqlType = "VARCHAR(255) DEFAULT NULL";
        switch (uiType) {
            case "InputNumber":
            case "Rating":
                sqlType = "INT DEFAULT 0";
                break;
            case "Currency":
                sqlType = "DECIMAL(10,2) DEFAULT 0.00";
                break;
            case "Switch":
                sqlType = "TINYINT(1) DEFAULT 0";
                break;
            case "DatePicker":
                sqlType = "DATETIME DEFAULT NULL";
                break;
            case "Textarea":
            case "Attachment": // 附件也用 TEXT
                sqlType = "TEXT";
                break;
        }

        // 2. 执行物理修改 (DDL)
        // 如果字段名没变，oldProp 和 newProp 一样，MySQL 也是支持的，仅修改类型
        crudMapper.modifyColumn(tableName, oldProp, newProp, sqlType);

        // 3. 更新元数据配置
        SysFieldConfig config = new SysFieldConfig();
        config.setId(id);
        config.setProp(newProp);
        config.setLabel(label);
        config.setUiType(uiType);
        config.setOptions(options);
        crudMapper.updateById(config);
    }

    public void exportData(String tableName, Map<String, Object> payload, HttpServletResponse response) throws Exception {
        // 1. 解析参数
        List<Integer> ids = (List<Integer>) payload.get("ids"); // 选中的ID
        String keyword = (String) payload.get("keyword");
        List<Map<String, String>> reqColumns = (List<Map<String, String>>) payload.get("columns"); // 前端传来的列配置 [{prop: 'name', label: '姓名'}]

        // 2. 构建查询逻辑
        List<LinkedHashMap<String, Object>> dataList;

        if (ids != null && !ids.isEmpty()) {
            // A. 如果选了特定行，直接按 ID 查
            // 这里需要你在 Mapper 加一个 selectByIds 或者是拼接 SQL
            // 简单起见，复用动态查询，拼接 id IN (...)
            String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
            // 为了简单演示，这里假设你有个 selectByIdsDynamic 的 Mapper 方法，或者拼在 where 里
            // 下面演示一种通用做法：复用 getData 但加一个专门的 filter
            List<Map<String, Object>> idFilter = new ArrayList<>();
            Map<String, Object> f = new HashMap<>();
            f.put("col", "id");
            f.put("sqlOp", "IN");
            f.put("val", "(" + idStr + ")");
            // 注意：这里的 sqlOp IN 处理需要在 Mapper XML 中特殊处理一下，或者循环调用
            // 为了代码简洁，这里建议在 CrudMapper 增加一个 selectByIds
            dataList = crudMapper.selectDynamicByIds(tableName, ids);
        } else {
            // B. 否则导出当前筛选条件下的所有数据 (不分页)
            // 复用之前的查询逻辑，但 offset=0, limit=100000 (大数)
            // 注意：需自行解析 filters json
            String filtersJson = (String) payload.get("filters");
            // ... 解析 filters ...
            // 这里为了演示，简化为只传 keyword
            List<String> searchCols = new ArrayList<>();
            // ... 获取搜索列逻辑同 getData ...

            // 这里的 limit 给个大数代表“全部”
            dataList = crudMapper.selectDynamicList(tableName, keyword, searchCols, null, null, null, 0, 100000);
        }

        // 3. 准备 Excel 表头和数据
        // EasyExcel 动态表头需要 List<List<String>>
        List<List<String>> head = new ArrayList<>();
        List<String> selectProps = new ArrayList<>();

        for (Map<String, String> col : reqColumns) {
            List<String> headRow = new ArrayList<>();
            headRow.add(col.get("label")); // 中文表头
            head.add(headRow);
            selectProps.add(col.get("prop")); // 记录对应的英文数据库字段
        }

        // 转换数据：Map -> List<Object> (按表头顺序)
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> map : dataList) {
            List<Object> row = new ArrayList<>();
            for (String prop : selectProps) {
                row.add(map.get(prop));
            }
            rows.add(row);
        }

        // 4. 写出流
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(tableName + "_export", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream())
                .head(head)
                .sheet("Sheet1")
                .doWrite(rows);
    }
}