package com.z.bill.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 招商银行账单
 *
 * @author zzz
 * @since 2024/5/24 0:11
 */
@Setter
@Getter
public class CmbBillRecord {
    // 交易日期
    @ExcelProperty(value = "交易日期")
    private String date;

    // 交易时间
    @ExcelProperty(value = "交易时间")
    private String time;

    // 收入
    @ExcelProperty(value = "收入")
    private BigDecimal income;

    // 支出
    @ExcelProperty(value = "支出")
    private BigDecimal expense;

    // 余额
    @ExcelProperty(value = "余额")
    private BigDecimal balance;

    // 交易类型
    @ExcelProperty(value = "交易类型")
    private String transactionType;

    // 交易备注
    @ExcelProperty(value = "交易备注")
    private String remark;
}