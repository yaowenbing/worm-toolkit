package com.worm.toolkit.gen;

import com.baomidou.mybatisplus.annotation.DbType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MybatisPlusGenProperties} 代码生成器配置测试（默认值 12 项基准锁定 / getJdbcUrl 连接参数拼接格式）。
 *
 * @author worm
 */
class MybatisPlusGenPropertiesTest {

    /**
     * 为什么：MybatisPlusGenProperties 由 4.1 迁移至新模块，需锁定 12 项字段默认值基准，防止迁移过程中默认行为漂移
     * —— 干什么：new MybatisPlusGenProperties() 后逐字段断言 enable（经 isEnable）、dbType、author、packageName、moduleName、tablePrefix、dateType 及 5 个审计字段名的默认值
     */
    @Test
    void defaultsMatchSpecifiedValues() {
        MybatisPlusGenProperties properties = new MybatisPlusGenProperties();
        assertTrue(properties.isEnable());
        assertEquals(DbType.MYSQL, properties.getDbType());
        assertEquals("姚文兵[worm][liuhong89671124@gmail.com]", properties.getAuthor());
        assertEquals("com.worm", properties.getPackageName());
        assertEquals("core", properties.getModuleName());
        assertEquals("base_", properties.getTablePrefix());
        assertEquals(EnumDateType.LOCAL_DATE, properties.getDateType());
        assertEquals("deleted", properties.getColumnDeleted());
        assertEquals("created_by", properties.getColumnCreatedBy());
        assertEquals("updated_by", properties.getColumnUpdatedBy());
        assertEquals("created_time", properties.getColumnCreatedTime());
        assertEquals("updated_time", properties.getColumnUpdatedTime());
    }

    /**
     * 为什么：getJdbcUrl 承担 host/port/dbName 到 MySQL 连接串的唯一定制格式（含 characterEncoding/useSSL/serverTimezone/rewriteBatchedStatements/allowPublicKeyRetrieval 五个固定参数），需以整串断言锁定模板
     * —— 干什么：以 host=127.0.0.1、port=3306、dbName=demo 构造 MybatisPlusGenUtils（4.3 产物构造器注入），断言 getJdbcUrl() 整串等于模板代入值；getJdbcUrl 当前为 private，本用例 RED（见 task-4.6 RED 取证），plan 4.7 将其调整为包内可见后转绿
     */
    @Test
    void getJdbcUrlFormatsConnectionParams() {
        MybatisPlusGenProperties properties = new MybatisPlusGenProperties();
        properties.setHost("127.0.0.1");
        properties.setPort(3306);
        properties.setDbName("demo");
        String jdbcUrl = new MybatisPlusGenUtils(properties).getJdbcUrl();
        assertEquals(
                "jdbc:mysql://127.0.0.1:3306/demo?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true",
                jdbcUrl);
    }
}
