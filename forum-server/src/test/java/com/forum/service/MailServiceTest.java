package com.forum.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MailService 单元测试
 *
 * 用 @MockBean 替换 JavaMailSender，验证 MailService 构造的邮件参数正确，
 * 而不会真的对外发邮件，避免污染 QQ 邮箱。
 *
 * 真实发送的手工验证请用 dev profile 启动应用后调用 /api/auth/register。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootTest
@ActiveProfiles("dev")
class MailServiceTest {

    @Autowired
    private MailService mailService;

    @MockBean
    private JavaMailSender mockMailSender;

    @Test
    void sendVerificationMail_shouldBuildExpectedMessage() {
        mailService.sendVerificationMail("user@example.com", "小明", "abcdef1234");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockMailSender, times(1)).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertArrayEquals(new String[]{"user@example.com"}, msg.getTo());
        assertTrue(msg.getSubject().contains("验证"));
        String body = msg.getText();
        assertNotNull(body);
        assertTrue(body.contains("小明"), "should personalize with nickname");
        assertTrue(body.contains("token=abcdef1234"), "should include verify link with token");
        assertTrue(body.contains("24 小时"), "should mention 24-hour expiry");
    }
}
