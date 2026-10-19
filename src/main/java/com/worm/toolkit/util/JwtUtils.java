package com.worm.toolkit.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 工具类（干什么：提供 JWT 签发和验证能力，基于 Hutool JWT 实现）
 * <p>
 * 为什么移除 JJWT：项目已依赖 {@code hutool-all}（含 Hutool JWT），功能开箱即用。
 * 网关 spec 中过期和无效 JWT 均返回 401，不需要 JJWT 的类型化异常区分。
 * 移除 JJWT 减少一个第三方依赖。
 * </p>
 * <p>
 * 为什么不再继承 {@link JWTUtil}：旧代码继承 Hutool {@code JWTUtil} 是为了直接调用
 * 静态方法，但继承工具类是不推荐的模式（工具类不应被继承）。改为独立工具类，
 * 内部委托 {@code JWTUtil} 和 {@code JWT} 的静态方法。
 * </p>
 *
 * @author worm
 */
@SuppressWarnings("unused")
@UtilityClass
@Slf4j
public class JwtUtils {

    /**
     * 签发 Hutool JWT 格式的 Token
     * <p>
     * JWT payload 仅包含 subject（用户名）和过期时间，不包含 userId、tenantId、roles 等字段。
     * 完整用户上下文通过 Redis 传递，JWT 仅作为身份凭证。
     * </p>
     *
     * @param key        JWT 签名密钥
     * @param subject    JWT subject，即登录用户名
     * @param expiration TTL Duration
     * @return 签名后的 JWT 字符串
     */
    public static String createToken(String key, String subject, Duration expiration) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiration);
        return JWT.create()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiresAt(Date.from(expiresAt))
                .setKey(key.getBytes(StandardCharsets.UTF_8))
                .sign();
    }

    /**
     * 验证并解析 JWT Token
     * <p>
     * 验证签名和过期时间。签名无效或 Token 过期时抛出 {@link cn.hutool.jwt.JWTException}。
     * 调用方可通过 {@code jwt.getPayload("sub")} 提取用户名。
     * </p>
     *
     * @param key   JWT 签名密钥
     * @param token JWT 字符串
     * @return 解析后的 JWT 对象
     * @throws cn.hutool.jwt.JWTException 签名无效或 Token 过期
     */
    public static JWT verifyToken(String key, String token) {
        JWT jwt = JWTUtil.parseToken(token);
        // 为什么：R9——hutool verify() 仅验签名，过期 Token 验签仍通过，违背 spec 场景③ —— 干什么：改用 validate(0) 同时校验签名与 exp/nbf 时间字段，失败统一抛 JWTException
        if (!jwt.setKey(key.getBytes(StandardCharsets.UTF_8)).validate(0)) {
            throw new cn.hutool.jwt.JWTException("JWT verification failed: invalid signature or expired token");
        }
        return jwt;
    }
}