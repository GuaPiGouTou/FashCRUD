package com.crud.fastcrud.controller;

import com.crud.fastcrud.config.JsonResult; // 你的统一返回类
import com.crud.fastcrud.entity.SysFieldConfig;
import com.crud.fastcrud.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/crud")
public class CrudController {

    @Autowired
    private CrudService crudService;

    /**
     * 1. 获取列定义 (Schema)
     */
    @GetMapping("/{tableName}/schema")
    public JsonResult<List<SysFieldConfig>> getSchema(@PathVariable String tableName) {
        return new JsonResult<>(200, crudService.getSchema(tableName), "success");
    }

    /**
     * 2. 获取动态数据列表
     */
    @GetMapping("/{tableName}/list")
    public JsonResult<Map<String, Object>> getList(
            @PathVariable String tableName,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filters,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return new JsonResult<>(200,
                crudService.getData(tableName, keyword, filters, sortField, sortOrder, page, size),
                "success");
    }

    /**
     * 3. 保存数据 (新增或修改)
     */
    @PostMapping("/{tableName}/save")
    public JsonResult<String> save(@PathVariable String tableName, @RequestBody Map<String, Object> data) {
        try {
            crudService.saveData(tableName, data);
            return new JsonResult<>(200, "保存成功", "success");
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "保存失败: " + e.getMessage());
        }
    }

    /**
     * 4. 删除数据
     */
    @DeleteMapping("/{tableName}/{id}")
    public JsonResult<String> delete(@PathVariable String tableName, @PathVariable Long id) {
        crudService.deleteData(tableName, id);
        return new JsonResult<>(200, "删除成功", "success");
    }

    /**
     * 5. 新增字段 (元数据 + DDL)
     */
    @PostMapping("/{tableName}/column")
    public JsonResult<String> addColumn(@PathVariable String tableName, @RequestBody Map<String, String> colDef) {
        try {
            crudService.addColumn(tableName, colDef);
            return new JsonResult<>(200, "字段添加成功", "success");
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "添加字段失败: " + e.getMessage());
        }
    }
    /**
     * 6. 修改字段配置
     */
    @PutMapping("/{tableName}/column")
    public JsonResult<String> updateColumn(@PathVariable String tableName, @RequestBody Map<String, Object> colDef) {
        try {
            crudService.updateColumn(tableName, colDef);
            return new JsonResult<>(200, "字段修改成功", "success");
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "修改失败: " + e.getMessage());
        }
    }
    /**
     * 7. 通用导出接口
     * @param tableName 表名
     * @param payload 包含: ids(选中ID), query(搜索条件), columns(导出列配置)
     */
    @PostMapping("/{tableName}/export")
    public void export(
            @PathVariable String tableName,
            @RequestBody Map<String, Object> payload,
            HttpServletResponse response
    ) {
        try {
            crudService.exportData(tableName, payload, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }
}