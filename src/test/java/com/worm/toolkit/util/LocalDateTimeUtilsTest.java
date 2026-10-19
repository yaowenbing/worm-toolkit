package com.worm.toolkit.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LocalDateTimeUtils} 时间处理能力测试（spec 时间处理：ofCtt 当前 CTT 时间 / 继承 Hutool 格式化行为）。
 *
 * @author worm
 */
class LocalDateTimeUtilsTest {

    /**
     * 为什么：spec 场景①要求 ofCtt() 返回 CTT 时区（CTT 经 ZoneId.SHORT_IDS 映射为 Asia/Shanghai）的当前时间，
     * 而 LocalDateTime 本身不携带时区概念，可验证形式为与 LocalDateTime.now(Asia/Shanghai) 偏差在容差内
     * —— 干什么：断言 ofCtt() 非空且偏差秒数小于 60 秒（容差防慢机时钟抖动误报）
     */
    @Test
    void ofCttReturnsCurrentCttTime() {
        LocalDateTime actual = LocalDateTimeUtils.ofCtt();
        assertNotNull(actual);
        Duration deviation = Duration.between(actual, LocalDateTime.now(ZoneId.of("Asia/Shanghai"))).abs();
        assertTrue(deviation.getSeconds() < 60);
    }

    /**
     * 为什么：spec 场景②要求从 Hutool LocalDateTimeUtil 继承的格式化方法行为与 Hutool 一致（旁证 extends 生效）
     * —— 干什么：验证 2026-01-02T03:04:05 按 yyyy-MM-dd 格式化为 2026-01-02
     */
    @Test
    void inheritedFormatMatchesHutoolBehavior() {
        LocalDateTime input = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
        assertEquals("2026-01-02", LocalDateTimeUtils.format(input, "yyyy-MM-dd"));
    }
}
