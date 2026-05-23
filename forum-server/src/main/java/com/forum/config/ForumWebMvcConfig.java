package com.forum.config;

import com.forum.interceptor.AdminInterceptor;
import com.forum.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 AuthInterceptor（M1-T14）+ AdminInterceptor（M6-T2）
 *
 * 拦截 /api/** 中需要登录的路径，排除公开接口。
 * /api/admin/** 在 AuthInterceptor 之后再过 AdminInterceptor 检查 role=admin。
 */
@Configuration
@RequiredArgsConstructor
public class ForumWebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .order(1)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/register",
                        "/api/auth/verify-email",
                        "/api/auth/resend-verification",
                        "/api/auth/login",
                        "/api/admin/auth/login",  // 后台登录公开
                        "/api/boards/**"          // 版块列表公开（M2）
                );

        // 后台路径必须 role=admin
        registry.addInterceptor(adminInterceptor)
                .order(2)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");
    }
}
