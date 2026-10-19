package com.worm.toolkit.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link LetterUtils} 汉字转拼音能力测试（spec toolkit-utilities：单字/词语/非汉字/首字母缩写）。
 *
 * @author worm
 */
class LetterUtilsTest {

    /**
     * 为什么：spec 要求单字转拼音返回无声调拼音 —— 干什么：验证「中」转 zhong
     */
    @Test
    void getPinyinReturnsTonelessPinyinForSingleChar() {
        assertEquals("zhong", LetterUtils.getPinyin("中"));
    }

    /**
     * 为什么：spec 要求词语按多音字规则选词后输出连续拼音 —— 干什么：验证「重庆」选 chong 而非 zhong
     */
    @Test
    void getPinyinResolvesPolyphoneByWord() {
        assertEquals("chongqing", LetterUtils.getPinyin("重庆"));
    }

    /**
     * 为什么：spec 要求纯非汉字输入原样返回不抛异常 —— 干什么：验证英文数字混合串原样
     */
    @Test
    void getPinyinReturnsNonChineseInputAsIs() {
        assertEquals("abc123", LetterUtils.getPinyin("abc123"));
    }

    /**
     * 为什么：spec SHALL 要求拼音首字母缩写能力 —— 干什么：验证「重庆」首字母缩写为 cq
     */
    @Test
    void getFirstLetterReturnsAcronym() {
        assertEquals("cq", LetterUtils.getFirstLetter("重庆"));
    }
}
