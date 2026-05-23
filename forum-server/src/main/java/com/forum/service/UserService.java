package com.forum.service;

import com.forum.common.ErrorCode;
import com.forum.common.JwtUtil;
import com.forum.common.exception.BizException;
import com.forum.entity.User;
import com.forum.entity.VerificationToken;
import com.forum.mapper.UserMapper;
import com.forum.mapper.VerificationTokenMapper;
import com.forum.service.dto.LoginRequest;
import com.forum.service.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户业务服务
 *
 * @author liuxinsi
 * @date 2026-05-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final VerificationTokenMapper tokenMapper;
    private final MailService mailService;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /** 验证 Token 有效期：24 小时（PRD §5.1.1） */
    private static final long VERIFY_TOKEN_VALID_HOURS = 24;

    /** 登录失败锁定阈值：5 次 */
    private static final int LOGIN_FAIL_LIMIT = 5;

    /** 锁定时长：15 分钟 */
    private static final long LOGIN_LOCK_MINUTES = 15;

    /** Redis Token 前缀 */
    private static final String TOKEN_REDIS_PREFIX = "token:";

    /**
     * 用户注册（plan.md §6.1）
     *
     * 步骤：邮箱唯一性 → 昵称唯一性 → bcrypt 加密 → 插入 user → 生成 verification token → 发送邮件
     *
     * @return 新用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest req) {
        // 1. 邮箱唯一性
        if (userMapper.selectByEmail(req.getEmail()) != null) {
            throw new BizException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        // 2. 昵称唯一性
        if (userMapper.selectByNickname(req.getNickname()) != null) {
            throw new BizException(ErrorCode.NICKNAME_EXISTS);
        }

        // 3. 构造用户，密码 bcrypt
        User u = new User();
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setNickname(req.getNickname());
        u.setRole("user");
        u.setStatus(1);
        u.setEmailVerified(0);
        u.setLoginFailCount(0);

        userMapper.insert(u);

        // 4. 生成验证 Token
        VerificationToken t = new VerificationToken();
        t.setUserId(u.getId());
        t.setEmail(u.getEmail());
        t.setToken(generateToken());
        t.setType(VerificationToken.TYPE_REGISTER);
        t.setUsed(0);
        t.setExpiresAt(LocalDateTime.now().plusHours(VERIFY_TOKEN_VALID_HOURS));
        tokenMapper.insert(t);

        // 5. 发邮件（失败回滚整个事务）
        mailService.sendVerificationMail(u.getEmail(), u.getNickname(), t.getToken());

        log.info("[REGISTER] new user id={} email={} nickname={}", u.getId(), u.getEmail(), u.getNickname());
        return u.getId();
    }

    /** 生成 64 字符的随机 token（UUID 去掉横杠 + 32 位随机后缀） */
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 邮箱验证（plan.md §5.1.1）
     *
     * @param token 邮件链接中的 token
     * @return 完成验证的用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long verifyEmail(String token) {
        if (token == null || token.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "验证令牌不能为空");
        }
        VerificationToken t = tokenMapper.selectByToken(token);
        if (t == null || !VerificationToken.TYPE_REGISTER.equals(t.getType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "验证链接无效");
        }
        if (Integer.valueOf(1).equals(t.getUsed())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该链接已使用");
        }
        if (t.getExpiresAt() == null || t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "验证链接已过期");
        }
        User u = userMapper.selectById(t.getUserId());
        if (u == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "用户不存在");
        }
        // 已验证：直接返回（幂等）
        if (Integer.valueOf(1).equals(u.getEmailVerified())) {
            t.setUsed(1);
            tokenMapper.updateById(t);
            return u.getId();
        }
        // 更新用户已验证
        u.setEmailVerified(1);
        userMapper.updateById(u);
        // 标记 token 已使用
        t.setUsed(1);
        tokenMapper.updateById(t);
        log.info("[VERIFY_EMAIL] user id={} verified", u.getId());
        return u.getId();
    }

    /**
     * 重发验证邮件（含 60s 冷却）
     *
     * 冷却：Redis key `mail:resend:cooldown:{email}` 60s TTL
     */
    @Transactional(rollbackFor = Exception.class)
    public void resendVerification(String email) {
        if (email == null || email.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "邮箱不能为空");
        }
        User u = userMapper.selectByEmail(email);
        if (u == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该邮箱未注册");
        }
        if (Integer.valueOf(1).equals(u.getEmailVerified())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "邮箱已验证，无需重发");
        }
        // 冷却检查
        String cooldownKey = "mail:resend:cooldown:" + email;
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", 60, java.util.concurrent.TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(ok)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请求过于频繁，请稍后再试（60 秒冷却）");
        }
        // 生成新 token，发邮件（不清旧 token；旧 token 在过期前仍可用，符合 PRD 容错）
        VerificationToken t = new VerificationToken();
        t.setUserId(u.getId());
        t.setEmail(u.getEmail());
        t.setToken(generateToken());
        t.setType(VerificationToken.TYPE_REGISTER);
        t.setUsed(0);
        t.setExpiresAt(LocalDateTime.now().plusHours(VERIFY_TOKEN_VALID_HOURS));
        tokenMapper.insert(t);
        mailService.sendVerificationMail(u.getEmail(), u.getNickname(), t.getToken());
        log.info("[RESEND_VERIFY] user id={} resent token prefix={}...", u.getId(), t.getToken().substring(0, 8));
    }

    /**
     * 用户登录（M1-T9）
     *
     * 流程：查邮箱 → 封禁校验 → 锁定校验 → 密码校验 → 失败计数/锁定 → 成功重置 + 签发 JWT + 存 Redis
     *
     * @return Map{token, user}
     */
    public Map<String, Object> login(LoginRequest req) {
        // 1. 查用户
        User u = userMapper.selectByEmail(req.getEmail());
        if (u == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "邮箱或密码错误");
        }
        // 2. 封禁校验
        if (Integer.valueOf(0).equals(u.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_BANNED);
        }
        // 3. 锁定校验
        if (u.getLockedUntil() != null && u.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BizException(ErrorCode.LOGIN_FAIL_LIMIT,
                    "登录失败次数过多，账号已锁定至 " + u.getLockedUntil().toLocalTime().toString().substring(0, 5));
        }
        // 4. 密码校验
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            handleLoginFail(u);
            throw new BizException(ErrorCode.PARAM_INVALID, "邮箱或密码错误");
        }
        // 5. 登录成功：重置失败计数
        userMapper.resetLoginFailCount(u.getId());
        // 6. 签发 JWT
        String token = jwtUtil.generateToken(u.getId(), u.getRole());
        // 7. 存 Redis（便于踢人/登出）
        long expireDays = 7;
        redisTemplate.opsForValue().set(TOKEN_REDIS_PREFIX + u.getId(), token,
                expireDays, java.util.concurrent.TimeUnit.DAYS);
        log.info("[LOGIN] user id={} email={}", u.getId(), u.getEmail());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", u.getId());
        userInfo.put("email", u.getEmail());
        userInfo.put("nickname", u.getNickname());
        userInfo.put("avatar", u.getAvatar());
        userInfo.put("role", u.getRole());
        result.put("user", userInfo);
        return result;
    }

    /** 登出：删除 Redis 中的 Token */
    public void logout(Long userId) {
        redisTemplate.delete(TOKEN_REDIS_PREFIX + userId);
        log.info("[LOGOUT] user id={}", userId);
    }

    /** 登出（从 Token 解析 userId，AuthInterceptor 未就绪时的临时方案） */
    public Long logoutByToken(String token) {
        Long userId = jwtUtil.parseUserId(token);
        logout(userId);
        return userId;
    }

    private void handleLoginFail(User u) {
        int newCount = (u.getLoginFailCount() == null ? 0 : u.getLoginFailCount()) + 1;
        if (newCount >= LOGIN_FAIL_LIMIT) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOGIN_LOCK_MINUTES);
            userMapper.lockAccount(u.getId(), lockUntil);
            log.warn("[LOGIN_LOCK] user id={} locked until {}", u.getId(), lockUntil);
        }
        userMapper.incrLoginFailCount(u.getId());
    }

    /** 昵称冷却天数：30 天 */
    private static final int NICKNAME_COOLDOWN_DAYS = 30;

    /**
     * 获取用户公开信息（M1-T15）
     */
    public User getUserById(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "用户不存在");
        }
        return u;
    }

    /**
     * 更新个人资料（M1-T15）
     *
     * 昵称修改受 30 天冷却限制。昵称重复抛 2003。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, String nickname, String bio) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "用户不存在");
        }
        // 昵称修改
        if (nickname != null && !nickname.equals(u.getNickname())) {
            // 冷却检查
            if (u.getNicknameUpdatedAt() != null) {
                LocalDateTime nextAllowed = u.getNicknameUpdatedAt().plusDays(NICKNAME_COOLDOWN_DAYS);
                if (nextAllowed.isAfter(LocalDateTime.now())) {
                    throw new BizException(ErrorCode.NICKNAME_COOLDOWN,
                            "昵称每 30 天可修改一次，下次可修改时间：" + nextAllowed.toLocalDate());
                }
            }
            // 唯一性检查
            if (userMapper.selectByNickname(nickname) != null) {
                throw new BizException(ErrorCode.NICKNAME_EXISTS);
            }
            u.setNickname(nickname);
            u.setNicknameUpdatedAt(LocalDateTime.now());
        }
        // 简介修改
        if (bio != null) {
            u.setBio(bio);
        }
        userMapper.updateById(u);
        log.info("[UPDATE_PROFILE] user id={}", userId);
    }

    /**
     * 更新头像 URL（M1-T18）
     */
    public void updateAvatar(Long userId, String avatarUrl) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "用户不存在");
        }
        u.setAvatar(avatarUrl);
        userMapper.updateById(u);
        log.info("[UPDATE_AVATAR] user id={}", userId);
    }
}
