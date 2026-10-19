# worm-toolkit

共享工具集（开源版）：Hutool 基座封装、拼音处理、HTML 转义与净化、JWT、路径匹配，以及 MyBatis-Plus 代码生成器等通用工具能力。

Maven 坐标：

```xml
<dependency>
    <groupId>com.worm</groupId>
    <artifactId>worm-toolkit</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 简介

worm-toolkit 是一个自包含的 Java 工具集模块：无 parent POM、无任何 Spring 依赖，编译基线为 JDK 8，可独立构建、独立引用。模块由三部分组成：

- `com.worm.toolkit.util`：5 个静态工具类（XSS 净化、拼音转换、时区时间、Ant 路径匹配、JWT）。
- `com.worm.toolkit.core`：9 个共享模型与契约（分页入参/出参、用户上下文、中文姓名 DTO、参数位置枚举与常量、请求头常量、多租户接口）。
- `com.worm.toolkit.gen`：基于 MyBatis-Plus Generator 的代码生成器，内置实体模板 `entity2.java.ftl`，支持逻辑删除、乐观锁、审计字段填充与加密字段生成期识别。

## 特性

- **零容器依赖**：不依赖 Spring、Servlet 及任何容器运行时，任何 JDK 8+ 项目直接引用即可使用。
- **XSS 净化**：jsoup + OWASP java-html-sanitizer 双重白名单净化富文本，移除脚本向量并保留基础排版标签。
- **拼音处理**：pinyin4j 实现汉字转无声调小写全拼与首字母缩写，多音字以词级映射兜底选音。
- **时区时间**：继承 Hutool `LocalDateTimeUtil` 全部静态能力，并提供 Asia/Shanghai 当前时间快捷方法。
- **路径匹配**：Ant 风格路径匹配（大小写不敏感），支持 RESTful 占位符与表单参数提取。
- **JWT 签发与校验**：基于 Hutool JWT，payload 仅含 `sub`/`iat`/`exp`，校验同时覆盖签名与过期。
- **代码生成器**：MySQL 表转 Lombok 链式实体、Mapper 与 Service（Controller 关闭），实体模板自动识别备注含「加密」的字段。

## 环境要求

- JDK 8 及以上
- Maven 3.6 及以上
- 运行代码生成器时需可连通的 MySQL 数据库；驱动 `com.mysql:mysql-connector-j` 为 provided 作用域，由使用方自行引入

## 工具类用法

包 `com.worm.toolkit.util` 下的 5 个工具类均以静态方法调用（多数经 Lombok `@UtilityClass` 消除样板，EscapeUtils 为普通静态方法类）。

### EscapeUtils（XSS 净化）

```java
public static String sanitize(String richText)
```

先以 jsoup 解析并整体移除 `script`、`iframe`、`frame`、`frameset`、`applet`、`embed`、`object`、`noscript`、`noframes`、`style` 等高危元素子树，再经 OWASP 白名单策略做两轮净化：白名单外标签被移除；属性仅放行 `img`（src/alt/width/height）与 `a`（href）；URL 协议仅允许 `http`/`https`/`data`，因此 `javascript:` 伪协议与 `on*` 事件属性均被移除；`<a>`、`<div>`、`<b>` 等白名单元素保留（含无属性元素）。

```java
String input = "<a href=\"javascript:alert(1)\" onclick=\"evil()\">链接</a><b>加粗</b>";
String clean = EscapeUtils.sanitize(input);
// href 与 onclick 属性被移除，<a> 与 <b> 标签保留
```

### LetterUtils（拼音与索引和值）

```java
public static int getIndexSum(String projectName)
public static String getPinyin(String text)
public static String getFirstLetter(String text)
```

- `getIndexSum`：对入参的 UTF-8 字节序列逐字节求和，并叠加内部固定基数，返回 int 和值。
- `getPinyin`：汉字转无声调小写全拼（如 `重庆` 转为 `chongqing`），非汉字字符原样保留，入参为 null 或空串时原样返回。
- `getFirstLetter`：取各汉字拼音首字母拼接（如 `重庆` 转为 `cq`），规则同上。
- 多音字：内置词级映射表兜底字级默认读音（当前含 `重庆` 一项）。

```java
LetterUtils.getPinyin("重庆");
// chongqing
LetterUtils.getFirstLetter("重庆");
// cq
```

### LocalDateTimeUtils（时区时间）

```java
public static LocalDateTime ofCtt()
```

类继承 Hutool `cn.hutool.core.date.LocalDateTimeUtil`，可使用其全部静态方法；`ofCtt()` 返回 CTT（经 `ZoneId.SHORT_IDS` 解析为 Asia/Shanghai）时区的当前时间。

```java
import java.time.LocalDateTime;

