package com.forum.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageResult 单元测试
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
class PageResultTest {

    @Test
    void of_directConstruct_shouldHoldAllFields() {
        PageResult<String> p = PageResult.of(100L, 2L, 20L, Arrays.asList("a", "b"));
        assertEquals(100L, p.getTotal());
        assertEquals(2L, p.getPage());
        assertEquals(20L, p.getSize());
        assertEquals(2, p.getList().size());
    }

    @Test
    void empty_shouldHaveZeroTotal() {
        PageResult<String> p = PageResult.empty(1L, 20L);
        assertEquals(0L, p.getTotal());
        assertTrue(p.getList().isEmpty());
    }

    @Test
    void of_fromIPage_shouldCopyMeta() {
        IPage<String> ipage = new Page<>(3L, 10L, 99L);
        ipage.setRecords(Arrays.asList("x", "y", "z"));
        PageResult<String> p = PageResult.of(ipage);
        assertEquals(99L, p.getTotal());
        assertEquals(3L, p.getPage());
        assertEquals(10L, p.getSize());
        assertEquals(3, p.getList().size());
    }

    @Test
    void of_fromIPageWithMapper_shouldMapRecords() {
        IPage<Integer> ipage = new Page<>(1L, 20L, 3L);
        ipage.setRecords(Arrays.asList(1, 2, 3));
        PageResult<String> p = PageResult.of(ipage, i -> "n=" + i);
        List<String> list = p.getList();
        assertEquals(3, list.size());
        assertEquals("n=1", list.get(0));
        assertEquals("n=3", list.get(2));
    }

    @Test
    void nullList_shouldBeNormalizedToEmpty() {
        PageResult<String> p = PageResult.of(0L, 1L, 20L, null);
        assertNotNull(p.getList());
        assertTrue(p.getList().isEmpty());
    }
}
