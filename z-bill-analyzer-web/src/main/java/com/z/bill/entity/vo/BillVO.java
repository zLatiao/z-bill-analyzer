package com.z.bill.entity.vo;

import com.z.bill.entity.Bill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