LocalDateTime now = LocalDateTimeUtils.ofCtt();
```

### AntPathMatcherUtils（路径匹配）

```java
public static boolean match(String regularPath, String targetPath)
public static Boolean isRestful(String uri)
public static List<String> getRestfulArgs(String uri)
public static List<String> getFormArgs(String uri)
```

- `match`：Ant 风格匹配，大小写不敏感（`setCaseSensitive(false)`）且忽略路径分段空白（`setTrimTokens(true)`）。
- `getRestfulArgs`：提取路径中 `{xxx}` 占位符名，如 `/user/{id}` 返回含 `id` 的列表。
- `isRestful`：路径含 RESTful 占位符时返回 true。
- `getFormArgs`：提取形如 `xxx={yyy}` 片段中的参数名 `yyy`。

```java
boolean ok = AntPathMatcherUtils.match("/API/User/**", "/api/user/list");
List<String> args = AntPathMatcherUtils.getRestfulArgs("/user/{id}/order/{orderId}");
```

### JwtUtils（JWT）

```java
public static String createToken(String key, String subject, Duration expiration)
public static JWT verifyToken(String key, String token)
```

- `createToken`：payload 仅含 `sub`（subject）、`iat`（签发时间）、`exp`（过期时间），以入参 key 的 UTF-8 字节作为签名密钥。
- `verifyToken`：内部以 `validate(0)` 同时校验签名与过期时间（`exp`/`nbf`），签名无效或 Token 过期统一抛 `cn.hutool.jwt.JWTException`；校验通过后可用 `jwt.getPayload("sub")` 取回 subject。

```java
import cn.hutool.jwt.JWT;

import java.time.Duration;

String token = JwtUtils.createToken("my-secret-key", "alice", Duration.ofHours(2));
JWT jwt = JwtUtils.verifyToken("my-secret-key", token);
String subject = jwt.getPayload("sub");
```

## 生成器快速开始

1. 在项目根目录创建 `datasource.setting`（与示例类 `com.worm.toolkit.gen.example.GenDbCode` 注释一致），键名如下：

```properties
dbName=worm_db
host=127.0.0.1
port=3306
uName=root
pwd=your_password
packageName=com.example.demo
```

2. 调用生成入口：

```java
import cn.hutool.setting.Setting;
import com.worm.toolkit.gen.MybatisPlusGenUtils;

import java.util.Arrays;

