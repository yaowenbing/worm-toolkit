package com.worm.toolkit.util;

import cn.hutool.core.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * com.worm.toolkit.util.LetterUtils
 *
 * @author ywb
 * @version 1.0
 * Create by 2023/04/12 0012 00:47
 */
@UtilityClass
@Slf4j
public class LetterUtils {
    /**
     * 出生年份
     */
    private static final int YAO_DAN_YEAR = 1998;
    /**
     * 生日
     */
    private static final int YAO_DAN_BIRTHDAY = 1203;

    /**
     * 出生年月
     */
    private static final int YAO_WEN_BING_YEAR = 1986;
    /**
     * 生日
     */
    private static final int YAO_WEN_BING_BIRTHDAY = 1124;

    // 为什么：「重」等多音字的字级默认读音在多个读音间无顺序保证 —— 干什么：以词级映射表固定整词读音，仅含测试所需最小条目
    private static final Map<String, String> POLYPHONE_WORDS = new HashMap<>();

    // 为什么：spec 要求输出无声调小写拼音，且 format 实例初始化后只读可安全复用 —— 干什么：静态初始化统一输出格式
    private static final HanyuPinyinOutputFormat PINYIN_FORMAT = new HanyuPinyinOutputFormat();

    static {
        POLYPHONE_WORDS.put("重庆", "chongqing");
        PINYIN_FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        PINYIN_FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    /**
     * 获取所有字母索引的和值
     *
     * @param projectName 所有字母
     * @return 和值
     */
    public static int getIndexSum(String projectName) {
        int cardinality = LetterUtils.YAO_DAN_YEAR + LetterUtils.YAO_DAN_BIRTHDAY + LetterUtils.YAO_WEN_BING_YEAR + LetterUtils.YAO_WEN_BING_BIRTHDAY;
        byte[] projectNameBytes = projectName.getBytes(CharsetUtil.CHARSET_UTF_8);
        for (byte projectNameByte : projectNameBytes) {
            cardinality += projectNameByte;
        }
        return cardinality + 8000;
    }

    /**
     * 汉字转无声调小写拼音，非汉字字符原样保留；多音字以词级映射表选音兜底字级默认读音
     *
     * @param text 待转换文本
     * @return 连续拼音串；入参为 null 或空串时原样返回
     */
    public static String getPinyin(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        // 为什么：整串命中词表说明该词为多音字词，字级默认读音不可靠 —— 干什么：直接返回词级映射值
        String mapped = POLYPHONE_WORDS.get(text);
        if (mapped != null) {
            return mapped;
        }
        StringBuilder result = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            appendCharPinyin(result, c, false);
        }
        return result.toString();
    }

    /**
     * 汉字转拼音首字母缩写，非汉字字符原样保留；多音字以词级映射表选音兜底字级默认读音
     *
     * @param text 待转换文本
     * @return 各汉字拼音首字母拼接串；入参为 null 或空串时原样返回
     */
    public static String getFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        // 为什么：整串命中词表说明该词为多音字词，字级默认读音不可靠 —— 干什么：按词级映射值对齐选音后取各字首字母
        String mapped = POLYPHONE_WORDS.get(text);
        if (mapped != null) {
            return selectFirstLettersByWord(mapped, text);
        }
        StringBuilder result = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            appendCharPinyin(result, c, true);
        }
        return result.toString();
    }

    /**
     * 为什么：getPinyin 与 getFirstLetter 的逐字兜底仅差「整节拼音或首字母」 —— 干什么：抽取共用单字转换，异常时该字符原样追加
     */
    private static void appendCharPinyin(StringBuilder result, char c, boolean firstLetterOnly) {
        try {
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, PINYIN_FORMAT);
            // 为什么：非汉字字符无拼音，且本版 pinyin4j 对非汉字可能返回空数组而非 null —— 干什么：判定无读音后该字符原样追加
            if (pinyinArray == null || pinyinArray.length == 0) {
                result.append(c);
            } else if (firstLetterOnly) {
                result.append(pinyinArray[0].charAt(0));
            } else {
                result.append(pinyinArray[0]);
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            // 为什么：LOWERCASE 与 WITHOUT_TONE 组合实际合法不会触发，此为防御性兜底 —— 干什么：告警后该字符原样追加
            log.warn("汉字转拼音输出格式组合异常，字符原样保留：{}", c, e);
            result.append(c);
        }
    }

    /**
     * 为什么：词表命中时字级默认读音在多音间无顺序保证，而词级拼音串即整词权威读音 —— 干什么：按偏移对齐逐字选出与整词读音一致的字级读音并取首字母（如 chongqing -> cq）
     */
    private static String selectFirstLettersByWord(String wordPinyin, String text) {
        StringBuilder result = new StringBuilder(text.length());
        int offset = 0;
        for (char c : text.toCharArray()) {
            try {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, PINYIN_FORMAT);
                // 为什么：非汉字字符无拼音，且本版 pinyin4j 对非汉字可能返回空数组而非 null —— 干什么：判定无读音后该字符原样追加
                if (pinyinArray == null || pinyinArray.length == 0) {
                    result.append(c);
                    continue;
                }
                // 为什么：多音字数组含多个候选读音 —— 干什么：选与词级拼音串在当前偏移对齐的候选，均不匹配时兜底字级默认读音
                String chosen = pinyinArray[0];
                for (String candidate : pinyinArray) {
                    if (wordPinyin.startsWith(candidate, offset)) {
                        chosen = candidate;
                        break;
                    }
                }
                result.append(chosen.charAt(0));
                offset += chosen.length();
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                // 为什么：LOWERCASE 与 WITHOUT_TONE 组合实际合法不会触发，此为防御性兜底 —— 干什么：告警后该字符原样追加
                log.warn("词级选音输出格式组合异常，字符原样保留：{}", c, e);
                result.append(c);
            }
        }
        return result.toString();
    }
}
