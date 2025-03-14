package com.z.billanalyzer.entity.vo.echarts;

import java.math.BigDecimal;
import java.util.List;


/**
 * 趋势数据
 *
 * @param timeList     季度列表
 * @param incomeTrend  incomeTrend
 * @param expenseTrend expenseTrend
 */
public record TrendVO(List<String> timeList, List<BigDecimal> incomeTrend, List<BigDecimal> expenseTrend) {
}