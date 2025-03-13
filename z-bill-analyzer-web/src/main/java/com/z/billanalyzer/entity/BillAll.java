package com.z.billanalyzer.entity;


import java.util.List;

// todo 暂时这么命名
public record BillAll(List<BaseBillInfo> billInfos, List<Bill> bills) {
}
