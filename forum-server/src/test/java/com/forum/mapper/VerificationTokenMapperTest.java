package com.forum.mapper;

import com.forum.entity.VerificationToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VerificationTokenMapper 集成测试
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootTest
@ActiveProfiles("dev")
class VerificationTokenMapperTest {

    @Autowired
    private VerificationTokenMapper tokenMapper;

    @Test
    @Transactional
    @Rollback
    void insertAndSelectByToken_shouldRoundtrip() {
        VerificationToken t = new VerificationToken();
        t.setEmail("test@example.com");
        t.setToken("test-token-" + System.nanoTime());
        t.setType(VerificationToken.TYPE_REGISTER);
        t.setUsed(0);
        t.setExpiresAt(LocalDateTime.now().plusHours(24));

        int rows = tokenMapper.insert(t);
        assertEquals(1, rows);
        assertNotNull(t.getId());

        VerificationToken loaded = tokenMapper.selectByToken(t.getToken());
        assertNotNull(loaded);
        assertEquals(t.getId(), loaded.getId());
        assertEquals(VerificationToken.TYPE_REGISTER, loaded.getType());
        assertEquals(0, loaded.getUsed());
    }

    @Test
    void selectByToken_nonExistent_shouldReturnNull() {
        assertNull(tokenMapper.selectByToken("non-existent-token-xyz"));
    }
}