MybatisPlusGenUtils.rebuild(new Setting("datasource.setting"), Arrays.asList("user"));
```

行为说明（与 `MybatisPlusGenUtils` 实现一致）：

- `rebuild(Setting, List<String>)` 会将 `enable` 自动置为 `true`，并按上述键名读取数据库连接与包名；也支持直接构造 `MybatisPlusGenProperties` 后调用 `rebuild(props)`（该重载要求 `enable` 为 true 才执行）。
- 实体等 Java 代码输出至 `{项目根}/src/main/java`；Mapper XML 输出至 `{项目根}/src/main/resources/mapper/core`。
- 实体使用内置模板 `/templates/entity2.java`（源文件 `src/main/resources/templates/entity2.java.ftl`）；Controller 生成被关闭。
- 实体固定启用 Lombok、链式模型与 `@TableField` 注解，主键 `IdType.ASSIGN_ID`，乐观锁列 `version`，逻辑删除列取配置项 `columnDeleted`（默认 `deleted`），并按配置注册审计字段自动填充（deleted/created_by/created_time 为 INSERT 填充，updated_by/updated_time 为 INSERT_UPDATE 填充）。
- Mapper 启用 `BaseResultMap` 与 `BaseColumnList`。
- 类型转换：`decimal` 列映射 `BigDecimal`，`blob` 列映射 `byte[]`，`json` 列映射 `String`；当 `dateType` 为 `DATE` 时 `datetime` 列映射 `java.util.Date`。

## 配置项

`com.worm.toolkit.gen.MybatisPlusGenProperties` 全部 21 个字段（默认值与源码一致）：

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| enable | boolean | true | 代码生成器开关，为 true 时 rebuild 才执行 |
| dbType | DbType | MYSQL | 数据库类型 |
| driverName | String | com.mysql.cj.jdbc.Driver | JDBC 驱动类名 |
| author | String | 姚文兵[worm][liuhong89671124@gmail.com] | 生成代码的作者注释 |
| host | String | 无 | 数据库主机 |
| port | int | 0（未显式设置） | 数据库端口 |
| dbName | String | 无 | 数据库名称 |
| uname | String | 无 | 数据库用户名 |
| pwd | String | 无 | 数据库密码 |
| outputPath | String | . | 生成文件所在目录，相对当前项目根目录 |
| packageName | String | com.worm | 生成代码的包名 |
| moduleName | String | core | 模块名 |
| tablePrefix | String | base_ | 表前缀，生成时剔除 |
| includeTableNames | List\<String\> | 无 | 要生成的表名列表；不填则处理所有表 |
| excludeTableNames | List\<String\> | 无 | 要排除的表 |
| dateType | EnumDateType | LOCAL_DATE | 时间类型策略（可取 DATE、LOCAL_DATE） |
| columnDeleted | String | deleted | 删除（逻辑删除）字段 |
| columnCreatedBy | String | created_by | 创建人字段 |
| columnUpdatedBy | String | updated_by | 修改人字段 |
| columnCreatedTime | String | created_time | 创建时间字段 |
| columnUpdatedTime | String | updated_time | 修改时间字段 |

## 加密字段识别

实体模板 `src/main/resources/templates/entity2.java.ftl` 在生成期扫描字段备注（COMMENT），备注中包含「加密」二字即识别为加密字段（不区分位置），并自动完成以下三件事：

1. 添加导入语句 `import com.worm.mybatis.encrypted.EncryptedTypeHandler;`
2. 在 `@TableName` 中追加 `autoResultMap = true`；
3. 为加密字段标注 `@TableField(value = "列名", typeHandler = EncryptedTypeHandler.class)`。

识别规则示例：

| 字段备注 | 是否识别为加密字段 |
| --- | --- |
| 手机号（加密） | 是 |
| 身份证号（加密存储） | 是 |
| 地址（需要加密） | 是 |
| 加密字段 | 是 |
| 用户名 | 否 |
| 邮箱地址 | 否 |

建表示例：

```sql
CREATE TABLE `user` (
  `id` bigint NOT NULL,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `phone` varchar(255) DEFAULT NULL COMMENT '手机号（加密）',
  `id_card` varchar(255) DEFAULT NULL COMMENT '身份证号（加密存储）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

生成的实体（节选）：

```java
@TableName(value = "user", autoResultMap = true)
public class User implements Serializable {

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号（加密）
     */
    @TableField(value = "phone", typeHandler = EncryptedTypeHandler.class)
    private String phone;
}
```

注意事项：

1. 加密后数据长度增加，加密字段建议使用 `varchar(255)` 或 `text` 类型。
2. 加密密钥必须为 16 字节，建议通过配置中心管理。
3. 加密字段无法直接使用 `=`、`LIKE` 等条件查询。
4. 表中已有历史数据时，启用加密后需要迁移历史数据。
5. 建议团队统一使用「加密」备注标识，便于识别和维护。

本模块仅负责生成期识别；加解密的运行时能力由消费方引入的 `mybatisPlus-spring-boot-starter` 提供，通过 `worm.mybatis.plus.encrypt` 配置启用：

```yaml
worm:
  mybatis:
    plus:
      encrypt:
        enable: true
        secret-key: your-16-byte-key
```

## 架构

包与文件结构：

```text
src/main/java/com/worm/toolkit/
|-- util/                              工具类（5 个）
|   |-- EscapeUtils                    XSS 富文本净化（jsoup + OWASP）
|   |-- LetterUtils                    拼音转换与索引和值（pinyin4j）
|   |-- LocalDateTimeUtils             时区时间，继承 Hutool LocalDateTimeUtil
|   |-- AntPathMatcherUtils            Ant 路径匹配与参数提取
|   `-- JwtUtils                       JWT 签发与校验（Hutool JWT）
|-- core/                              共享模型与契约（9 个）
|   |-- PageDto / PageVo               分页入参 / 分页出参（PageVo 继承 MyBatis-Plus Page）
|   |-- UserContext                    用户上下文（Redis 与 SecurityContext 的统一载体，实现 TenantDetails）
|   |-- ChineseNameDto                 中文姓名及拼音 DTO
|   |-- EnumParamType / ConsParamType  参数位置枚举与常量（body/path/query/form/header）
|   |-- ConsHeader                     请求头常量（X-Username）
|   `-- TenantDetails / TenantIdSupplier  多租户契约接口
`-- gen/                               MyBatis-Plus 代码生成器（3 类 + 示例）
    |-- MybatisPlusGenUtils            生成入口（rebuild 静态方法 + execute）
    |-- MybatisPlusGenProperties       生成器配置（21 个字段）
    |-- EnumDateType                   时间类型策略枚举（DATE / LOCAL_DATE）
    `-- example.GenDbCode              代码生成入口示例
src/main/resources/templates/
`-- entity2.java.ftl                   实体模板（加密字段识别）
```

依赖清单（版本在本模块 pom 中自包含写死，无 parent BOM）：

| 依赖 | 版本 | 用途与作用域 |
| --- | --- | --- |
| cn.hutool:hutool-all | 5.8.25 | 基座工具（JWT、Setting、AntPathMatcher、日期等），compile |
| com.belerweb:pinyin4j | 2.5.1 | 拼音转换，compile |
| org.jsoup:jsoup | 1.17.2 | HTML 解析，compile |
| com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer | 20240325.1 | HTML 白名单净化，compile |
| io.swagger.core.v3:swagger-annotations-jakarta | 2.2.21 | OpenAPI v3 @Schema 注解（纯注解包），compile |
| com.baomidou:mybatis-plus-annotation | 3.5.16 | MP 注解，compile |
| com.baomidou:mybatis-plus-extension | 3.5.16 | PageVo 编译依赖 Page，compile |
| com.baomidou:mybatis-plus-generator | 3.5.16 | 代码生成器，compile |
| org.freemarker:freemarker | 2.3.34 | 实体模板引擎，compile |
| org.projectlombok:lombok | 1.18.34 | 编译期注解处理器 |
| org.slf4j:slf4j-api | 2.0.17 | 日志门面，compile |
| com.mysql:mysql-connector-j | 9.7.0 | 生成器直连 MySQL 使用，provided |
| org.junit.jupiter:junit-jupiter | 5.10.2 | 单元测试，test |

除上述清单外，本模块零 Spring 依赖、零 Servlet 依赖、零容器运行时依赖。

## FAQ

1. 问：零容器依赖的模块如何独立使用？
   答：本模块无 parent、无 Spring 依赖，任何 JDK 8+ 项目引入 Maven 坐标即可调用；日志仅依赖 slf4j-api 门面，运行时由使用方绑定具体实现（如 logback）。

2. 问：为什么显式声明 mybatis-plus-annotation 与 mybatis-plus-extension？
   答：mybatis-plus-generator 3.5.16 对这两个依赖的 scope 为 provided，不会传递到消费方；而共享模型 PageVo 编译需要 Page、实体契约需要 MP 注解，故在本模块显式声明（经 dependency:tree 实证），保证依赖完整传递。

3. 问：旧坐标 worm-toolkit-hutool / worm-toolkit-mybatisplusGen 如何迁移？
   答：Maven 坐标统一为 `com.worm:worm-toolkit`。包名迁移映射：`com.worm.hutool.*`（原 5 个工具类）迁移至 `com.worm.toolkit.util`；原共享模型迁移至 `com.worm.toolkit.core`；`com.worm.MybatisPlusGenUtils`、`com.worm.MybatisPlusGenProperties`、`com.worm.EnumDateType` 迁移至 `com.worm.toolkit.gen`。

4. 问：加密字段有什么使用限制？
   答：密钥必须 16 字节；字段建议 `varchar(255)` 或 `text`；加密字段无法直接使用 `=`、`LIKE` 等条件查询；启用加密后需迁移历史数据。详见「加密字段识别」一节。

5. 问：JDK 基线与测试如何？
   答：编译源码级别与字节码目标均为 Java 8（pom 中 `maven.compiler.source/target` 为 8）。单元测试基于 JUnit 5（5.10.2），在模块目录执行 `mvn test` 运行，覆盖 5 个工具类与生成器配置类。

## 许可证

本项目基于 Apache License 2.0 开源，完整协议文本见 [LICENSE](LICENSE)。

```text
Copyright 2026 姚文兵
```
