package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 限流服务（M7-T1）
 *
 * - 发帖：每用户 N 秒一次
 * - 评论：每用户 N 秒一次
 * - 搜索：每用户/IP 每分钟 M 次
 *
 * 策略：先查后写 + Redis 异常降级放行（C6）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${forum.ratelimit.post-create-seconds:10}")
    private int postCreateSeconds;

    @Value("${forum.ratelimit.comment-create-seconds:5}")
    private int commentCreateSeconds;

    @Value("${forum.ratelimit.search-per-minute:30}")
    private int searchPerMinute;

    @Value("${forum.ratelimit.fallback:allow}")
    private String fallback;

    /** 发帖限流：每用户 N 秒一次。触发抛 PARAM_INVALID。 */
    public void checkPostCreate(Long userId) {
        cooldown("rate:post:" + userId, postCreateSeconds,
                "发帖过于频繁，请 " + postCreateSeconds + " 秒后再试");
    }

    /** 评论限流 */
    public void checkCommentCreate(Long userId) {
        cooldown("rate:comment:" + userId, commentCreateSeconds,
                "评论过于频繁，请 " + commentCreateSeconds + " 秒后再试");
    }

    /** 搜索限流：每分钟 N 次（按 userId 或 ip 区分） */
    public void checkSearch(String identity) {
        String key = "rate:search:" + identity;
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, 60, TimeUnit.SECONDS);
            }
            if (count != null && count > searchPerMinute) {
                throw new BizException(ErrorCode.PARAM_INVALID,
                        "搜索过于频繁，请稍后再试");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            handleRedisFailure("search", identity, e);
        }
    }

    /** 简单冷却：N 秒内只能调一次 */
    private void cooldown(String key, int seconds, String msg) {
        try {
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, "1", seconds, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(ok)) {
                throw new BizException(ErrorCode.PARAM_INVALID, msg);
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            handleRedisFailure(key, "", e);
        }
    }

    /** Redis 故障时按 fallback 策略决定放行还是拒绝（C6 默认 allow） */
    private void handleRedisFailure(String scope, String identity, Exception e) {
        log.warn("[RATE_LIMIT] Redis 异常 scope={} identity={}，降级策略={}", scope, identity, fallback, e);
        if ("deny".equalsIgnoreCase(fallback)) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "限流服务暂不可用");
        }
        // allow：放行
    }
}
