package com.forum.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单测（M1-T10）
 */
@SpringBootTest
@ActiveProfiles("dev")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateToken_and_parseUserId() {
        String token = jwtUtil.generateToken(42L, "user");
        assertNotNull(token);
        assertEquals(42L, jwtUtil.parseUserId(token));
    }

    @Test
    void parseRole_shouldReturnRole() {
        String token = jwtUtil.generateToken(1L, "admin");
        assertEquals("admin", jwtUtil.parseRole(token));
    }

    @Test
    void isValid_validToken_shouldReturnTrue() {
        String token = jwtUtil.generateToken(1L, "user");
        assertTrue(jwtUtil.isValid(token));
    }

    @Test
    void isValid_garbageToken_shouldReturnFalse() {
        assertFalse(jwtUtil.isValid("not.a.valid.jwt.token"));
    }

    @Test
    void parseClaims_shouldContainSubjectAndRole() {
        String token = jwtUtil.generateToken(99L, "admin");
        Claims claims = jwtUtil.parseClaims(token);
        assertEquals("99", claims.getSubject());
        assertEquals("admin", claims.get("role"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void parseUserId_invalidToken_shouldThrow() {
        assertThrows(JwtException.class, () -> jwtUtil.parseUserId("bad"));
    }
}
