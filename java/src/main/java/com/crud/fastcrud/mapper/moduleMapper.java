package com.crud.fastcrud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crud.fastcrud.entity.module;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface moduleMapper extends BaseMapper<module>{
    // 1. 统计所有记录总数 (用于仪表盘)
    @Select("SELECT IFNULL(SUM(record_count), 0) FROM sys_module WHERE is_deleted = 0")
    Long sumTotalRecords();

    // 2. 物理创建表 (用于创建模块)
    // 注意：${tableName} 使用 $ 是为了直接拼接表名，会有注入风险，但在 Service 层我们会做校验
    @Update("CREATE TABLE ${tableName} (id BIGINT PRIMARY KEY AUTO_INCREMENT, created_time DATETIME DEFAULT CURRENT_TIMESTAMP, updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)")
    void createPhysicalTable(@Param("tableName") String tableName);

    // 3. 物理删除表 (用于删除模块)
    @Update("DROP TABLE IF EXISTS ${tableName}")
    void dropPhysicalTable(@Param("tableName") String tableName);

    // 4.查询回收站列表 (显式查询 is_deleted = 1)
    @Select("SELECT * FROM sys_module WHERE is_deleted = 1 ORDER BY updated_at DESC")
    List<module> selectRecycleBin();

    // 5.恢复模块 (显式将 is_deleted 更新为 0)
    @Update("UPDATE sys_module SET is_deleted = 0 WHERE id = #{id}")
    void recoverModule(@Param("id") Long id);
    // 6. 批量删除模块 (显式将 is_deleted 置为 1)
    @Delete("DELETE FROM sys_module WHERE id = #{id}") void deleteReal(@Param("id") Long id);
}
