package com.jewelry.controller;

import com.jewelry.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    // 上传根目录：resources/static/images/
    private static final String UPLOAD_ROOT = System.getProperty("user.dir") +
            "/src/main/resources/static/images/";

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public R uploadAvatar(@RequestParam("file") MultipartFile file) {
        return upload(file, "avatars");
    }

    /**
     * 上传商品图片
     */
    @PostMapping("/product")
    public R uploadProduct(@RequestParam("file") MultipartFile file) {
        return upload(file, "products");
    }

    /**
     * 通用文件上传
     */
    private R upload(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            return R.error("文件不能为空");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return R.error("只允许上传图片文件");
        }

        // 校验文件大小 (最大 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return R.error("图片大小不能超过 5MB");
        }

        try {
            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String newFileName = subDir + "/" +
                    new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
                    "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

            // 保存文件
            Path uploadPath = Paths.get(UPLOAD_ROOT, newFileName);
            Files.createDirectories(uploadPath.getParent());
            file.transferTo(uploadPath.toFile());

            // 返回访问路径
            return R.ok("上传成功").put("data", newFileName);
        } catch (IOException e) {
            return R.error("上传失败: " + e.getMessage());
        }
    }
}
