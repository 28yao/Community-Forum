package com.forum.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具
 *
 * - 签名算法：HS256
 * - secret 来源：forum.jwt.secret（≥ 32 字节）
 * - 有效期：forum.jwt.expire-days
 *
 * 业务流程：登录成功后用 generateToken(userId, role) 签发，
 * 客户端在后续请求 Authorization 头携带，AuthInterceptor 调 parseUserId() 校验。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expireMillis;

    public JwtUtil(@Value("${forum.jwt.secret}") String secret,
                   @Value("${forum.jwt.expire-days:7}") int expireDays) {
        // HS256 要求 secret ≥ 32 字节
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(bytes);
        this.expireMillis = expireDays * 24L * 3600L * 1000L;
    }

    /** 签发 token；userId 放 subject，role 放 claim */
    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireMillis))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析并校验 token，返回 userId；过期/签名错都抛 JwtException。
     */
    public Long parseUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String parseRole(String token) {
        Object role = parseClaims(token).get("role");
        return role == null ? null : role.toString();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** 静默校验：true=有效；false=无效（含过期/签名错） */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
