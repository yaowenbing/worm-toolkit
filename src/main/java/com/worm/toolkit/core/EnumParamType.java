package com.worm.toolkit.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wbyao
 */
@Getter
@AllArgsConstructor
public enum EnumParamType {
    /**
     * body
     */
    BODY(ConsParamType.PARAM_TYPE_BODY),
    /**
     * body
     */
    PATH(ConsParamType.PARAM_TYPE_PATH),
    /**
     * body
     */
    QUERY(ConsParamType.PARAM_TYPE_QUERY),
    /**
     * body
     */
    FORM(ConsParamType.PARAM_TYPE_FORM),
    /**
     * body
     */
    HEADER(ConsParamType.PARAM_TYPE_HEADER);

    private final String code;
}
