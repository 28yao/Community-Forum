package com.forum.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forum.common.ErrorCode;
import com.forum.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 后台权限拦截器（M6-T1）
 *
 * 拦截 /api/admin/**；要求 role=admin。
 * AuthInterceptor 在前已经把 userId/role 注入 request；这里只校验 role。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        Object role = request.getAttribute("role");
        if (role == null) {
            writeError(response, ErrorCode.UNAUTHORIZED, "请先登录");
            return false;
        }
        if (!"admin".equals(role)) {
            writeError(response, ErrorCode.FORBIDDEN, "需要管理员权限");
            return false;
        }
        return true;
    }

    private void writeError(HttpServletResponse response, ErrorCode err, String msg) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> r = Result.fail(err, msg);
        response.getWriter().write(objectMapper.writeValueAsString(r));
    }
}
