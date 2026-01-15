package com.crud.fastcrud.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crud.fastcrud.entity.module;
import com.crud.fastcrud.mapper.moduleMapper;
import com.crud.fastcrud.service.moduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class moduleServiceImpl extends ServiceImpl<moduleMapper, module> implements moduleService {
    @Autowired
    private moduleMapper moduleMapper;
    /**
     * 获取列表
     */
    public List<module> selectModuleList(String keyword) {
        QueryWrapper<module> wrapper = new QueryWrapper<>();

        // 1. 基础排序：按更新时间倒序
        wrapper.orderByDesc("updated_at");

        // 2. 搜索逻辑：如果 keyword 不为空，则拼接查询条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 生成 SQL: AND (module_name LIKE '%keyword%' OR table_name LIKE '%keyword%')
            wrapper.and(w -> w
                    .like("module_name", keyword)
                    .or()
                    .like("table_name", keyword)
            );
        }

        return this.list(wrapper);
    }

    /**
     * 接口2: 获取仪表盘统计数据
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. 总表数
        long totalTables = this.count();

        // 2. 总记录数 (调用Mapper自定义SQL)
        long totalRecords = moduleMapper.sumTotalRecords();

        // 3. 本周新增 (查询 created_at > 本周一)
        LocalDateTime startOfWeek = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        long newThisWeek = this.count(new QueryWrapper<module>().ge("created_at", startOfWeek));

        // 4. Top 5 数据量排行
        List<module> topModules = this.list(new QueryWrapper<module>()
                .orderByDesc("record_count")
                .last("LIMIT 5"));

        stats.put("totalTables", totalTables);
        stats.put("totalRecords", totalRecords);
        stats.put("newThisWeek", newThisWeek);
        stats.put("storageUsed", "10.5 MB"); // 模拟数据
        stats.put("topModules", topModules);

        return stats;
    }

    /**
     * 接口3: 创建新模块 (包含物理建表)
     */
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public boolean createModule(module mod) {
        // 1. 校验表名是否存在
        long count = this.count(new QueryWrapper<module>().eq("table_name", mod.getTableName()));
        if (count > 0) {
            throw new RuntimeException("表名已存在: " + mod.getTableName());
        }

        // 2. 插入元数据到 sys_module
        mod.setRecordCount(0); // 初始记录数为0
        boolean saveSuccess = this.save(mod);

        // 3. 执行物理建表 SQL
        if (saveSuccess) {
            moduleMapper.createPhysicalTable(mod.getTableName());
        }
        return saveSuccess;
    }

    /**
     * 接口4: 删除模块 (包含物理删表)
     */
    // [修改] 普通删除 -> 变为软删除 (移入回收站)
    @Override
    public boolean deleteModule(Long id) {
        // MyBatis-Plus 配置了 @TableLogic，调用 removeById 会自动 update is_deleted = 1
        return this.removeById(id);
    }
    /**
     * 接口5: 修改模块信息
     * 注意：通常不建议修改 table_name，因为涉及物理表重命名，风险较大。
     * 这里只允许修改 module_name, icon, theme_color, description
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateModule(module mod) {
        if (mod.getId() == null) {
            throw new RuntimeException("ID不能为空");
        }

        // 1. 查出旧数据
        module oldMod = this.getById(mod.getId());
        if (oldMod == null) return false;

        // 2. 强制设回原来的表名，防止物理表名被篡改
        mod.setTableName(oldMod.getTableName());

        // 3. 更新元数据
        return this.updateById(mod);
    }
    // 在 moduleServiceImpl 中



    // [新增] 获取回收站列表
    public List<module> getRecycleBinList() {
        return moduleMapper.selectRecycleBin();
    }

    // [新增] 恢复模块
    public void recoverModule(Long id) {
        moduleMapper.recoverModule(id);
    }

    // [新增] 彻底删除 (物理删除)
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteModule(Long id) {
        // 1. 我们需要先查出表名。因为 MP 默认不查已删除数据，这里我们要手写 SQL 或者用特定方法
        // 简单起见，我们先恢复它再查，或者直接用 mapper 查（建议用 mapper 查）
        // 这里为了演示简单，假设我们能获取到表名。
        // 实际项目中，建议在 Mapper 加一个 selectDeletedById

        // 这里采用一种取巧方式：先查回收站列表找到它
        List<module> deletedList = moduleMapper.selectRecycleBin();
        module mod = deletedList.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);

        if (mod != null) {
            // 1. 物理删表
            moduleMapper.dropPhysicalTable(mod.getTableName());
            // 2. 物理删记录 (MyBatis-Plus 没提供直接物理删的方法，需手写 SQL)
            // 可以在 Mapper 加一个 @Delete("DELETE FROM sys_module WHERE id = #{id}")
            moduleMapper.deleteReal(id); // 需在 Mapper 补充这个方法
        }
    }

}
