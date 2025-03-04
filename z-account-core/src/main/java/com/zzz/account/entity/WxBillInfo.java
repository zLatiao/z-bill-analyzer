package com.zzz.account.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author z-latiao
 * @since 2025/2/26 15:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class WxBillInfo extends BaseBillInfo {
    /**
     * 微信昵称
     */
    private String wechatNickname;

    /**
     * 导出类型
     */
    private String exportType;

    /**
     * 总交易数量
     */
    private Integer transactionCount;

    /**
     * 收入数量
     */
    private Integer incomeCount;

    /**
     * 收入金额
     */
    private BigDecimal incomeAmount;

    /**
     * 支出数量
     */
    private Integer expenseCount;

    /**
     * 支出金额
     */
    private BigDecimal expenseAmount;

    /**
     * 中性交易数量
     */
    private Integer neutralCount;

    /**
     * 中性交易金额
     */
    private BigDecimal neutralAmount;
}