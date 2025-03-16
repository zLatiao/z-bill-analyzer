package com.z.billanalyzer.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 招商银行账单明细
 *
 * @author zzz
 * @since 2025/3/16 19:15
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class CmbBillDetail extends BaseBillDetail {
    /**
     * 个余额
     */
    private BigDecimal balance;
}
