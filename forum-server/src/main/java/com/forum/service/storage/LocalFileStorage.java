package com.forum.service.storage;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 本地文件存储实现（M3-T5）
 *
 * 后续替换为 OSS 时只需新增 OssFileStorage 并切换 @Primary。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Slf4j
@Component
public class LocalFileStorage implements FileStorage {

    @Value("${forum.upload.path:./uploads}")
    private String uploadPath;

    @Value("${forum.upload.url-prefix:/static/uploads}")
    private String urlPrefix;

    @Override
    public String save(MultipartFile file, String subDir) {
        String ext = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = Paths.get(uploadPath, subDir);
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            file.transferTo(target);
        } catch (IOException e) {
            log.error("文件存储失败", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }
        return urlPrefix + "/" + subDir + "/" + fileName;
    }

    @Override
    public void delete(String url) {
        if (url == null || !url.startsWith(urlPrefix)) {
            return;
        }
        String relative = url.substring(urlPrefix.length()).replaceFirst("^/", "");
        Path target = Paths.get(uploadPath, relative);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("删除文件失败: {}", target, e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }
}
