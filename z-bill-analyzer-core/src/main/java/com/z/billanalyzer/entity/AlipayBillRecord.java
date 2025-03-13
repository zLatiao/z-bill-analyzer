package com.z.billanalyzer.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付宝账单
 *
 * @author zzz
 * @since 2024/5/23 23:40
 */
@Setter
@Getter
@ToString
public class AlipayBillRecord {
    // 交易时间
    @ExcelProperty(value = "交易时间")
    private LocalDateTime transactionTime;

    // 交易分类
    @ExcelProperty(value = "交易分类")
    private String transactionCategory;

    // 交易对方
    @ExcelProperty(value = "交易对方")
    private String counterparty;

    // 对方账号
    @ExcelProperty(value = "对方账号")
    private String counterpartyAccount;

    // 商品说明
    @ExcelProperty(value = "商品说明")
    private String product;

    // 收/支
    @ExcelProperty(value = "收/支")
    private String incomeOrExpense;

    // 金额
    @ExcelProperty(value = "金额")
    private BigDecimal amount; // 使用BigDecimal来处理精确的小数计算

    // 收/付款方式（这里假设使用字符串）
    @ExcelProperty(value = "收/付款方式")
    private String paymentMethod;

    // 交易状态
    @ExcelProperty(value = "交易状态")
    private String transactionStatus;

    // 交易订单号
    @ExcelProperty(value = "交易订单号")
    private String transactionOrderId;

    // 商家订单号
    @ExcelProperty(value = "商家订单号")
    private String merchantOrderId;

    // 备注
    @ExcelProperty(value = "备注")
    private String remark;
}