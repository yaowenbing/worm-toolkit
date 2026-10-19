package com.worm.toolkit.core;

/**
 * 租户 ID 提供者接口
 * <p>
 * 用于多租户场景下，为字段自动填充提供当前请求的租户 ID。
 * 业务层提供该接口的实现（从 TenantContextHolder 获取），
 * 非多租户场景下使用默认实现返回 null，此时不填充 tenant_id。
 * 理由：解耦 mybatisPlus-starter 和具体业务模块，避免循环依赖。
 * </p>
 *
 * @author ywb
 */
@FunctionalInterface
public interface TenantIdSupplier {

    /**
     * 获取当前请求的租户 ID
     *
     * @return 租户 ID，可能为 null（未启用多租户时）
     */
    Long getTenantId();

    /**
     * 判断当前请求是否为超级管理员（跳过租户过滤）。
     * <p>
     * 默认返回 false，业务层的实现会覆盖此方法以提供
     * 真实的超级管理员判定逻辑。
     * </p>
     *
     * @return true 表示为超级管理员
     */
    default boolean isSuperAdmin() {
        return false;
    }
}
