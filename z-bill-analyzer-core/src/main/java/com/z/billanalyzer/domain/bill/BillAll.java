package com.z.billanalyzer.domain.bill;


import java.util.List;

// todo 暂时这么命名
public record BillAll(List<BaseBill<?>> bills, List<? extends BaseBillDetail> billDetails) {
}
