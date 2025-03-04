package com.zzz.account.entity.vo;

import java.math.BigDecimal;

/**
 * 账单统计
 *
 * @param income  收入（单位：元）
 * @param incomeCnt  收入数量
 * @param expense  收入（单位：元）
 * @param expenseCnt 支出（单位：元）
 * @param balance      累计结余 = 总收入 - 总支出
 */
public record StatisticVO(BigDecimal income,
                          Integer incomeCnt,
                          BigDecimal expense,
                          Integer expenseCnt,
                          BigDecimal balance) {
}