package com.crud.fastcrud.service.Impl;

import cn.dev33.satoken.stp.StpInterface;
import com.crud.fastcrud.entity.SysPermission;
import com.crud.fastcrud.entity.SysRole;
import com.crud.fastcrud.mapper.SysPermissionMapper;
import com.crud.fastcrud.mapper.SysRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
public class StpInterfaceImpl implements StpInterface {
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysPermissionMapper sysPermissionMapper;
    @Override
    public List<String> getPermissionList(Object o, String s) {
        return  sysPermissionMapper.selectByUserId(Long.valueOf(o.toString()))
                .stream()
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toList());

    }

    @Override
    public List<String> getRoleList(Object o, String s) {
        return  sysRoleMapper.selectByUserId(Long.valueOf(o.toString())).stream().map(SysRole::getRoleCode).collect(
                Collectors.toList()
        );
    }
}
