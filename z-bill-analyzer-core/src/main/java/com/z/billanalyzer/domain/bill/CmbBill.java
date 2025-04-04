package com.z.billanalyzer.domain.bill;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 招商银行账单
 *
 * @author z-latiao
 * @since 2025/2/26 15:10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class CmbBill extends BaseBill<CmbBillDetail> {
    /**
     * 账号
     */
    private String bankAccountNumber;

    /**
     * 账号最后四位数
     */
    private String bankAccountLast4Number;

    /**
     * 币种
     */
    private String currency;

    /**
     * 过滤设置
     */
    private String filterSettings;
}
