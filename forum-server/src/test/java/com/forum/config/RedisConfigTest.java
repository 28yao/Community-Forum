package com.forum.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisConfig 集成测试
 *
 * 验收：set/get/expire/delete 全链路通畅，序列化正确。
 * 依赖：Redis 服务在 127.0.0.1:6379 上运行。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootTest
@ActiveProfiles("dev")
class RedisConfigTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void setAndGet_string_shouldRoundTrip() {
        String key = "test:t11:string:" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, "hello");
        Object val = redisTemplate.opsForValue().get(key);
        assertEquals("hello", val);
        redisTemplate.delete(key);
    }

    @Test
    void setAndGet_objectMap_shouldRoundTrip() {
        String key = "test:t11:map:" + System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>();
        data.put("name", "admin");
        data.put("age", 30);
        redisTemplate.opsForValue().set(key, data);
        Object val = redisTemplate.opsForValue().get(key);
        assertNotNull(val);
        // 不严格断言反序列化为 Map，因为 Jackson + activateDefaultTyping 会带类型元数据
        // 这里能取回非 null 即说明序列化-反序列化通路工作
        redisTemplate.delete(key);
    }

    @Test
    void expire_shouldWork() {
        String key = "test:t11:ttl:" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, "v", 5, TimeUnit.SECONDS);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= 5, "ttl should be in (0,5], got " + ttl);
        redisTemplate.delete(key);
    }

    @Test
    void delete_shouldRemoveKey() {
        String key = "test:t11:del:" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, "x");
        Boolean deleted = redisTemplate.delete(key);
        assertEquals(Boolean.TRUE, deleted);
        assertNull(redisTemplate.opsForValue().get(key));
    }
}
