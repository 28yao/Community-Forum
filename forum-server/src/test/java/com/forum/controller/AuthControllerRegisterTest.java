package com.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forum.entity.User;
import com.forum.mapper.UserMapper;
import com.forum.service.MailService;
import com.forum.service.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController.register 接口测试
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthControllerRegisterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserMapper userMapper;

    @MockBean
    private MailService mailService;

    private String json(String email, String password, String nickname) throws Exception {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword(password);
        r.setNickname(nickname);
        return mapper.writeValueAsString(r);
    }

    @Test
    @Transactional
    @Rollback
    void register_happyPath_shouldReturn0() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("t6-ok@example.com", "abc123", "T6测试用户")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("注册成功，验证邮件已发送"));

        User u = userMapper.selectByEmail("t6-ok@example.com");
        assertNotNull(u);
        verify(mailService, atLeastOnce()).sendVerificationMail(eq("t6-ok@example.com"), anyString(), anyString());
    }

    @Test
    void register_invalidEmail_shouldReturn2001() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("not-an-email", "abc123", "T6")))
                .andExpect(jsonPath("$.code").value(2001));
    }

    @Test
    void register_weakPassword_shouldReturn2001() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("t6-weak@example.com", "abcdef", "T6Weak")))
                .andExpect(jsonPath("$.code").value(2001));
    }

    @Test
    void register_shortNickname_shouldReturn2001() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("t6-short@example.com", "abc123", "x")))
                .andExpect(jsonPath("$.code").value(2001));
    }
}
