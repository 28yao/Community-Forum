package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import com.forum.service.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * 文件上传服务（M1-T17 / M3-T5）
 *
 * 校验由本类负责，落盘由 FileStorage 抽象实现（C1：本地 + OSS 预留）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif");

    private final FileStorage fileStorage;

    @Value("${forum.upload.max-single-size:5242880}")
    private long maxSingleSize;

    public String uploadAvatar(MultipartFile file) {
        validateImage(file);
        String url = fileStorage.save(file, "avatar");
        log.info("[UPLOAD_AVATAR] saved {}", url);
        return url;
    }

    public String uploadPostImage(MultipartFile file) {
        validateImage(file);
        String url = fileStorage.save(file, "post");
        log.info("[UPLOAD_POST_IMG] saved {}", url);
        return url;
    }

    public void delete(String url) {
        fileStorage.delete(url);
    }

    private void validateImage(MultipartFile file) {
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
}
