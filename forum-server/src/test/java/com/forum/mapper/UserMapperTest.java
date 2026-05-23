package com.forum.mapper;

import com.forum.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserMapper 集成测试
 *
 * 依赖：MySQL forum 库已建表，含 admin 种子数据。
 * 每个测试事务回滚，不污染数据。
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootTest
@ActiveProfiles("dev")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void selectById_seedAdmin_shouldExist() {
        User u = userMapper.selectById(1L);
        assertNotNull(u, "admin (id=1) should exist from seed.sql");
        assertEquals("admin@forum.com", u.getEmail());
        assertEquals("admin", u.getRole());
        assertEquals(1, u.getEmailVerified());
    }

    @Test
    void selectByEmail_seedAdmin_shouldFind() {
        User u = userMapper.selectByEmail("admin@forum.com");
        assertNotNull(u);
        assertEquals(1L, u.getId());
    }

    @Test
    void selectByEmail_nonExistent_shouldReturnNull() {
        assertNull(userMapper.selectByEmail("no-such@nowhere.com"));
    }

    @Test
    @Transactional
    @Rollback
    void insertAndDelete_shouldRoundtrip() {
        User u = new User();
        u.setEmail("t1-mapper-test@example.com");
        u.setPassword("$2a$10$dummy");
        u.setNickname("测试用户T2");
        u.setRole("user");
        u.setStatus(1);
        u.setEmailVerified(0);
        u.setLoginFailCount(0);
        // createdAt / updatedAt 由数据库 CURRENT_TIMESTAMP 填充

        int rows = userMapper.insert(u);
        assertEquals(1, rows);
        assertNotNull(u.getId());

        User loaded = userMapper.selectById(u.getId());
        assertNotNull(loaded);
        assertEquals("测试用户T2", loaded.getNickname());

        // 软删除：deleteById 实际是把 deleted 改为 1
        int deleted = userMapper.deleteById(u.getId());
        assertEquals(1, deleted);

        // 软删除后默认查询应该查不到
        User afterDelete = userMapper.selectById(u.getId());
        assertNull(afterDelete, "soft-deleted user should not be visible to default queries");
    }

    @Test
    @Transactional
    @Rollback
    void incrLoginFailCount_shouldIncrement() {
        // 用 admin 行做测试（事务回滚不会污染）
        User before = userMapper.selectById(1L);
        Integer beforeCount = before.getLoginFailCount();

        int rows = userMapper.incrLoginFailCount(1L);
        assertEquals(1, rows);

        User after = userMapper.selectById(1L);
        assertEquals(beforeCount + 1, after.getLoginFailCount());
    }
}
