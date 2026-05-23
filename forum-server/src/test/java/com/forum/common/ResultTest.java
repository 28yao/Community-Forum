package com.forum.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 单元测试
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
class ResultTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void success_noData_shouldHaveCode0() {
        Result<Void> r = Result.success();
        assertEquals(0, r.getCode());
        assertEquals("success", r.getMessage());
        assertNull(r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    void success_withData_shouldCarryData() {
        Result<String> r = Result.success("hello");
        assertEquals(0, r.getCode());
        assertEquals("hello", r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    void success_customMessage_shouldOverride() {
        List<Integer> data = Arrays.asList(1, 2, 3);
        Result<List<Integer>> r = Result.success("ok", data);
        assertEquals("ok", r.getMessage());
        assertEquals(3, r.getData().size());
    }

    @Test
    void fail_withCodeAndMessage_shouldNotBeSuccess() {
        Result<Void> r = Result.fail(2001, "参数校验失败");
        assertEquals(2001, r.getCode());
        assertEquals("参数校验失败", r.getMessage());
        assertNull(r.getData());
        assertFalse(r.isSuccess());
    }

    @Test
    void fail_withErrorCode_shouldUseEnumValues() {
        Result<Void> r = Result.fail(ErrorCode.SUCCESS);
        assertEquals(ErrorCode.SUCCESS.getCode(), r.getCode());
        assertEquals(ErrorCode.SUCCESS.getMessage(), r.getMessage());
    }

    @Test
    void jsonSerialization_shouldOmitNullData() throws Exception {
        Result<Void> r = Result.success();
        String json = mapper.writeValueAsString(r);
        // data 为 null 时不应序列化
        assertFalse(json.contains("\"data\""), "JSON should omit null data, got: " + json);
        assertTrue(json.contains("\"code\":0"));
        assertTrue(json.contains("\"message\":\"success\""));
    }

    @Test
    void jsonSerialization_withData_shouldIncludeData() throws Exception {
        Result<String> r = Result.success("hello");
        String json = mapper.writeValueAsString(r);
        assertTrue(json.contains("\"data\":\"hello\""), "JSON should contain data, got: " + json);
    }
}
