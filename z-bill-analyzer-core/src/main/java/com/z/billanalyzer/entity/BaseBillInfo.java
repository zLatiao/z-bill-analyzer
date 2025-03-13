package com.z.billanalyzer.entity;

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
public class BaseBillInfo {
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
     * 账单
     */
    private List<Bill> bills;

    /**
     * 是否银行账单
     */
    private Boolean isBankBill;

//    public BaseBillInfo setStartTime(LocalDateTime startTime) {
//        this.startTime = startTime;
//        return this;
//    }
//
//    public BaseBillInfo setEndTime(LocalDateTime endTime) {
//        this.endTime = endTime;
//        return this;
//    }
//
//    public BaseBillInfo setExportTime(LocalDateTime exportTime) {
//        this.exportTime = exportTime;
//        return this;
//    }
//
//    public BaseBillInfo setBills(List<Bill> bills) {
//        this.bills = bills;
//        return this;
//    }
}
