package com.z.billanalyzer.entity.vo;

/**
 * 支出来源数据视图
 *
 * @param name  支出分类名称（如：餐饮、住房）
 * @param value 分类金额（单位：元）
 */
public record ExpenseSourceVO(String name, Number value) {
}
