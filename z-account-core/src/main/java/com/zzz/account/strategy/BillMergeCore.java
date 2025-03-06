package com.zzz.account.strategy;

import com.zzz.account.entity.AlipayBillInfo;
import com.zzz.account.entity.BaseBillInfo;
import com.zzz.account.entity.CmbBillInfo;
import com.zzz.account.entity.WxBillInfo;

import java.util.List;

public class BillMergeCore {
    public void merge(List<BaseBillInfo> billInfos) {
        billInfos.forEach(currBillInfo -> billInfos.stream()
                .filter(info -> currBillInfo != info)
                .forEach(otherBillInfo -> merge(currBillInfo, otherBillInfo))
        );
    }

    public static void merge(BaseBillInfo source, BaseBillInfo target) {
        switch (source) {
            case WxBillInfo wxBillInfo -> {
                switch (target) {
                    case WxBillInfo wxBillInfo1 ->
                            merge(wxBillInfo, wxBillInfo1);
                    case AlipayBillInfo alipayBillInfo ->
                            merge(wxBillInfo, alipayBillInfo);
                    default ->
                            throw new IllegalStateException("Unexpected value: " + target);
                }
            }
            case AlipayBillInfo alipayBillInfo -> {
                switch (target) {
                    case AlipayBillInfo alipayBillInfo1 ->
                            merge(alipayBillInfo, alipayBillInfo1);
                    case CmbBillInfo cmbBillInfo ->
                            merge(alipayBillInfo, cmbBillInfo);
                    default ->
                            throw new IllegalStateException("Unexpected value: " + target);
                }
            }
            case CmbBillInfo cmbBillInfo -> {
                switch (target) {
                    case CmbBillInfo cmbBillInfo1 ->
                            merge(cmbBillInfo, cmbBillInfo1);
                    default ->
                            throw new IllegalStateException("Unexpected value: " + target);
                }
            }
            default ->
                    throw new IllegalStateException("Unexpected value: " + source.getClass());
        }
    }

    private static void merge(CmbBillInfo cmbBillInfo, CmbBillInfo cmbBillInfo1) {

    }

    private static void merge(AlipayBillInfo alipayBillInfo, CmbBillInfo cmbBillInfo) {

    }

    private static void merge(AlipayBillInfo alipayBillInfo, AlipayBillInfo alipayBillInfo1) {

    }

    private static void merge(WxBillInfo wxBillInfo, AlipayBillInfo alipayBillInfo) {

    }

    private static void merge(WxBillInfo wxBillInfo, WxBillInfo wxBillInfo2) {
    }
}
