package com.crud.fastcrud.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
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

}
