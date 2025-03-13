package com.z.bill.entity.vo;

import java.time.LocalDate;
import java.util.List;

/**
 * todo 命名暂定，用来存储账单可以筛选的信息，例如文件名、交易类型、账单起止时间等
 *
 * @author zzz
 * @since 2025/3/11 22:08
 */
public record ImportBillInfoVO(List<String> fileNames, List<String> transactionTypes, LocalDate startDate,
                               LocalDate endDate) {
}
