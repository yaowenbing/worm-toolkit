package com.worm.toolkit.gen;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.Data;

import java.util.List;

/**
 * @author winiger
 */
@Data
public class MybatisPlusGenProperties {
    /**
     * 代码生成器开关 默认不开启
     */
    private boolean enable = true;
    /**
     * 数据库相关配置 mysql
     */
    private DbType dbType = DbType.MYSQL;
    /**
     * 驱动名称 默认 com.mysql.cj.jdbc.Driver
     */
    private String driverName = "com.mysql.cj.jdbc.Driver";
    /**
     * 配置备注信息 姚文兵
     */
    private String author = "姚文兵[worm][liuhong89671124@gmail.com]";
    /**
     * 主机
     */
    private String host;
    /**
     * 端口
     */
    private int port;
    /**
     * 数据库名称
     */
    private String dbName;
    /**
     * 数据库用户名
     */
    private String uname;
    /**
     * 密码
     */
    private String pwd;
    /**
     * 生成的文件所在的目录，使用相对于当前项目的根目录结构
     */
    private String outputPath = ".";
    /**
     * 指定包名，默认 com.worm
     */
    private String packageName = "com.worm";
    /**
     * 模块名 默认 core
     */
    private String moduleName = "core";
    /**
     * 表前缀
     */
    private String tablePrefix = "base_";
    /**
     * 表名,如果不输入就是所有表
     */
    private List<String> includeTableNames;

    /**
     * 要排除的表结构
     */
    private List<String> excludeTableNames;
    /**
     * 时间类型
     */
    private EnumDateType dateType = EnumDateType.LOCAL_DATE;
    /**
     * 删除字段
     */
    private String columnDeleted = "deleted";
    /**
     * 创建人字段
     */
    private String columnCreatedBy = "created_by";
    /**
     * 修改人字段
     */
    private String columnUpdatedBy = "updated_by";
    /**
     * 创建时间字段
     */
    private String columnCreatedTime="created_time";
    /**
     * 修改时间字段
     */
    private String columnUpdatedTime="updated_time";

}
