package com.z.billanalyzer.domain.param;

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

    private Integer pageIndex = 1;

    private Integer pageSize = 100;

    private Integer amountType;

    /**
     * asc升序、desc降序
     */
    private String orderType = "desc";

    /**
     * 排序字段
     */
    private String orderBy = "transactionTime";

    /**
     * 账单来源列表
     */
    private List<Integer> sourceList;

    /**
     * 过滤账单来源列表
     */
    private List<Integer> filterSourceList;

    /**
     * 过滤重复项类型列表
     */
    private List<Integer> duplicateTypeList;

    /**
     * 时间单位：0日度、1月度、2季度、3年度
     */
    private Integer timeUnit = 1;
}
