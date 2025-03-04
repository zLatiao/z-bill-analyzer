package com.zzz.account.entity.vo;

import com.zzz.account.entity.Bill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillVO extends Bill {


    /**
     * 金额类型
     */
    private String amountTypeStr;

    /**
     * 账单来源
     */
    private String sourceStr;
}
