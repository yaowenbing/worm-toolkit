package com.worm.toolkit.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * 用户上下文 DTO
 * <p>
 * 作为 Redis 存储值（key = {@code iam:user:{username}}）和 Spring Security
 * {@code SecurityContext.principal} 的统一用户上下文对象。实现 {@link TenantDetails}
 * 接口以维持 {@code TenantContextFilter} 的多租户隔离链路。
 * </p>
 *
 * <p>
 * 数据流：
 * <pre>
 * IAM 登录 → Jackson JSON 序列化 → Redis SET iam:user:{username}
 * 下游服务 → Redis GET iam:user:{username} → Jackson JSON 反序列化 → UserContext
 * → SecurityContext.principal → TenantContextFilter instanceof TenantDetails
 * </pre>
 * </p>
 *
 * <p>
 * 匿名哨兵值：当网关注入 {@code X-Username: anonymous} 时，下游服务构建匿名 UserContext，
 * 字段取固定哨兵值（userId=0, username="anonymous", tenantId=null, deptId=null,
 * superAdmin=false, roles=[], perms=[]），不执行 Redis GET 查询。
 * </p>
 *
 * @author ywb
 * @since 2026-06-05
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserContext implements TenantDetails {

    public static final String ANONYMOUS_USERNAME = "anonymous";

    private Long userId;

    private String username;

    private String fullName;

    private String avatar;

    private Long tenantId;

    private Long deptId;

    private List<String> roles;

    private List<String> perms;

    private boolean superAdmin;

    /**
     * 有效数据权限范围（1=全部、2=本级、3=本级及子级、4=自定义）
     * <p>
     * 登录时由 {@code UserDetailsServiceImpl} 根据用户所有角色的 dataScope 计算得出
     * （多角色取最宽范围：1>4>3>2），供 {@code IamDataScopeProvider} 读取。
     * 匿名用户为 {@code null}。
     * </p>
     */
    private Integer dataScope;

    /**
     * 本级及子级数据权限的部门 ID 列表（dataScope=3 时有效）
     * <p>
     * 登录时由 {@code UserDetailsServiceImpl} 递归计算用户部门的所有子部门 ID 并集（含间接子级），
     * 供 {@code IamDataScopeProvider.getSubDeptIds()} 直接读取，避免运行时查库导致循环依赖。
     * 非 dataScope=3 时为空列表。
     * </p>
     */
    private List<Long> subDeptIds;

    /**
     * 自定义数据权限的部门 ID 列表
     * <p>
     * 仅在有效 dataScope=4 时有值，为所有 dataScope=4 角色关联部门 ID 的并集（去重）。
     * 登录时由 {@code UserDetailsServiceImpl} 计算，供 {@code IamDataScopeProvider.getCustomDeptIds()} 读取。
     * 非 dataScope=4 时为空列表。
     * </p>
     */
    private List<Long> customDeptIds;

    /**
     * 构建匿名 UserContext 哨兵对象
     * <p>
     * 当网关注入 {@code X-Username: anonymous} 时，{@code TokenOncePerRequestFilter}
     * 调用此方法构建匿名 principal，不执行 Redis GET 查询。
     * </p>
     *
     * @return 匿名 UserContext，roles 和 perms 为不可变空列表
     */
    public static UserContext anonymous() {
        return new UserContext()
                .setUserId(0L)
                .setUsername(ANONYMOUS_USERNAME)
                .setTenantId(null)
                .setDeptId(null)
                .setRoles(Collections.emptyList())
                .setPerms(Collections.emptyList())
                .setSuperAdmin(false)
                .setDataScope(null)
                .setSubDeptIds(Collections.emptyList())
                .setCustomDeptIds(Collections.emptyList());
    }

    /**
     * 判断当前上下文是否为匿名用户
     *
     * @return {@code true} 表示匿名用户（非真实登录态）
     */
    public boolean isAnonymous() {
        return ANONYMOUS_USERNAME.equals(this.username);
    }
}