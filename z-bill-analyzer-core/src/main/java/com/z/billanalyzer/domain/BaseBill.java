package com.z.billanalyzer.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author z-latiao
 * @since 2025/2/26 15:04
 */
@Data
@Accessors(chain = true)
public class BaseBill<T extends BaseBillDetail> {
    /**
     * 文件名
     */
    private String fileName;

    /**
     * 起始时间
     */
    private LocalDateTime startTime;

    /**
     * 终止时间
     */
    private LocalDateTime endTime;

    /**
     * 导出时间
     */
    private LocalDateTime exportTime;

    /**
     * 账单明细
     */
    private List<T> billDetails;

    /**
     * 是否银行账单
     */
    private Boolean isBankBill;
}
