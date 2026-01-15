package com.crud.fastcrud.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crud.fastcrud.entity.UiTable;
import com.crud.fastcrud.mapper.UiTableMapper;
import com.crud.fastcrud.service.UiTableService;
import org.springframework.stereotype.Service;

@Service
public class UiTableServiceImpl extends ServiceImpl<UiTableMapper, UiTable> implements UiTableService {
}
