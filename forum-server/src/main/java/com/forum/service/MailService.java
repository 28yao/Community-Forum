package com.forum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务
 *
 * C7 决策：使用 QQ 邮箱 SMTP。
 * 配置项 spring.mail.* 与发件人 forum.mail.from 在 application-dev.yml。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${forum.mail.from}")
    private String from;

    @Value("${forum.mail.verify-link-base}")
    private String verifyLinkBase;

    /**
     * 发送邮箱验证邮件
     *
     * @param to    收件人邮箱
     * @param nickname 用户昵称（个性化称呼）
     * @param token 验证令牌（拼接到验证链接）
     */
    public void sendVerificationMail(String to, String nickname, String token) {
        String link = verifyLinkBase + "?token=" + token;
        String subject = "【社区论坛】请验证您的邮箱";
        String body =
                "Hi " + nickname + "，\n\n" +
                "欢迎注册社区论坛系统！请点击下面的链接完成邮箱验证（24 小时内有效）：\n\n" +
                link + "\n\n" +
                "如果不是您本人操作，请忽略此邮件。\n\n" +
                "——社区论坛系统";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        mailSender.send(msg);
        log.info("[MAIL] verification mail sent to {} (token prefix={})", to,
                token.length() > 8 ? token.substring(0, 8) + "..." : token);
    }
}
