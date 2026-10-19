package com.worm.toolkit.core;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author liuho
 */
@Data
public class PageDto {
    /**
     * 每页显示条数，默认 10，最大值 100
     */
    @Schema(description = "每页显示条数，默认 10，最大值 100", example = "10")
    private long size;

    /**
     * 当前页
     */
    @Schema(description = "当前页", example = "1")
    private long current;
}
