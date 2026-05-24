package com.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 社区论坛系统后端启动类
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@SpringBootApplication
@EnableAsync
@MapperScan("com.forum.mapper")
public class ForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumApplication.class, args);
    }
}
