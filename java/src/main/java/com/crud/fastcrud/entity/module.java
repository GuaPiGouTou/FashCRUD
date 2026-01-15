package com.crud.fastcrud.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 系统模块/数据表配置表
 * </p>
 *
 * @description 对应数据库表 sys_module
 */
@Data
@Accessors(chain = true) // 开启链式调用 set 方法
@TableName("sys_module")
public class module implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 显示名称 (如: 客户管理)
     */
    @TableField("module_name")
    private String moduleName;

    /**
     * 数据库真实表名 (如: tb_customer)
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 图标名称 (对应 ElementPlus 图标)
     */
    @TableField("icon")
    private String icon;

    /**
     * 图标背景色/主题色
     */
    @TableField("theme_color")
    private String themeColor;

    /**
     * 数据行数缓存
     */
    @TableField("record_count")
    private Integer recordCount;

    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 模块描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     * pattern: 指定前端接收到的 JSON 时间格式
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    /**
     * 软删除标记 (0:正常 1:删除)
     * @TableLogic: MyBatis-Plus 自动处理逻辑删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}