package com.z.billanalyzer.domain.vo;

import com.z.billanalyzer.domain.BillDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailVO extends BillDetail {


    /**
     * 金额类型
     */
    private String amountTypeStr;

    /**
     * 账单来源
     */
    private String sourceStr;
}
