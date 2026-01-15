package com.crud.fastcrud.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crud.fastcrud.config.JsonResult;
import com.crud.fastcrud.entity.module;
import com.crud.fastcrud.mapper.moduleMapper;
import com.crud.fastcrud.service.Impl.moduleServiceImpl;
import com.crud.fastcrud.service.moduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/module")
public class moduleController {
    @Autowired
    private moduleServiceImpl moduleServiceimpl;
    @Autowired
    private moduleMapper moduleMapper;
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
}
