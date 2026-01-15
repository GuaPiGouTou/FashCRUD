package com.crud.fastcrud;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crud.fastcrud.entity.UiTable;
import com.crud.fastcrud.entity.module;
import com.crud.fastcrud.mapper.UiTableMapper;
import com.crud.fastcrud.service.Impl.moduleServiceImpl;
import com.crud.fastcrud.service.UiTableService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
public class UiTableTest {
    @Autowired
    private UiTableMapper uiTableMapper;
    @Autowired
    private UiTableService uiTableService;
    @Autowired
    private moduleServiceImpl moduleServiceimpl;
    @Test
    void testInsertUiTable() {
        UiTable uiTable = new UiTable();
        uiTable.setUiId(1L);
        uiTable.setUiName("测试");
        uiTable.setTableName("测试");
        uiTable.setUiDescription("测试");
        uiTable.setCreateType("测试");
        uiTable.setExcelfileName("测试");
        uiTable.setExcelfilePath("测试");
        uiTable.setUiStatus(1);
        uiTable.setCreatedBy("测试");
        uiTable.setUpdatedBy("测试");
        uiTable.setCreateTime(new Date());
        uiTable.setUpdateTime(new Date());
        uiTableMapper.insert(uiTable);
    }
    @Test
    void SaveBathInsertUiTable() {
        List<UiTable> uiTables =new ArrayList<>();
        for (int i = 0; i <10; i++) {
            UiTable uiTable = new UiTable();
            uiTable.setUiId(1L);
            uiTable.setUiName("测试");
            uiTable.setTableName("测试");
            uiTable.setUiDescription("测试");
            uiTable.setCreateType("测试");
            uiTable.setExcelfileName("测试");
            uiTable.setExcelfilePath("测试");
            uiTable.setUiStatus(1);
            uiTable.setCreatedBy("测试");
            uiTable.setUpdatedBy("测试");
            uiTable.setCreateTime(new Date());
            uiTable.setUpdateTime(new Date());
            uiTables.add(uiTable);
        }

        boolean result = uiTableService.saveBatch(uiTables);
        System.out.println(result);

    }
    @Test
    void deleteUiTable() {
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("ui_id", 1);
        boolean result = uiTableService.removeByMap(columnMap);
        System.out.println(result);

    }
    @Test
    void LambdaWrapperUiTable() {
        LambdaQueryWrapper<UiTable> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UiTable::getUiId, 1).eq(UiTable::getUiStatus,1);
        uiTableMapper.selectList(queryWrapper);

    }
    @Test
    void TestPage() {
        Page<UiTable> page = new Page<>(2, 10);
        LambdaQueryWrapper<UiTable> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UiTable::getUiId, 1);
        uiTableMapper.selectPage(page, queryWrapper);
    }
    @Test
    void cr() {
        module mod = new module();
        mod.setModuleName("测试");
        mod.setTableName("测试");
        mod.setIcon("测试");
        mod.setThemeColor("测试");
        mod.setRecordCount(1);
        mod.setSortOrder(1);
        mod.setDescription("测试");

        moduleServiceimpl.createModule(mod);
    }

}
