package com.worm.toolkit.gen;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author worm
 */
@AllArgsConstructor
@Getter
public enum EnumDateType {
    DATE("java.util.Date"),
    LOCAL_DATE("java.time.LocalDate"),
    ;
    private final String val;

    public static EnumDateType of(String val) {
        return Arrays.stream(EnumDateType.values()).filter(v -> v.val.equals(val)).findFirst().orElse(null);
    }
}
