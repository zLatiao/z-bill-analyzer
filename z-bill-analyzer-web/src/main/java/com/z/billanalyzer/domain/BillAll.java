package com.z.billanalyzer.domain;


import java.util.List;

// todo 暂时这么命名
public record BillAll(List<BaseBill<?>> billInfos, List<? extends BaseBillDetail> billDetails) {
}
