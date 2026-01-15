package com.crud.fastcrud.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_field_config")
public class SysFieldConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("table_name")
    private String tableName;

    private String prop;     // 字段名 (英文)
    private String label;    // 显示名 (中文)

    @TableField("ui_type")
    private String uiType;   // 组件类型 (Email, Rating...)

    private String options;  // 下拉选项

    @TableField("is_show_in_list")
    private Integer isShowInList; // 1显示 0隐藏

    @TableField("sort_order")
    private Integer sortOrder;
}