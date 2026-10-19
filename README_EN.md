# worm-toolkit

Shared utility toolkit (open-source edition): Hutool-based foundation wrappers, pinyin processing, HTML escaping and sanitization, JWT, path matching, a MyBatis-Plus code generator, and other general-purpose utility capabilities.

Maven coordinates:

```xml
<dependency>
    <groupId>com.worm</groupId>
    <artifactId>worm-toolkit</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Introduction

worm-toolkit is a self-contained Java utility module: no parent POM and no Spring dependencies at all, compiled against JDK 8, buildable and consumable independently. The module consists of three parts:

- `com.worm.toolkit.util`: 5 static utility classes (XSS sanitization, pinyin conversion, timezone-aware time, Ant path matching, JWT).
- `com.worm.toolkit.core`: 9 shared models and contracts (pagination input/output, user context, Chinese name DTO, parameter-location enum and constants, request-header constants, multi-tenancy interfaces).
- `com.worm.toolkit.gen`: a code generator based on MyBatis-Plus Generator, shipped with the entity template `entity2.java.ftl`, supporting logical delete, optimistic locking, audit-field filling, and build-time recognition of encrypted fields.

## Features

- **Zero container dependencies**: no Spring, Servlet, or any container runtime; any JDK 8+ project can consume it directly.
- **XSS sanitization**: jsoup plus OWASP java-html-sanitizer apply dual whitelist sanitization to rich text, removing script vectors while keeping basic formatting tags.
- **Pinyin processing**: pinyin4j converts Chinese characters to toneless lowercase full pinyin and to initial-letter abbreviations, with word-level mappings as the fallback for polyphonic characters.
- **Timezone time**: inherits all static capabilities of Hutool `LocalDateTimeUtil` and provides a shortcut method for the current Asia/Shanghai time.
- **Path matching**: Ant-style path matching (case-insensitive), supporting RESTful placeholder and form-parameter extraction.
- **JWT issuing and verification**: built on Hutool JWT; the payload contains only `sub`/`iat`/`exp`, and verification covers both the signature and the expiry.
- **Code generator**: turns MySQL tables into Lombok chained entities, Mappers, and Services (Controller generation disabled); the entity template automatically recognizes fields whose comments contain 加密 ("encrypted").

## Requirements

- JDK 8 or above
- Maven 3.6 or above
- A reachable MySQL database when running the code generator; the driver `com.mysql:mysql-connector-j` is provided-scope and must be introduced by the consumer

## Utility Classes

The 5 utility classes under package `com.worm.toolkit.util` are all invoked through static methods (most of them eliminate boilerplate via Lombok `@UtilityClass`; EscapeUtils is a plain static-method class).

### EscapeUtils (XSS Sanitization)

```java
public static String sanitize(String richText)
```

It first parses the input with jsoup and removes whole high-risk element subtrees such as `script`, `iframe`, `frame`, `frameset`, `applet`, `embed`, `object`, `noscript`, `noframes`, and `style`; the content then goes through two rounds of OWASP whitelist sanitization: tags outside the whitelist are removed; attributes are allowed only on `img` (src/alt/width/height) and `a` (href); URL protocols are restricted to `http`/`https`/`data`, so `javascript:` pseudo-protocols and `on*` event attributes are all removed; whitelisted elements such as `<a>`, `<div>`, and `<b>` are preserved (attribute-less elements included).

```java
String input = "<a href=\"javascript:alert(1)\" onclick=\"evil()\">链接</a><b>加粗</b>";
String clean = EscapeUtils.sanitize(input);
// href 与 onclick 属性被移除，<a> 与 <b> 标签保留
```

### LetterUtils (Pinyin and Index Sum)

```java
public static int getIndexSum(String projectName)
public static String getPinyin(String text)
public static String getFirstLetter(String text)
```

- `getIndexSum`: sums the UTF-8 byte sequence of the input byte by byte and adds a fixed internal base value, returning the int sum.
- `getPinyin`: converts Chinese characters to toneless lowercase full pinyin (e.g. `重庆` becomes `chongqing`); non-Chinese characters are kept as-is, and a null or empty input is returned as-is.
- `getFirstLetter`: concatenates the first letters of each character's pinyin (e.g. `重庆` becomes `cq`), following the same rules as above.
- Polyphonic characters: a built-in word-level mapping table falls back to the character-level default reading (currently containing `重庆` only).

```java
LetterUtils.getPinyin("重庆");
// chongqing
LetterUtils.getFirstLetter("重庆");
// cq
```

### LocalDateTimeUtils (Timezone Time)

```java
public static LocalDateTime ofCtt()
```

The class extends Hutool `cn.hutool.core.date.LocalDateTimeUtil`, so all of its static methods are available; `ofCtt()` returns the current time in the CTT timezone (resolved via `ZoneId.SHORT_IDS` to Asia/Shanghai).

```java
import java.time.LocalDateTime;

