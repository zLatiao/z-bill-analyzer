package com.z.billanalyzer.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailVO {

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 金额类型:0.收入,1.支出,2.不计收支
     */
    private Integer amountType;

    /**
     * 交易类型
     */
    private String transactionType;

    /**
     * 来源:0.微信,1.支付宝,2.招商银行
     */
    private Integer source;

    /**
     * 交易时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionTime;

    /**
     * 交易对方
     */
    private String counterparty;

    /**
     * 商品
     */
    private String product;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 交易状态
     */
    private String transactionStatus;

    /**
     * 账单单号
     */
    private String transactionNo;

    /**
     * 商家单号
     */
    private String merchantNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 招商银行还有这个余额
     */
    private BigDecimal balance;

    /**
     * 金额类型
     */
    private String amountTypeStr;

    /**
     * 账单来源
     */
    private String sourceStr;
}
