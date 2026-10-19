package com.worm.toolkit.gen.example;

import cn.hutool.setting.Setting;
import com.worm.toolkit.gen.MybatisPlusGenUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 代码生成入口示例
 * <p>
 * 使用前需在项目根目录创建 datasource.setting 文件，包含数据库连接信息：
 * <pre>
 * host=127.0.0.1
 * port=3306
 * dbName=your_database
 * uName=root
 * pwd=your_password
 * packageName=com.example.demo
 * </pre>
 * </p>
 *
 * @author worm
 */
public class GenDbCode {
    public static void main(String[] args) {
        // 表列表（按需修改）
        List<String> includes = Arrays.asList(
                "your_table_name"
        );
        MybatisPlusGenUtils.rebuild(new Setting("datasource.setting"), includes);
    }
}
