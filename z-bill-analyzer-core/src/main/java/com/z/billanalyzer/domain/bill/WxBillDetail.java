package com.z.billanalyzer.domain.bill;

import com.z.billanalyzer.enums.BankEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 微信账单明细
 *
 * @author zzz
 * @since 2025/3/16 19:12
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class WxBillDetail extends BaseBillDetail {
    /**
     * 商品
     */
    private String product;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 交易单号
     */
    private String transactionNo;

    /**
     * 商户单号
     */
    private String merchantNo;

    /**
     * 支付方式的银行
     */
    private BankEnum paymentModeBank;

    /**
     * 交易状态
     */
    private String transactionStatus;
}
