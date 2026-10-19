package com.worm.toolkit.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author winiger
 */
@Data
@Accessors(chain = true)
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChineseNameDto {
    /**
     * 姓
     */
    private String surname;
    /**
     * 名
     */
    private String subName;
    /**
     * 全名（中文）
     */
    private String fullName;
    /**
     * 姓拼音
     */
    private String surnamePinyin;
    /**
     * 名拼音
     */
    private String subNamePinyin;
    /**
     * 全面拼音
     */
    private String fullNamePinyin;
    /**
     * 1：男性 0 女性
     */
    private int sexCode;
    /**
     * 男 or 女
     */
    private String sex;
    /**
     * nan or nv
     */
    private String sexPinyin;
}
