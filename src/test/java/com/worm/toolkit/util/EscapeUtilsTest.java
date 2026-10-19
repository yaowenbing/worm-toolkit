package com.worm.toolkit.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EscapeUtils#sanitize(String)} XSS 净化行为测试（对齐 specs/toolkit-utilities/spec.md XSS Requirement 三场景）。
 *
 * @author worm
 */
class EscapeUtilsTest {

    /**
     * 为什么：spec 场景 1 要求 javascript: 协议属性被移除且锚标签本身保留 —— 干什么：验证脚本协议被滤除而链接元素不丢失
     */
    @Test
    void sanitizeRemovesJavascriptProtocolButKeepsAnchorTag() {
        String sanitized = EscapeUtils.sanitize("<a href=\"javascript:alert(1)\">link</a>");
        // 为什么：javascript: 协议值必须被移除 —— 干什么：断言输出不含脚本前缀
        assertFalse(sanitized.contains("javascript:"));
        // 为什么：spec 要求 <a> 标签本身保留 —— 干什么：断言锚元素仍在输出中
        assertTrue(sanitized.contains("<a"));
        // 为什么：链接文本不应被误删 —— 干什么：断言文本内容仍在输出中
        assertTrue(sanitized.contains("link"));
    }

    /**
     * 为什么：spec 场景 2 要求 on* 内联事件属性被移除且 <div> 标签保留 —— 干什么：验证 onclick 被滤除而容器元素不丢失
     */
    @Test
    void sanitizeRemovesOnEventAttributeButKeepsDivElement() {
        String sanitized = EscapeUtils.sanitize("<div onclick=\"hack()\">x</div>");
        // 为什么：onclick 是 XSS 主要载体且未被属性白名单放行 —— 干什么：断言输出不含 onclick
        assertFalse(sanitized.contains("onclick"));
        // 为什么：spec 要求 <div> 标签保留 —— 干什么：断言容器元素仍在输出中
        assertTrue(sanitized.contains("<div"));
    }

    /**
     * 为什么：spec 场景 3 要求 script 标签及其内容整体移除且 <b> 标签保留 —— 干什么：验证内联脚本文本不残留且格式元素不丢失
     */
    @Test
    void sanitizeRemovesScriptElementWithContentButKeepsBoldTag() {
        String sanitized = EscapeUtils.sanitize("<script>alert(1)</script><b>x</b>");
        // 为什么：script 必须连内容整体移除 —— 干什么：断言输出不含 script 字样
        assertFalse(sanitized.contains("script"));
        // 为什么：内联脚本文本不得残留 —— 干什么：断言输出不含 alert
        assertFalse(sanitized.contains("alert"));
        // 为什么：spec 要求 <b> 标签保留 —— 干什么：断言格式元素仍在输出中
        assertTrue(sanitized.contains("<b>"));
    }

    /**
     * 为什么：白名单外标签可能携带不可控结构 —— 干什么：验证自定义标签 mytag 被移除
     */
    @Test
    void sanitizeRemovesTagsOutsideWhitelist() {
        String sanitized = EscapeUtils.sanitize("<mytag>hello</mytag>");
        // 为什么：mytag 不在白名单内 —— 干什么：断言输出不含 mytag
        assertFalse(sanitized.contains("mytag"));
    }

    /**
     * 为什么：jsoup 对 null 输入的 Validate 校验会抛异常 —— 干什么：验证 null 输入以异常形式兜住而非静默返回
     */
    @Test
    void sanitizeThrowsOnNullInput() {
        assertThrows(Exception.class, () -> EscapeUtils.sanitize(null));
    }
}
