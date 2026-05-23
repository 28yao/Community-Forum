package com.forum.config;

import com.forum.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 AuthInterceptor（M1-T14）
 *
 * 拦截 /api/** 中需要登录的路径，排除公开接口。
 */
@Configuration
@RequiredArgsConstructor
public class ForumWebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/register",
                        "/api/auth/verify-email",
                        "/api/auth/resend-verification",
                        "/api/auth/login",
                        "/api/boards/**"       // 版块列表公开（M2）
                );
    }
}
