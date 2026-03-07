package com.crud.fastcrud.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("ui_table")
public class UiTable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 界面id
     */
    private Long uiId;

    /**
     * 界面名称
     */
    private String uiName;

    /**
     * 界面数据库表名
     */
    private String tableName;

    /**
     * 界面描述
     */
    private String uiDescription;

    /**
     * 创建方式
     */
    private String createType;

    /**
     * excel文件名称
     */
    private String excelfileName;

    /**
     * excel文件路径
     */
    private String excelfilePath;

    /**
     * 状态：0-草稿 1-已启用 2-已停用
     */
    private Integer uiStatus;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getUiId() {
        return uiId;
    }

    public void setUiId(Long uiId) {
        this.uiId = uiId;
    }

    public String getUiName() {
        return uiName;
    }

    public void setUiName(String uiName) {
        this.uiName = uiName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getUiDescription() {
        return uiDescription;
    }

    public void setUiDescription(String uiDescription) {
        this.uiDescription = uiDescription;
    }

    public String getCreateType() {
        return createType;
    }

    public void setCreateType(String createType) {
        this.createType = createType;
    }

    public String getExcelfileName() {
        return excelfileName;
    }

    public void setExcelfileName(String excelfileName) {
        this.excelfileName = excelfileName;
    }

    public String getExcelfilePath() {
        return excelfilePath;
    }

    public void setExcelfilePath(String excelfilePath) {
        this.excelfilePath = excelfilePath;
    }

    public Integer getUiStatus() {
        return uiStatus;
    }

    public void setUiStatus(Integer uiStatus) {
        this.uiStatus = uiStatus;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
