package com.worm.toolkit.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConsHeader {
    /**
     * 下游服务通过此请求头获取用户名(干什么: 网关注入, 下游 TokenOncePerRequestFilter 从此头构建 SecurityContext)
     * <p>
     * 为什么用 X- 前缀: RFC 7231 规定 X- 前缀为自定义头, 避免与标准 HTTP 头冲突.
     * </p>
     */
    public static final String HEADER_USERNAME = "X-Username";
}
