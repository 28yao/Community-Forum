package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务（M1-T17）
 *
 * 当前实现：本地存储。接口预留，后续可替换为 OSS。
 */
@Slf4j
@Service
public class FileService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif");

    @Value("${forum.upload.path:./uploads}")
    private String uploadPath;

    @Value("${forum.upload.url-prefix:/static/uploads}")
    private String urlPrefix;

    @Value("${forum.upload.max-single-size:5242880}")
    private long maxSingleSize;

    /**
     * 上传头像图片
     *
     * @return 可访问的 URL（如 /static/uploads/avatar/xxx.jpg）
     */
    public String uploadAvatar(MultipartFile file) {
        validateFile(file);
        String subDir = "avatar";
        String url = store(file, subDir);
        log.info("[UPLOAD_AVATAR] saved {}", url);
        return url;
    }

    /**
     * 上传帖子图片
     */
    public String uploadPostImage(MultipartFile file) {
        validateFile(file);
        return store(file, "post");
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请选择要上传的文件");
        }
        if (file.getSize() > maxSingleSize) {
            throw new BizException(ErrorCode.UPLOAD_TOO_LARGE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BizException(ErrorCode.UPLOAD_TYPE_INVALID);
        }
    }

    private String store(MultipartFile file, String subDir) {
        String ext = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = Paths.get(uploadPath, subDir);
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            log.error("文件存储失败", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }
        return urlPrefix + "/" + subDir + "/" + fileName;
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }
}
