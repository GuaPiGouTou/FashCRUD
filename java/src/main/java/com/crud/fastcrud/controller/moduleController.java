package com.crud.fastcrud.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crud.fastcrud.config.JsonResult;
import com.crud.fastcrud.dto.ExcelImportResult;
import com.crud.fastcrud.dto.ImportResult;
import com.crud.fastcrud.entity.module;
import com.crud.fastcrud.mapper.moduleMapper;
import com.crud.fastcrud.service.Impl.moduleServiceImpl;
import com.crud.fastcrud.service.ExcelImportService;
import com.crud.fastcrud.service.CsvImportService;
import com.crud.fastcrud.service.JsonImportService;
import com.crud.fastcrud.service.DatabaseImportService;
import com.crud.fastcrud.service.moduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/module")
public class moduleController {
    @Autowired
    private moduleServiceImpl moduleServiceimpl;
    @Autowired
    private moduleMapper moduleMapper;
    @Autowired
    private ExcelImportService excelImportService;
    @Autowired
    private CsvImportService csvImportService;
    @Autowired
    private JsonImportService jsonImportService;
    @Autowired
    private DatabaseImportService databaseImportService;
    /**
     * 1. 获取模块列表
     */
    @GetMapping("/list")
    public JsonResult<List<module>> selectListMoudule(@RequestParam(required = false) String keyword) {
        // 将 keyword 传给 Service 层
        List<module> modules = moduleServiceimpl.selectModuleList(keyword);

        if (modules.isEmpty()) {
            return new JsonResult<>(200, modules, "null");
        }
        return new JsonResult<>(200, modules, "success");
    }

