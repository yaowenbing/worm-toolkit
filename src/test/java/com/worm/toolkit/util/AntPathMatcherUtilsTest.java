package com.worm.toolkit.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AntPathMatcherUtils} Ant 路径匹配能力测试（spec Ant 路径匹配：忽略大小写匹配 / 不匹配返回 false / 提取 RESTful 参数名）。
 *
 * @author worm
 */
class AntPathMatcherUtilsTest {

    /**
     * 为什么：spec 场景①要求规则与目标路径大小写不一致时 SHALL 匹配成功，
     * 依赖 Hutool AntPathMatcher 在 setCaseSensitive(false) 生效下的忽略大小写语义
     * —— 干什么：断言规则 /API/{id}/USER 匹配目标路径 /api/123/user 返回 true
     */
    @Test
    void matchIgnoresCase() {
        assertTrue(AntPathMatcherUtils.match("/API/{id}/USER", "/api/123/user"));
    }

    /**
     * 为什么：spec 场景②要求首段字面不等时 SHALL 返回 false（首段 /other 与 /api 不等，忽略大小写也无法弥补结构差异）
     * —— 干什么：断言规则 /api/{id} 匹配目标路径 /other/123 返回 false
     */
    @Test
    void matchReturnsFalseWhenPathMismatch() {
        assertFalse(AntPathMatcherUtils.match("/api/{id}", "/other/123"));
    }

    /**
     * 为什么：spec 场景③要求参数提取方法按出现顺序提取路径模板中的花括号参数名
     * —— 干什么：断言 /api/{version}/users/{id} 提取结果恰为 [version, id]（顺序即出现顺序）
     */
    @Test
    void getRestfulArgsExtractsParameterNames() {
        List<String> args = AntPathMatcherUtils.getRestfulArgs("/api/{version}/users/{id}");
        assertEquals(Arrays.asList("version", "id"), args);
    }
}
