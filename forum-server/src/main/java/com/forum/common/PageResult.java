package com.forum.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 分页响应数据
 *
 * 对应 plan.md §4.1：分页参数 ?page=1&size=20
 * 与 MyBatis-Plus IPage 适配，提供 of(IPage) 快速转换；
 * 同时支持转换 entity 列表为 VO 列表（map）。
 *
 * @param <T> 列表元素类型
 * @author liuxinsi
 * @date 2026-05-23
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private final long total;

    /** 当前页（1 起） */
    private final long page;

    /** 每页大小 */
    private final long size;

    /** 当前页数据 */
    private final List<T> list;

    private PageResult(long total, long page, long size, List<T> list) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.list = list == null ? Collections.emptyList() : list;
    }

    /** 直接构造 */
    public static <T> PageResult<T> of(long total, long page, long size, List<T> list) {
        return new PageResult<>(total, page, size, list);
    }

    /** 从 MyBatis-Plus IPage 转换 */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /** 从 IPage 转换并把每个 entity 映射为 VO */
    public static <E, V> PageResult<V> of(IPage<E> page, Function<E, V> mapper) {
        List<V> mapped = page.getRecords().stream().map(mapper).collect(java.util.stream.Collectors.toList());
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), mapped);
    }

    /** 空分页 */
    public static <T> PageResult<T> empty(long page, long size) {
        return new PageResult<>(0L, page, size, Collections.emptyList());
    }
}
