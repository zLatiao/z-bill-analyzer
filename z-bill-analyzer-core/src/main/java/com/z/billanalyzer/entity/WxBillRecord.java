package com.z.billanalyzer.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 微信账单
 *
 * @author zzz
 * @since 2024/5/23 13:39
 */
@Setter
@Getter
@ToString
public class WxBillRecord {
    // 交易时间
    @ExcelProperty(value = "交易时间")
    private LocalDateTime transactionTime;

    // 交易类型
    @ExcelProperty(value = "交易类型")
    private String transactionType;

    // 交易对方
    @ExcelProperty(value = "交易对方")
    private String counterparty;

    // 商品
    @ExcelProperty(value = "商品")
    private String product;

    // 收/支
    @ExcelProperty(value = "收/支")
    private String incomeOrExpense;

    // 金额(元)
    @ExcelProperty(value = "金额(元)")
    private String amount;

    // 支付方式
    @ExcelProperty(value = "支付方式")
    private String paymentMethod;

    // 当前状态
    @ExcelProperty(value = "当前状态")
    private String currentStatus;

    // 交易单号
    @ExcelProperty(value = "交易单号")
    private String transactionNo;

    // 商户单号
    @ExcelProperty(value = "商户单号")
    private String merchantNo;

    // 备注
    @ExcelProperty(value = "备注")
    private String remark;
}
