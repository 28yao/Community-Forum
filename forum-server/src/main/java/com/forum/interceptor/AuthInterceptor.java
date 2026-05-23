package com.forum.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forum.common.ErrorCode;
import com.forum.common.JwtUtil;
import com.forum.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

/**
 * 认证拦截器（M1-T13）
 *
 * 从 Authorization: Bearer xxx 中解析 Token，校验后将 userId/role 写入 request attribute。
 * 未通过直接返回 1001 JSON。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /** GET /api/users/{id} 是公开接口，路径中的 id 为纯数字 */
    private static final Pattern PUBLIC_USER_GET = Pattern.compile("^/api/users/\\d+$");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // GET /api/users/{id} 公开访问
        if ("GET".equalsIgnoreCase(request.getMethod())
                && PUBLIC_USER_GET.matcher(request.getRequestURI()).matches()) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeError(response, ErrorCode.UNAUTHORIZED, "未登录，请先登录");
            return false;
        }

        String token = header.substring(7);
        if (!jwtUtil.isValid(token)) {
            writeError(response, ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
            return false;
        }

        Long userId = jwtUtil.parseUserId(token);
        String role = jwtUtil.parseRole(token);
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);
        return true;
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode, String msg) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.fail(errorCode, msg);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
