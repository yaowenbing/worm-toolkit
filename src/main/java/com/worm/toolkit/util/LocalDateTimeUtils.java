package com.worm.toolkit.util;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author liuho
 */
@SuppressWarnings("unused")
@UtilityClass
@Slf4j
public class LocalDateTimeUtils extends LocalDateTimeUtil {
    /**
     * 获取亚洲时间得当前时间
     *
     * @return 亚洲时间得当前时间
     */
    public static LocalDateTime ofCtt() {
        return LocalDateTimeUtil.of(Instant.now(), ZoneId.of("CTT", ZoneId.SHORT_IDS));
    }
}
