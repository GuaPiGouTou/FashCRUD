package com.crud.fastcrud.controller;

import com.crud.fastcrud.config.JsonResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    // 文件存储路径
    private final Path UPLOAD_PATH = Paths.get(System.getProperty("user.dir"), "uploads");

    /**
     * 上传接口 (保持不变)
     */
    @PostMapping("/upload")
    public JsonResult<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return new JsonResult<>(500, null, "文件为空");
        try {
            if (!UPLOAD_PATH.toFile().exists()) UPLOAD_PATH.toFile().mkdirs();

            String originalName = file.getOriginalFilename();
            String suffix = "";
            if (originalName != null && originalName.contains(".")) {
                suffix = originalName.substring(originalName.lastIndexOf("."));
            }
            String newName = UUID.randomUUID().toString() + suffix;

            file.transferTo(UPLOAD_PATH.resolve(newName).toFile());

            return new JsonResult<>(200, newName, "上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return new JsonResult<>(500, null, "上传失败: " + e.getMessage());
        }
    }

    /**
     * [修复] 预览接口：使用 HttpServletResponse 流式输出
     */
    @GetMapping("/view")
    public void view(@RequestParam String fileName, HttpServletResponse response) {
        sendFile(fileName, response, false);
    }

    /**
     * [修复] 下载接口：使用 HttpServletResponse 流式输出
     */
    @GetMapping("/download")
    public void download(@RequestParam String fileName, HttpServletResponse response) {
        sendFile(fileName, response, true);
    }

    /**
     * 核心私有方法：将文件写入响应流
     * @param fileName 文件名
     * @param response 响应对象
     * @param isDownload 是否强制下载
     */
    private void sendFile(String fileName, HttpServletResponse response, boolean isDownload) {
        File file = UPLOAD_PATH.resolve(fileName).toFile();
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 1. 设置响应头
        response.reset();
        response.setContentLength((int) file.length());

        try {
            if (isDownload) {
                // 强制下载
                String encodedName = URLEncoder.encode(fileName, "UTF-8");
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedName + "\"");
            } else {
                // 在线预览 (根据后缀设置 Content-Type)
                String name = fileName.toLowerCase();
                if (name.endsWith(".jpg") || name.endsWith(".jpeg")) response.setContentType("image/jpeg");
                else if (name.endsWith(".png")) response.setContentType("image/png");
                else if (name.endsWith(".gif")) response.setContentType("image/gif");
                else if (name.endsWith(".pdf")) response.setContentType("application/pdf");
                else response.setContentType("application/octet-stream"); // 默认
            }

            // 2. 读取文件并写入响应流 (最底层的 IO 操作)
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                 OutputStream os = response.getOutputStream()) {

                byte[] buffer = new byte[1024 * 8]; // 8KB 缓冲区
                int i;
                while ((i = bis.read(buffer)) != -1) {
                    os.write(buffer, 0, i);
                }
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}