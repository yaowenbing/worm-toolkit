package com.worm.toolkit.core;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author worm
 */
public class PageVo<T> extends Page<T> {

    @Schema(description = "当前页码", example = "1")
    @Override
    public long getCurrent() {
        return super.getCurrent();
    }

    @Schema(description = "每页显示条数", example = "10")
    @Override
    public long getSize() {
        return super.getSize();
    }

    @Schema(description = "总记录数", example = "100")
    @Override
    public long getTotal() {
        return super.getTotal();
    }

    @Schema(description = "总页数", example = "10")
    @Override
    public long getPages() {
        return super.getPages();
    }
}
