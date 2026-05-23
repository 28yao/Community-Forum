package com.forum.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;

/**
 * WebMvc 配置
 *
 * - 跨域：允许前端 5173 域访问全部 /api/** 与 /static/uploads/**
 * - 静态资源：把 forum.upload.path 配置目录映射为 forum.upload.access-prefix，
 *             用于上传图片的 HTTP 访问。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${forum.upload.path:./uploads}")
    private String uploadPath;

    @Value("${forum.upload.access-prefix:/static/uploads/**}")
    private String accessPrefix;

    /** 启动时确保上传目录存在 */
    @PostConstruct
    public void ensureUploadDir() {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            log.info("[UPLOAD] create dir {} -> {}", dir.getAbsolutePath(), created);
        } else {
            log.info("[UPLOAD] dir exists: {}", dir.getAbsolutePath());
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // accessPrefix 形如 "/static/uploads/**"，注册时去掉末尾通配符
        String pattern = accessPrefix.endsWith("/**") ? accessPrefix : accessPrefix + "/**";
        String location = "file:" + Paths.get(uploadPath).toAbsolutePath().normalize() + File.separator;
        registry.addResourceHandler(pattern).addResourceLocations(location);
        log.info("[STATIC] {} -> {}", pattern, location);
    }
}
