package com.forum.common.exception;

import com.forum.common.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GlobalExceptionHandler 单元测试
 *
 * 通过临时 controller 触发各种异常，验证响应被统一封装。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestCtrlConfig {
        @Bean
        TestExceptionController testExceptionController() {
            return new TestExceptionController();
        }
    }

    @RestController
    static class TestExceptionController {
        @GetMapping("/__test/biz-with-enum")
        public String bizEnum() {
            throw new BizException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        @GetMapping("/__test/biz-with-custom-msg")
        public String bizCustom() {
            throw new BizException(ErrorCode.NICKNAME_COOLDOWN, "下次可修改时间：2026-06-22");
        }

        @GetMapping("/__test/runtime")
        public String runtime() {
            throw new RuntimeException("boom");
        }
    }

    @Test
    void bizException_shouldReturnUnifiedJson() throws Exception {
        mockMvc.perform(get("/__test/biz-with-enum"))
                .andExpect(status().isOk())               // 业务错误 HTTP 仍 200
                .andExpect(jsonPath("$.code").value(2002))
                .andExpect(jsonPath("$.message").value("该邮箱已注册，请直接登录"));
    }

    @Test
    void bizException_withCustomMessage_shouldOverride() throws Exception {
        mockMvc.perform(get("/__test/biz-with-custom-msg"))
                .andExpect(jsonPath("$.code").value(2004))
                .andExpect(jsonPath("$.message").value(containsString("2026-06-22")));
    }

    @Test
    void unknownException_shouldFallbackTo9999() throws Exception {
        mockMvc.perform(get("/__test/runtime"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code").value(9999))
                .andExpect(jsonPath("$.message").value("系统异常"));
    }
}
