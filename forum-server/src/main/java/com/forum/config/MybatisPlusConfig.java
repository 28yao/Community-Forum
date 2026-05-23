package com.forum.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置
 *
 * 包含：
 *  - 分页插件（MySQL 方言）
 *  - 元数据填充器（创建/更新时间兜底；DB 层已配 CURRENT_TIMESTAMP，应用层填充作为防御）
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件（plan.md §4.1 分页参数 ?page=1&size=20）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor page = new PaginationInnerInterceptor(DbType.MYSQL);
        page.setMaxLimit(100L);              // 单页最大 100 条，防止恶意大分页
        page.setOverflow(false);             // 越界不自动归零
        interceptor.addInnerInterceptor(page);
        return interceptor;
    }

    /**
     * 自动填充处理器
     *
     * 仅当 entity 字段标注 @TableField(fill = INSERT/UPDATE) 才生效。
     * 当前 schema 中 created_at/updated_at 已由数据库 CURRENT_TIMESTAMP 处理，
     * 这里作为防御性兜底，不强制要求 entity 标注。
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
                strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
