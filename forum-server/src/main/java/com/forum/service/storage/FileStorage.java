package com.forum.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储抽象（C1 决策：本地 + OSS 接口预留）
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
public interface FileStorage {

    /**
     * 保存文件并返回可访问的 URL
     *
     * @param file   原始上传文件
     * @param subDir 子目录（如 avatar / post）
     * @return 可访问的 URL（如 /static/uploads/post/xxx.jpg）
     */
    String save(MultipartFile file, String subDir);

    /**
     * 删除文件（按 URL）
     */
    void delete(String url);
}