    /**
     * 2. 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    public JsonResult<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = moduleServiceimpl.getDashboardStats();
            return new JsonResult<>(200, stats, "success");
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 3. 创建新模块 (手动)
     */
    @PostMapping("/create")
    public JsonResult<String> createModule(@RequestBody module mod) {
        // 简单参数校验
        if (mod.getModuleName() == null || mod.getTableName() == null) {
            return new JsonResult<>(400, null, "模块名和表名不能为空");
        }

        // 自动补充 tb_ 前缀 (可选，根据你前端是否传前缀决定)
        if (!mod.getTableName().startsWith("tb_") && !mod.getTableName().startsWith("t_")) {
            mod.setTableName("tb_" + mod.getTableName());
        }

        try {
            moduleServiceimpl.createModule(mod);
            return new JsonResult<>(200, "创建成功", "success");
        } catch (RuntimeException re) {
            return new JsonResult<>(400, null, re.getMessage()); // 业务异常
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "系统异常: " + e.getMessage());
        }
    }

    /**
     * 4. 删除模块
     */
    @DeleteMapping("/{id}")
    public JsonResult<String> deleteModule(@PathVariable Long id) {
        try {
            boolean success = moduleServiceimpl.deleteModule(id);
            if (success) {
                return new JsonResult<>(200, "删除成功", "success");
            } else {
                return new JsonResult<>(404, null, "模块不存在或已删除");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "删除失败: " + e.getMessage());
        }
    }
    /**
     * 5. 获取单个模块详情 (查)
     */
    @GetMapping("/{id}")
    public JsonResult<module> getModuleById(@PathVariable Long id) {
        module mod = moduleServiceimpl.getById(id);
        if (mod != null) {
            return new JsonResult<>(200, mod, "success");
        } else {
            return new JsonResult<>(404, null, "模块不存在");
        }
    }

    /**
     * 6. 更新模块信息 (改)
     */
    @PutMapping("/update")
    public JsonResult<String> updateModule(@RequestBody module mod) {
        try {
            boolean success = moduleServiceimpl.updateModule(mod);
            if (success) {
                return new JsonResult<>(200, "更新成功", "success");
            } else {
                return new JsonResult<>(500, null, "更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "系统异常: " + e.getMessage());
        }
    }
    // [新增] 获取回收站列表
    @GetMapping("/recycle/list")
    public JsonResult<List<module>> getRecycleList() {
        List<module> list = moduleServiceimpl.getRecycleBinList();
        return new JsonResult<>(200, list, "success");
    }

    // [新增] 恢复
    @PostMapping("/recover/{id}")
    public JsonResult<String> recover(@PathVariable Long id) {
        moduleServiceimpl.recoverModule(id);
        return new JsonResult<>(200, "恢复成功", "success");
    }

    // [新增] 彻底删除
    @DeleteMapping("/hard/{id}")
    public JsonResult<String> hardDelete(@PathVariable Long id) {
        moduleServiceimpl.hardDeleteModule(id);
        return new JsonResult<>(200, "彻底删除成功", "success");
    }

    // [新增] Excel导入并自动建表
    @PostMapping("/import-excel")
    public JsonResult<ExcelImportResult> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "moduleName", required = false) String moduleName,
            @RequestParam(value = "tableName", required = false) String tableName) {
        try {
            ExcelImportResult result = excelImportService.importExcelAndCreateTable(file, moduleName, tableName);
            if (result.isSuccess()) {
                return new JsonResult<>(200, result, "导入成功");
            } else {
                return new JsonResult<>(400, result, result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "导入失败: " + e.getMessage());
        }
    }

    // [新增] CSV导入并自动建表
    @PostMapping("/import-csv")
    public JsonResult<ImportResult> importCsv(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "moduleName", required = false) String moduleName,
            @RequestParam(value = "tableName", required = false) String tableName,
            @RequestParam(value = "delimiter", required = false) String delimiter) {
        try {
            ImportResult result = csvImportService.importCsvAndCreateTable(file, moduleName, tableName, delimiter);
            if (result.isSuccess()) {
                return new JsonResult<>(200, result, "导入成功");
            } else {
                return new JsonResult<>(400, result, result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "导入失败: " + e.getMessage());
        }
    }

    // [新增] CSV文本导入
    @PostMapping("/import-csv-text")
    public JsonResult<ImportResult> importCsvText(
            @RequestBody Map<String, String> request) {
        try {
            String csvContent = request.get("csvContent");
            String moduleName = request.get("moduleName");
            String tableName = request.get("tableName");
            String delimiter = request.get("delimiter");
            
            ImportResult result = csvImportService.importCsvTextAndCreateTable(csvContent, moduleName, tableName, delimiter);
            if (result.isSuccess()) {
                return new JsonResult<>(200, result, "导入成功");
            } else {
                return new JsonResult<>(400, result, result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "导入失败: " + e.getMessage());
        }
    }

    // [新增] JSON导入并自动建表
    @PostMapping("/import-json")
    public JsonResult<ImportResult> importJson(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "moduleName", required = false) String moduleName,
            @RequestParam(value = "tableName", required = false) String tableName) {
        try {
            ImportResult result = jsonImportService.importJsonAndCreateTable(file, moduleName, tableName);
            if (result.isSuccess()) {
                return new JsonResult<>(200, result, "导入成功");
            } else {
                return new JsonResult<>(400, result, result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "导入失败: " + e.getMessage());
        }
    }

    // [新增] JSON文本导入
    @PostMapping("/import-json-text")
    public JsonResult<ImportResult> importJsonText(
            @RequestBody Map<String, String> request) {
        try {
            String jsonContent = request.get("jsonContent");
            String moduleName = request.get("moduleName");
            String tableName = request.get("tableName");
            
            ImportResult result = jsonImportService.importJsonTextAndCreateTable(jsonContent, moduleName, tableName);
            if (result.isSuccess()) {
                return new JsonResult<>(200, result, "导入成功");
            } else {
                return new JsonResult<>(400, result, result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "导入失败: " + e.getMessage());
        }
    }

    // [新增] 获取现有数据库表列表
    @GetMapping("/existing-tables")
    public JsonResult<List<Map<String, Object>>> getExistingTables() {
        try {
            List<Map<String, Object>> tables = databaseImportService.getExistingTables();
            return new JsonResult<>(200, tables, "success");
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "获取表列表失败: " + e.getMessage());
        }
    }

    // [新增] 从现有数据库表导入
    @PostMapping("/import-database")
    public JsonResult<ImportResult> importDatabase(
            @RequestParam("tableName") String tableName,
            @RequestParam(value = "moduleName", required = false) String moduleName) {
        try {
            ImportResult result = databaseImportService.importExistingTable(tableName, moduleName);
            if (result.isSuccess()) {
                return new JsonResult<>(200, result, "导入成功");
            } else {
                return new JsonResult<>(400, result, result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "导入失败: " + e.getMessage());
        }
    }
}