LocalDateTime now = LocalDateTimeUtils.ofCtt();
```

### AntPathMatcherUtils (Path Matching)

```java
public static boolean match(String regularPath, String targetPath)
public static Boolean isRestful(String uri)
public static List<String> getRestfulArgs(String uri)
public static List<String> getFormArgs(String uri)
```

- `match`: Ant-style matching, case-insensitive (`setCaseSensitive(false)`) and ignoring whitespace within path segments (`setTrimTokens(true)`).
- `getRestfulArgs`: extracts the `{xxx}` placeholder names from the path, e.g. `/user/{id}` returns a list containing `id`.
- `isRestful`: returns true when the path contains RESTful placeholders.
- `getFormArgs`: extracts the parameter name `yyy` from fragments of the form `xxx={yyy}`.

```java
boolean ok = AntPathMatcherUtils.match("/API/User/**", "/api/user/list");
List<String> args = AntPathMatcherUtils.getRestfulArgs("/user/{id}/order/{orderId}");
```

### JwtUtils (JWT)

```java
public static String createToken(String key, String subject, Duration expiration)
public static JWT verifyToken(String key, String token)
```

- `createToken`: the payload contains only `sub` (subject), `iat` (issue time), and `exp` (expiration time); the UTF-8 bytes of the given key serve as the signing secret.
- `verifyToken`: internally calls `validate(0)` to verify both the signature and the expiry (`exp`/`nbf`); an invalid signature or an expired token uniformly throws `cn.hutool.jwt.JWTException`; after a successful verification, the subject can be retrieved with `jwt.getPayload("sub")`.

```java
import cn.hutool.jwt.JWT;

import java.time.Duration;

String token = JwtUtils.createToken("my-secret-key", "alice", Duration.ofHours(2));
JWT jwt = JwtUtils.verifyToken("my-secret-key", token);
String subject = jwt.getPayload("sub");
```

## Code Generator Quick Start

1. Create `datasource.setting` in the project root directory (consistent with the comments of the sample class `com.worm.toolkit.gen.example.GenDbCode`), using the following keys:

```properties
dbName=worm_db
host=127.0.0.1
port=3306
uName=root
pwd=your_password
packageName=com.example.demo
```

2. Invoke the generation entry point:

```java
import cn.hutool.setting.Setting;
import com.worm.toolkit.gen.MybatisPlusGenUtils;

import java.util.Arrays;

