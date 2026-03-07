package com.crud.fastcrud.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUiType() {
        return uiType;
    }

    public void setUiType(String uiType) {
        this.uiType = uiType;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public Integer getIsShowInList() {
        return isShowInList;
    }

    public void setIsShowInList(Integer isShowInList) {
        this.isShowInList = isShowInList;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}