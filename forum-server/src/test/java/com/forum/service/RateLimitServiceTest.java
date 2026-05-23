package com.forum.service;

import com.forum.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RateLimitService 测试（M7-T1）
 */
@SpringBootTest
@ActiveProfiles("dev")
class RateLimitServiceTest {

    @Autowired private RateLimitService rateLimitService;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    private static final long TEST_UID = 999999L;
    private static final String TEST_IP = "test-ip-127.0.0.1";

    @BeforeEach
    void cleanRedis() {
        redisTemplate.delete("rate:post:" + TEST_UID);
        redisTemplate.delete("rate:comment:" + TEST_UID);
        redisTemplate.delete("rate:search:" + TEST_IP);
    }

    @Test
    void postCreate_firstAllow_secondReject() {
        rateLimitService.checkPostCreate(TEST_UID);
        BizException ex = assertThrows(BizException.class,
                () -> rateLimitService.checkPostCreate(TEST_UID));
        assertTrue(ex.getMessage().contains("发帖过于频繁"));
    }

    @Test
    void commentCreate_firstAllow_secondReject() {
        rateLimitService.checkCommentCreate(TEST_UID);
        BizException ex = assertThrows(BizException.class,
                () -> rateLimitService.checkCommentCreate(TEST_UID));
        assertTrue(ex.getMessage().contains("评论过于频繁"));
    }

    @Test
    void search_withinLimit_shouldAllow() {
        // 默认 30 次/分钟；连续调 5 次都应通过
        for (int i = 0; i < 5; i++) {
            rateLimitService.checkSearch(TEST_IP);
        }
    }

    @Test
    void search_overLimit_shouldReject() {
        // 调超过 30 次
        for (int i = 0; i < 30; i++) {
            rateLimitService.checkSearch(TEST_IP);
        }
        BizException ex = assertThrows(BizException.class,
                () -> rateLimitService.checkSearch(TEST_IP));
        assertTrue(ex.getMessage().contains("搜索过于频繁"));
    }
}
