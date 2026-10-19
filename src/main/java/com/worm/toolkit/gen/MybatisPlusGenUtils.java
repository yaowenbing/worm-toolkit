package com.worm.toolkit.gen;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.setting.Setting;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.baomidou.mybatisplus.generator.fill.Property;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class MybatisPlusGenUtils {
    private static final String DATA_TYPE_DECIMAL = "decimal";

    private static final String DATA_TYPE_BLOB = "blob";

    private static final String DATA_TYPE_JSON = "json";

    private static final String DATA_TYPE_DATETIME = "datetime";

    private MybatisPlusGenProperties mybatisPlusGenProperties;

    public MybatisPlusGenUtils(MybatisPlusGenProperties mybatisPlusGenProperties) {
        // 为什么：@Data 与单例已移除（plan 4.3，零外部消费实证） —— 干什么：以构造器注入替代 setter 注入，实例生命周期由调用方管理
        this.mybatisPlusGenProperties = mybatisPlusGenProperties;
    }

    private String getProjectPath() {
        String projectPath = System.getProperty("user.dir");
        log.debug("项目的默认路径: {}", projectPath);
        if (".".equalsIgnoreCase(mybatisPlusGenProperties.getOutputPath())) {
            ClassLoader classLoader = MybatisPlusGenUtils.class.getClassLoader();
            java.net.URL resource = classLoader.getResource(CharSequenceUtil.EMPTY);
            File classPath;
            if (resource != null) {
                classPath = new File(resource.getPath());
                projectPath = classPath.getParentFile().getParentFile().getAbsolutePath();
                log.debug("目标路径: {}", projectPath);
            } else {
                log.error("未能获取到类的资源路径，使用默认项目路径");
            }
        }
        log.debug("项目路径:{}", projectPath);
        return projectPath;
    }

    String getJdbcUrl() {
        return CharSequenceUtil.format(
                "jdbc:mysql://{}:{}/{}?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true",
                mybatisPlusGenProperties.getHost(),
                mybatisPlusGenProperties.getPort(),
                mybatisPlusGenProperties.getDbName()
        );
    }

    public void execute() {
        String projectPath = getProjectPath();
        String outputDir = CharSequenceUtil.format("{}/src/main/java", projectPath);
        log.info("最终输出路径:{}", outputDir);

        FastAutoGenerator.create(getJdbcUrl(), mybatisPlusGenProperties.getUname(), mybatisPlusGenProperties.getPwd())
                .globalConfig(builder -> builder
                        .author(mybatisPlusGenProperties.getAuthor())
                        .outputDir(outputDir)
                        .disableOpenDir()
                        .dateType(DateType.TIME_PACK)
                        .commentDate("yyyy-MM-dd")
                )
                .dataSourceConfig(builder -> builder
                        .typeConvert((globalConfig, fieldType) -> {
                            String size = ReUtil.get(PatternPool.NUMBERS, fieldType, 0);
                            log.debug("字段类型:{},长度:{}", fieldType, size);
                            if (fieldType.toLowerCase().contains(DATA_TYPE_DECIMAL)) {
                                return com.baomidou.mybatisplus.generator.config.rules.DbColumnType.BIG_DECIMAL;
                            }
                            if (fieldType.toLowerCase().contains(DATA_TYPE_BLOB)) {
                                return com.baomidou.mybatisplus.generator.config.rules.DbColumnType.BYTE_ARRAY;
                            }
                            if (fieldType.toLowerCase().contains(DATA_TYPE_JSON)) {
                                return com.baomidou.mybatisplus.generator.config.rules.DbColumnType.STRING;
                            }
                            EnumDateType enumDateType = mybatisPlusGenProperties.getDateType();
                            if (enumDateType == EnumDateType.DATE && DATA_TYPE_DATETIME.equalsIgnoreCase(fieldType)) {
                                return com.baomidou.mybatisplus.generator.config.rules.DbColumnType.DATE;
                            }
                            return null;
                        })
                )
                .packageConfig(builder -> builder
                        .parent(mybatisPlusGenProperties.getPackageName())
                        .moduleName(mybatisPlusGenProperties.getModuleName())
                        .pathInfo(Collections.singletonMap(OutputFile.xml,
                                CharSequenceUtil.format("{}/src/main/resources/mapper/core", projectPath)))
                )
                .strategyConfig(builder -> {
                    builder.addInclude(getIncludeTables())
                            .addExclude(getExcludeTables())
                            .addTablePrefix(mybatisPlusGenProperties.getTablePrefix());

                    builder.entityBuilder()
                            .enableLombok()
                            .enableChainModel()
                            .enableTableFieldAnnotation()
                            .idType(IdType.ASSIGN_ID)
                            .versionColumnName("version")
                            .logicDeleteColumnName(mybatisPlusGenProperties.getColumnDeleted())
                            .addTableFills(Arrays.asList(
                                    new Column(mybatisPlusGenProperties.getColumnDeleted(), FieldFill.INSERT),
                                    new Column(mybatisPlusGenProperties.getColumnCreatedBy(), FieldFill.INSERT),
                                    new Column(mybatisPlusGenProperties.getColumnCreatedTime(), FieldFill.INSERT),
                                    new Property(mybatisPlusGenProperties.getColumnUpdatedBy(), FieldFill.INSERT_UPDATE),
                                    new Property(mybatisPlusGenProperties.getColumnUpdatedTime(), FieldFill.INSERT_UPDATE)
                            ));

                    builder.controllerBuilder()
                            .disable();

                    builder.mapperBuilder()
                            .enableBaseResultMap()
                            .enableBaseColumnList();
                })
                .templateConfig(builder -> builder
                        .controller(null)
                        .entity("/templates/entity2.java")
                )
                .injectionConfig(builder -> builder
                        .beforeOutputFile((tableInfo, objectMap) ->
                                log.info("准备生成文件: {}", tableInfo.getEntityName()))
                )
                .execute();
    }

    private List<String> getIncludeTables() {
        List<String> includeTableNames = mybatisPlusGenProperties.getIncludeTableNames();
        if (CollUtil.isNotEmpty(includeTableNames)) {
            return includeTableNames;
        }
        return Collections.emptyList();
    }

    private List<String> getExcludeTables() {
        List<String> excludeTableNames = mybatisPlusGenProperties.getExcludeTableNames();
        if (CollUtil.isNotEmpty(excludeTableNames)) {
            return excludeTableNames;
        }
        return Collections.emptyList();
    }

    public static void rebuild(MybatisPlusGenProperties mybatisPlusGenProperties) {
        if (mybatisPlusGenProperties.isEnable()) {
            // 为什么：移除 private static instance 单例（plan 4.3：每次创建新实例，消除共享可变状态） —— 干什么：rebuild 静态入口每次构造新实例后执行，对外方法签名不变
            MybatisPlusGenUtils mybatisPlusGenUtils = new MybatisPlusGenUtils(mybatisPlusGenProperties);
            mybatisPlusGenUtils.execute();
        }
    }

    public static void rebuild(Setting setting, List<String> tables) {
        setting.autoLoad(false);
        MybatisPlusGenProperties mybatisPlusGenProperties = new MybatisPlusGenProperties();
        mybatisPlusGenProperties.setEnable(true);
        mybatisPlusGenProperties.setDbName(setting.getStr("dbName"));
        mybatisPlusGenProperties.setHost(setting.getStr("host"));
        mybatisPlusGenProperties.setPort(setting.getInt("port"));
        mybatisPlusGenProperties.setUname(setting.getStr("uName"));
        mybatisPlusGenProperties.setPwd(setting.getStr("pwd"));
        mybatisPlusGenProperties.setPackageName(setting.getStr("packageName"));
        mybatisPlusGenProperties.setIncludeTableNames(tables);
        rebuild(mybatisPlusGenProperties);
    }
}