MybatisPlusGenUtils.rebuild(new Setting("datasource.setting"), Arrays.asList("user"));
```

Behavior notes (consistent with the `MybatisPlusGenUtils` implementation):

- `rebuild(Setting, List<String>)` automatically sets `enable` to `true` and reads the database connection and package name with the keys above; alternatively, you can build `MybatisPlusGenProperties` directly and call `rebuild(props)` (that overload executes only when `enable` is true).
- Java code such as entities is written to `{project root}/src/main/java`; Mapper XML files are written to `{project root}/src/main/resources/mapper/core`.
- Entities use the built-in template `/templates/entity2.java` (source file `src/main/resources/templates/entity2.java.ftl`); Controller generation is disabled.
- Entities always enable Lombok, the chain model, and `@TableField` annotations; the primary key uses `IdType.ASSIGN_ID`, the optimistic-lock column is `version`, the logical-delete column comes from the `columnDeleted` option (default `deleted`), and audit-field auto-filling is registered per the configuration (deleted/created_by/created_time are filled on INSERT; updated_by/updated_time are filled on INSERT_UPDATE).
- Mappers enable `BaseResultMap` and `BaseColumnList`.
- Type conversions: `decimal` columns map to `BigDecimal`, `blob` columns map to `byte[]`, `json` columns map to `String`; when `dateType` is `DATE`, `datetime` columns map to `java.util.Date`.

## Configuration

All 21 fields of `com.worm.toolkit.gen.MybatisPlusGenProperties` (defaults identical to the source code):

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| enable | boolean | true | Code generator switch; rebuild executes only when true |
| dbType | DbType | MYSQL | Database type |
| driverName | String | com.mysql.cj.jdbc.Driver | JDBC driver class name |
| author | String | 姚文兵[worm][liuhong89671124@gmail.com] | Author comment in generated code |
| host | String | none | Database host |
| port | int | 0 (not explicitly set) | Database port |
| dbName | String | none | Database name |
| uname | String | none | Database username |
| pwd | String | none | Database password |
| outputPath | String | . | Directory where generated files are placed, relative to the current project root |
| packageName | String | com.worm | Package name of the generated code |
| moduleName | String | core | Module name |
| tablePrefix | String | base_ | Table prefix, stripped during generation |
| includeTableNames | List\<String\> | none | List of tables to generate; all tables are processed when empty |
| excludeTableNames | List\<String\> | none | Tables to exclude |
| dateType | EnumDateType | LOCAL_DATE | Time type strategy (DATE or LOCAL_DATE) |
| columnDeleted | String | deleted | Delete (logical delete) column |
| columnCreatedBy | String | created_by | Created-by column |
| columnUpdatedBy | String | updated_by | Updated-by column |
| columnCreatedTime | String | created_time | Created-time column |
| columnUpdatedTime | String | updated_time | Updated-time column |

## Encrypted Field Recognition

During generation, the entity template `src/main/resources/templates/entity2.java.ftl` scans the field comments (COMMENT); any field whose comment contains the two characters 加密 ("encrypted", regardless of position) is recognized as an encrypted field, and the following three things are done automatically:

1. Add the import statement `import com.worm.mybatis.encrypted.EncryptedTypeHandler;`
2. Append `autoResultMap = true` to `@TableName`;
3. Annotate the encrypted field with `@TableField(value = "列名", typeHandler = EncryptedTypeHandler.class)`.

Recognition rule examples:

| Field Comment | Recognized as Encrypted Field |
| --- | --- |
| 手机号（加密） | Yes |
| 身份证号（加密存储） | Yes |
| 地址（需要加密） | Yes |
| 加密字段 | Yes |
| 用户名 | No |
| 邮箱地址 | No |

Table creation example:

```sql
CREATE TABLE `user` (
  `id` bigint NOT NULL,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `phone` varchar(255) DEFAULT NULL COMMENT '手机号（加密）',
  `id_card` varchar(255) DEFAULT NULL COMMENT '身份证号（加密存储）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

Generated entity (excerpt):

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

Notes:

1. Encrypted data is longer than the plaintext; `varchar(255)` or `text` is recommended for encrypted columns.
2. The encryption key must be 16 bytes; managing it through a configuration center is recommended.
3. Encrypted fields cannot be queried directly with conditions such as `=` or `LIKE`.
4. If the table already contains historical data, that data must be migrated after encryption is enabled.
5. Teams are advised to uniformly mark encrypted fields with the 加密 comment for easy recognition and maintenance.

This module only handles build-time recognition; the runtime encryption/decryption capability is provided by the `mybatisPlus-spring-boot-starter` introduced by the consumer and is enabled through the `worm.mybatis.plus.encrypt` configuration:

```yaml
worm:
  mybatis:
    plus:
      encrypt:
        enable: true
        secret-key: your-16-byte-key
```

## Architecture

Package and file structure (annotations in the tree remain in Chinese):

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

Dependency list (versions are self-contained and pinned in this module's pom; no parent BOM):

| Dependency | Version | Purpose and Scope |
| --- | --- | --- |
| cn.hutool:hutool-all | 5.8.25 | Foundation utilities (JWT, Setting, AntPathMatcher, date, etc.), compile |
| com.belerweb:pinyin4j | 2.5.1 | Pinyin conversion, compile |
| org.jsoup:jsoup | 1.17.2 | HTML parsing, compile |
| com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer | 20240325.1 | HTML whitelist sanitization, compile |
| io.swagger.core.v3:swagger-annotations-jakarta | 2.2.21 | OpenAPI v3 @Schema annotations (annotation-only package), compile |
| com.baomidou:mybatis-plus-annotation | 3.5.16 | MP annotations, compile |
| com.baomidou:mybatis-plus-extension | 3.5.16 | Compile-time dependency of PageVo on Page, compile |
| com.baomidou:mybatis-plus-generator | 3.5.16 | Code generator, compile |
| org.freemarker:freemarker | 2.3.34 | Entity template engine, compile |
| org.projectlombok:lombok | 1.18.34 | Compile-time annotation processor |
| org.slf4j:slf4j-api | 2.0.17 | Logging facade, compile |
| com.mysql:mysql-connector-j | 9.7.0 | Used by the generator to connect to MySQL directly, provided |
| org.junit.jupiter:junit-jupiter | 5.10.2 | Unit testing, test |

Beyond the list above, this module has zero Spring dependencies, zero Servlet dependencies, and zero container runtime dependencies.

## FAQ

1. Q: How can a module with zero container dependencies be used standalone?
   A: The module has no parent and no Spring dependencies; any JDK 8+ project only needs the Maven coordinates to call it. Logging relies solely on the slf4j-api facade; the consumer binds a concrete implementation (e.g. logback) at runtime.

2. Q: Why are mybatis-plus-annotation and mybatis-plus-extension declared explicitly?
   A: In mybatis-plus-generator 3.5.16 these two dependencies are provided-scoped and therefore not transitively delivered to consumers; since the shared model PageVo needs Page at compile time and the entity contract needs MP annotations, they are declared explicitly in this module (verified via dependency:tree) to guarantee complete dependency delivery.

3. Q: How do the old coordinates worm-toolkit-hutool / worm-toolkit-mybatisplusGen migrate?
   A: The Maven coordinates are unified as `com.worm:worm-toolkit`. Package mapping: `com.worm.hutool.*` (the original 5 utility classes) moves to `com.worm.toolkit.util`; the original shared models move to `com.worm.toolkit.core`; `com.worm.MybatisPlusGenUtils`, `com.worm.MybatisPlusGenProperties`, and `com.worm.EnumDateType` move to `com.worm.toolkit.gen`.

4. Q: What restrictions apply to encrypted fields?
   A: The key must be 16 bytes; `varchar(255)` or `text` is recommended for the columns; encrypted fields cannot be queried directly with conditions such as `=` or `LIKE`; historical data must be migrated after encryption is enabled. See the "Encrypted Field Recognition" section for details.

5. Q: What about the JDK baseline and testing?
   A: Both the compile source level and the bytecode target are Java 8 (`maven.compiler.source/target` is 8 in the pom). Unit tests are based on JUnit 5 (5.10.2); run `mvn test` in the module directory, covering the 5 utility classes and the generator configuration class.

## License

This project is open-sourced under the Apache License 2.0; see [LICENSE](LICENSE) for the full license text.

```text
Copyright 2026 姚文兵
```
