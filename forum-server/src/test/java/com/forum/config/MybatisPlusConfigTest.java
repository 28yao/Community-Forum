package com.forum.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MybatisPlusConfig 加载性测试
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootTest
@ActiveProfiles("dev")
class MybatisPlusConfigTest {

    @Autowired
    private MybatisPlusInterceptor interceptor;

    @Autowired
    private MetaObjectHandler metaObjectHandler;

    @Test
    void interceptor_shouldBeRegistered() {
        assertNotNull(interceptor);
        assertFalse(interceptor.getInterceptors().isEmpty(), "should contain pagination inner interceptor");
    }

    @Test
    void metaObjectHandler_shouldBeRegistered() {
        assertNotNull(metaObjectHandler);
    }
}
