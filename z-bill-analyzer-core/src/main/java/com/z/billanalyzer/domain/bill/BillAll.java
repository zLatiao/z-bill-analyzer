package com.z.billanalyzer.domain.bill;


import com.z.billanalyzer.domain.bill.BaseBill;
import com.z.billanalyzer.domain.bill.BaseBillDetail;

import java.util.List;

// todo 暂时这么命名
public record BillAll(List<BaseBill<?>> billInfos, List<? extends BaseBillDetail> billDetails) {
}
