package com.z.bill.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author z-latiao
 * @since 2025/2/26 15:10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AlipayBillInfo extends BaseBillInfo {

    /**
     * 用户真实姓名
     */
    private String name;

    /**
     * 支付宝账号
     */
    private String alipayAccount;

    /**
     * 导出类型
     */
    private String exportType;

    /**
     * 总交易数量
     */
    private Integer transactionCount;

    /**
     * 收入交易数量
     */
    private Integer incomeCount;

    /**
     * 收入总金额
     */
    private BigDecimal incomeAmount;

    /**
     * 支出交易数量
     */
    private Integer expenseCount;

    /**
     * 支出总金额
     */
    private BigDecimal expenseAmount;

    /**
     * 不计收支的交易数量
     */
    private Integer neutralCount;

    /**
     * 不计收支的总金额
     */
    private BigDecimal neutralAmount;
}