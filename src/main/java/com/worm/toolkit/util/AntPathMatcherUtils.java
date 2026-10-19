package com.worm.toolkit.util;

import cn.hutool.core.text.AntPathMatcher;
import cn.hutool.core.util.ReUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author worm
 */
@UtilityClass
@Slf4j
public class AntPathMatcherUtils {

    /**
     * 匹配路径
     *
     * @param regularPath 正则路径
     * @param targetPath  目标路径
     * @return 是否匹配
     */
    public static boolean match(String regularPath, String targetPath) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        antPathMatcher.setCaseSensitive(false);
        antPathMatcher.setTrimTokens(true);
        // 路径是否匹配
        boolean match = antPathMatcher.match(regularPath, targetPath);
        log.info("\n时间:{},路径规则:{},路径：{},匹配结果:{}", LocalDateTimeUtils.ofCtt(), regularPath, targetPath, match);
        return match;
    }

    public static Boolean isRestful(String uri) {
        return getRestfulArgs(uri).size() > BigDecimal.ZERO.intValue();
    }

    /**
     * 判断路径是否为restful的风格
     *
     * @param uri url路径
     * @return 是否为rest风格
     */
    public static List<String> getRestfulArgs(String uri) {
        return ReUtil.findAll(Pattern.compile("/\\{(\\w+)}"), uri, BigDecimal.ONE.intValue());
    }

    public static List<String> getFormArgs(String uri) {
        return ReUtil.findAll(Pattern.compile("(\\w+=\\{(\\w+)})"), uri, 2);
    }
}
