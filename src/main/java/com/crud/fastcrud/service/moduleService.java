package com.crud.fastcrud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crud.fastcrud.entity.module;

import java.util.List;

public interface moduleService extends IService<module> {

    boolean deleteModule(Long id);
}
