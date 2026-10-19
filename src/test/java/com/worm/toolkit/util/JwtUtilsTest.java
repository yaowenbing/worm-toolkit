package com.worm.toolkit.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link JwtUtils} JWT 签发与验签能力测试（spec JWT 签发与验签：同密钥验签通过且 payload sub 为 alice / 错误密钥验签抛 JWTException / 过期 Token 验签抛 JWTException）。
 *
 * @author worm
 */
class JwtUtilsTest {

    /**
     * 为什么：spec 场景①要求以密钥 key 为用户 alice 签发 TTL 1 小时的 Token，再以同一密钥验签 SHALL 通过且 payload 的 sub 为 alice
     * —— 干什么：断言 createToken("key", "alice", 1小时) 签发的 Token 经 verifyToken("key", token) 解析后 getPayload("sub") 等于 alice
     */
    @Test
    void createAndVerifyReturnsSubject() {
        String token = JwtUtils.createToken("key", "alice", Duration.ofHours(1));
        JWT jwt = JwtUtils.verifyToken("key", token);
        assertEquals("alice", jwt.getPayload("sub"));
    }

    /**
     * 为什么：spec 场景②要求以密钥 key 签发的 Token 改用密钥 other 验签时，密钥不符导致签名校验失败 SHALL 抛出 JWTException
     * —— 干什么：断言 verifyToken("other", token) 抛出 cn.hutool.jwt.JWTException
     */
    @Test
    void verifyWithWrongKeyThrowsJwtException() {
        String token = JwtUtils.createToken("key", "alice", Duration.ofHours(1));
        assertThrows(JWTException.class, () -> JwtUtils.verifyToken("other", token));
    }

    /**
     * 为什么：spec 场景③要求对已过期 Token 验签 SHALL 抛出 JWTException（R9 备案：旧 verifyToken 用 hutool verify() 仅验签名不查过期，2.10 将改用 validate(0) 实现 spec 合规；createToken 内部以 now.plus(expiration) 计算 exp，传负 TTL 即得 exp 早于当前时间的已过期 Token，无需 sleep）
     * —— 干什么：以 Duration.ofSeconds(-60) 签发已过期 Token，断言 verifyToken("key", token) 抛出 cn.hutool.jwt.JWTException
     */
    @Test
    void verifyExpiredTokenThrowsJwtException() {
        String token = JwtUtils.createToken("key", "alice", Duration.ofSeconds(-60));
        assertThrows(JWTException.class, () -> JwtUtils.verifyToken("key", token));
    }
}
