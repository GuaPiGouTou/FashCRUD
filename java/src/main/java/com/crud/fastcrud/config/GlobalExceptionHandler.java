package com.crud.fastcrud.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常拦截器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 拦截：未登录异常
    @ExceptionHandler(NotLoginException.class)
    public JsonResult<String> handlerNotLoginException(NotLoginException e) {
        // 打印详细日志到控制台，方便后端排查
        e.printStackTrace();

        // 提取 Sa-Token 提供的详细未登录场景信息（比如是被顶下线、还是 Token 过期）
        String message = e.getMessage();

        // 优雅地返回 401 状态码和提示信息，而不是冷冰冰的 500
        return new JsonResult<>(401, null, "认证失败：" + message);
    }

    // 2. 拦截：缺少角色异常
    @ExceptionHandler(NotRoleException.class)
    public JsonResult<String> handlerNotRoleException(NotRoleException e) {
        e.printStackTrace();
        return new JsonResult<>(403, null, "无权访问：缺少对应的角色 [" + e.getRole() + "]");
    }

    // 3. 拦截：缺少权限异常
    @ExceptionHandler(NotPermissionException.class)
    public JsonResult<String> handlerNotPermissionException(NotPermissionException e) {
        e.printStackTrace();
        return new JsonResult<>(403, null, "无权访问：缺少对应的权限 [" + e.getCode() + "]");
    }

    // 4. 拦截：其他所有未知的异常（兜底防护）
    @ExceptionHandler(Exception.class)
    public JsonResult<String> handlerException(Exception e) {
        e.printStackTrace();
        return new JsonResult<>(500, null, "服务器开小差了：" + e.getMessage());
    }
}
