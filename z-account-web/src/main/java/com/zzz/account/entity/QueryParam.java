package com.zzz.account.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 查询参数
 */
@Data
public class QueryParam {
    /**
     * 账单ID
     */
    private Integer id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer pageIndex;

    private Integer pageSize;

    /**
     * 账单来源列表
     */
    private List<Integer> sourceList;
}
