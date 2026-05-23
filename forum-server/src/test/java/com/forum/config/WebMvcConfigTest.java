package com.forum.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WebMvcConfig 集成测试
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class WebMvcConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadDir_shouldExistAfterStartup() {
        File dir = new File("./uploads");
        assertTrue(dir.exists() && dir.isDirectory(), "upload dir should be created");
    }

    @Test
    void cors_preflightFromLocalhost5173_shouldBeAllowed() throws Exception {
        mockMvc.perform(options("/api/boards")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}
