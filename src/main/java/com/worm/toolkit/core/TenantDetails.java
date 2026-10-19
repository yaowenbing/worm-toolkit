package com.worm.toolkit.core;

/**
 * 租户详情接口
 * <p>
 * 标记认证主体（UserDetails / UserContext）所属的租户，使 {@code TenantContextFilter} 能从
 * Spring Security 的认证信息中提取租户 ID，桥接到 {@code TenantContextHolder}。
 * </p>
 *
 * <p>
 * 为什么上移到 公共工具模块：
 * 原位于 mybatisPlus-spring-boot-starter，但 gateway-auth 提案引入的 UserContext（同在
 * 公共工具模块 的 model 包）也需要实现此接口以支持在 gateway 模式下维持多租户
 * 数据隔离链路。由于 mybatisPlus-starter 已依赖 公共工具模块，直接在此实现会导致
 * 循环依赖。将此接口上移至最底层公共模块后，两个实现类均可通过编译。
 * </p>
 *
 * <p>
 * 使用方式：让 UserDetails / UserContext 实现类同时实现此接口，例如：
 * <pre>
 * public class MyUserContext implements TenantDetails {
 *     private Long tenantId;
 *     private boolean superAdmin;
 *
 *     &#64;Override
 *     public Long getTenantId() { return tenantId; }
 *
 *     &#64;Override
 *     public boolean isSuperAdmin() { return superAdmin; }
 * }
 * </pre>
 * </p>
 *
 * <p>
 * 数据流：
 * <pre>
 * 请求 → TenantContextFilter → SecurityContext.Authentication.principal
 *        → instanceof TenantDetails → getTenantId() → TenantContextHolder.setTenantId()
 *        → TenantIdSupplier.getTenantId() → SQL 拼接 tenant_id 条件
 * </pre>
 * </p>
 *
 * @author ywb
 * @since 2026-06-04
 */
public interface TenantDetails {

    /**
     * 获取当前认证主体所属的租户 ID
     * <p>
     * 由 {@code TenantContextFilter} 调用，提取后设置到 {@code TenantContextHolder}，
     * 最终由 {@code TenantIdSupplier} 读取，
     * 用于 MyBatis-Plus 租户拦截器自动拼接 {@code AND tenant_id = ?} 条件。
     * </p>
     *
     * @return 租户 ID，{@code null} 表示全局用户（超级管理员，不受租户隔离限制）
     */
    Long getTenantId();

    /**
     * 判断当前认证主体是否为超级管理员
     * <p>
     * 超级管理员不受租户隔离限制，可查看和操作所有租户的数据。
     * 由 {@code TenantContextFilter} 调用，设置为 {@code TenantContextHolder#setSuperAdmin(boolean)}，
     * 最终影响：
     * <ul>
     *     <li>{@code TenantInnerInterceptor}：超级管理员时不拼接租户过滤条件</li>
     *     <li>{@code TenantIdSupplier#isSuperAdmin()}：权限校验时跳过租户级别检查</li>
     * </ul>
     * </p>
     *
     * <p>
     * 为什么默认返回 {@code false}：绝大多数用户都是普通租户用户，只有极少数系统管理员
     * 是超级管理员。默认 {@code false} 确保安全——不确定时宁可不跳过租户过滤，
     * 也不让普通用户看到其他租户数据。
     * </p>
     *
     * @return 是否为超级管理员，默认 {@code false}
     */
    default boolean isSuperAdmin() {
        // 默认 false：绝大多数用户为普通租户用户，安全优先——不确定时不过滤不如多过滤
        return false;
    }
}
