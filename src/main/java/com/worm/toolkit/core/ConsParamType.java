package com.worm.toolkit.core;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wbyao
 */
@UtilityClass
@Slf4j
public class ConsParamType {
    /**
     * body
     */
    public static final String PARAM_TYPE_BODY = "body";
    /**
     * path
     */
    public static final String PARAM_TYPE_PATH = "path";
    /**
     * query
     */
    public static final String PARAM_TYPE_QUERY = "query";
    /**
     * form
     */
    public static final String PARAM_TYPE_FORM = "form";
    /**
     * header
     */
    public static final String PARAM_TYPE_HEADER = "header";
}
