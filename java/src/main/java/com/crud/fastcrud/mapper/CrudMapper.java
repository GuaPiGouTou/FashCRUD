package com.crud.fastcrud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crud.fastcrud.entity.SysFieldConfig;
import org.apache.ibatis.annotations.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface CrudMapper extends BaseMapper<SysFieldConfig> {


    /**
     * 2. 动态插入
     */
    @Insert("INSERT INTO ${tableName} (${keys}) VALUES (${values})")
    void insertDynamic(@Param("tableName") String tableName,
                       @Param("keys") String keys,
                       @Param("values") String values);

    /**
     * 3. 动态更新
     */
    @Update("UPDATE ${tableName} SET ${setSql} WHERE id = #{id}")
    void updateDynamic(@Param("tableName") String tableName,
                       @Param("id") Long id,
                       @Param("setSql") String setSql);

    /**
     * 4. 动态删除
     */
    @Delete("DELETE FROM ${tableName} WHERE id = #{id}")
    void deleteDynamic(@Param("tableName") String tableName, @Param("id") Long id);

    /**
     * 5. 动态加列 (DDL)
     */
    @Update("ALTER TABLE ${tableName} ADD COLUMN ${columnName} ${columnType}")
    void addColumn(@Param("tableName") String tableName,
                   @Param("columnName") String columnName,
                   @Param("columnType") String columnType);

    /**
     * 6. 修改列结构 (DDL)
     * MySQL语法: ALTER TABLE table_name CHANGE old_col_name new_col_name new_type
     */
    @Update("ALTER TABLE ${tableName} CHANGE COLUMN ${oldName} ${newName} ${columnType}")
    void modifyColumn(@Param("tableName") String tableName,
                      @Param("oldName") String oldName,
                      @Param("newName") String newName,
                      @Param("columnType") String columnType);

    // 1. 查询数据列表 (增加分页 LIMIT)
    @Select("<script>" +
            "SELECT * FROM ${tableName} " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\" and queryColumns != null and queryColumns.size() > 0'>" +
            "    AND (" +
            "      <foreach collection='queryColumns' item='col' separator=' OR '>" +
            "        ${col} LIKE CONCAT('%', #{keyword}, '%')" +
            "      </foreach>" +
            "    )" +
            "  </if>" +
            "  <if test='filters != null and filters.size() > 0'>" +
            "    <foreach collection='filters' item='item'>" +
            "       AND ${item.col} ${item.sqlOp} #{item.val}" +
            "    </foreach>" +
            "  </if>" +
            "</where>" +
            "<!-- 排序 -->" +
            "<if test='sortField != null and sortField != \"\"'>" +
            "  ORDER BY ${sortField} ${sortOrder}" +
            "</if>" +
            "<if test='sortField == null'>" +
            "  ORDER BY id DESC" +
            "</if>" +
            "<!-- 分页 -->" +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<LinkedHashMap<String, Object>> selectDynamicList(
            @Param("tableName") String tableName,
            @Param("keyword") String keyword,
            @Param("queryColumns") List<String> queryColumns,
            @Param("filters") List<Map<String, Object>> filters,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder,
            @Param("offset") int offset, // 新增
            @Param("limit") int limit    // 新增
    );

    // 2. 查询总条数 (用于分页计算)
    @Select("<script>" +
            "SELECT COUNT(*) FROM ${tableName} " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\" and queryColumns != null and queryColumns.size() > 0'>" +
            "    AND (" +
            "      <foreach collection='queryColumns' item='col' separator=' OR '>" +
            "        ${col} LIKE CONCAT('%', #{keyword}, '%')" +
            "      </foreach>" +
            "    )" +
            "  </if>" +
            "  <if test='filters != null and filters.size() > 0'>" +
            "    <foreach collection='filters' item='item'>" +
            "       AND ${item.col} ${item.sqlOp} #{item.val}" +
            "    </foreach>" +
            "  </if>" +
            "</where>" +
            "</script>")
    Long countDynamicList(
            @Param("tableName") String tableName,
            @Param("keyword") String keyword,
            @Param("queryColumns") List<String> queryColumns,
            @Param("filters") List<Map<String, Object>> filters
    );

    /*
    *
    * */
    @Select("<script>" +
            "SELECT * FROM ${tableName} WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<LinkedHashMap<String, Object>> selectDynamicByIds(@Param("tableName") String tableName, @Param("ids") List<Integer> ids);
}