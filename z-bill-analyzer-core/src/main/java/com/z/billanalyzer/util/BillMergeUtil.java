package com.z.billanalyzer.util;

import com.z.billanalyzer.domain.*;
import com.z.billanalyzer.enums.BankEnum;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 微信/支付宝账单和银行账单可能会有重合记录，需要合并/去重
 * <p>
 * 微信账单：
 * 可以通过支付方式判断是零钱支付还是银行卡支付，银行卡支付的话用来去重。
 * 收入：支付方式为/
 * 支出：支付方式有 零钱、招商银行储蓄卡(xxxx)、中国银行储蓄卡(xxxx)
 * 支付宝账单：
 * 收/付款方式同上
 *  TODO 退款的也是不计收支
 *  退款和理财的要合并。退款的订单号好像是有关联_
 * 招商银行：交易备注里面或许可以解析出是微信还是支付宝的、交易类型未知。支付用朝朝宝的话，朝朝宝赎回有一笔收入，支付有一笔支出。可能会重复，这点也要考虑合并。
 * <p>
 * 招商银行的备注和微信的交易对方/不同，例如：招行的备注是【财付通-广东罗森】，微信的交易对方是【LAWSON壹方中心店消费】
 * <p>
 * 微信-微信：所有字段都相同
 *
 * @author z-latiao
 * @since 2025/2/27 9:20
 */
@Slf4j
public class BillMergeUtil {

    public static List<String> cmbPaymentModeList = List.of("网联退款", "网联协议支付", "银联快捷支付", "投资理财", "网联付款交易");

    public static void merge(List<BaseBill> billInfos) {
        billInfos.forEach(sourceBillInfo -> billInfos.forEach(targetBillInfo -> merge(sourceBillInfo, targetBillInfo))
        );
    }

    public static void merge(BaseBill sourceBillInfo, BaseBill targetBillInfo) {
        if (targetBillInfo.getBillDetails() == null || targetBillInfo.getBillDetails().isEmpty()) {
            return;
        }
        switch (sourceBillInfo) {
            case WxBill wxBillInfo -> {
                switch (targetBillInfo) {
                    case WxBill wxBillInfo1 -> merge(wxBillInfo, wxBillInfo1);
                    case CmbBill cmbBillInfo -> merge(wxBillInfo, cmbBillInfo);
                    default -> {
                    }
                }
            }
            case AlipayBill alipayBillInfo -> {
                switch (targetBillInfo) {
                    case AlipayBill alipayBillInfo1 -> merge(alipayBillInfo, alipayBillInfo1);
                    case CmbBill cmbBillInfo -> merge(alipayBillInfo, cmbBillInfo);
                    default -> {
                    }
                }
            }
            case CmbBill cmbBillInfo -> {
                switch (targetBillInfo) {
                    case CmbBill cmbBillInfo1 -> merge(cmbBillInfo, cmbBillInfo1);
                    default -> {
                    }
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + sourceBillInfo.getClass());
        }
    }

    public static void merge(CmbBill sourceBillInfo, CmbBill targetBillInfo) {
        if (sourceBillInfo == targetBillInfo) {
            // TODO: 2025/3/11  朝朝宝赎回和支出可能会重复，要考虑怎么合并
        } else {
            sourceBillInfo.getBillDetails().stream()
                    .filter(sourceBill -> !sourceBill.isMerge())
                    .forEach(sourceBill -> targetBillInfo.getBillDetails().stream()
                            .filter(targetBill -> Objects.equals(sourceBill.getTransactionTime(), targetBill.getTransactionTime()))
                            .filter(targetBill -> Objects.equals(sourceBill.getAmountType(), targetBill.getAmountType()))
                            .filter(targetBill -> Objects.equals(sourceBill.getAmount(), targetBill.getAmount()))
                            .filter(targetBill -> Objects.equals(sourceBill.getTransactionType(), targetBill.getTransactionType()))
                            .filter(targetBill -> Objects.equals(sourceBill.getRemark(), targetBill.getRemark()))
                            .forEach(targetBill -> setMerge(targetBill, 0)));
        }

    }

    public static void merge(AlipayBill sourceBillInfo, CmbBill targetBillInfo) {
        /**
         * 1. 按过滤出CMB的按银行卡号分组
         * 2. 按照时间分组
         */
        // 按银行卡号分组
        LinkedHashMap<String, List<BillDetail>> bankMap = sourceBillInfo.getBillDetails().stream()
                .filter(sourceBill -> BankEnum.CMB.equals(sourceBill.getBank()))
                .collect(Collectors.groupingBy(BillDetail::getBankAccountLast4Number, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<String, List<BillDetail>> entry : bankMap.entrySet()) {
            // 按照时间分组
            LinkedHashMap<LocalDateTime, List<BillDetail>> collect = entry.getValue().stream()
                    .collect(Collectors.groupingBy(BillDetail::getTransactionTime, LinkedHashMap::new, Collectors.toList()));
            for (Map.Entry<LocalDateTime, List<BillDetail>> subEntry : collect.entrySet()) {
                List<BillDetail> sourceBillDetails = subEntry.getValue();
                String cardNumber = sourceBillDetails.getFirst().getBankAccountLast4Number();
                LocalDateTime transactionTime = sourceBillDetails.getFirst().getTransactionTime();

                if (!cardNumber.equals(targetBillInfo.getBankAccountLast4Number())) {
//                    log.error("找不到匹配的招商银行账单信息，卡号后四位：{}", cardNumber);
                    return;
                }

                BigDecimal amountSum = sourceBillDetails.stream().map(BillDetail::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

                Predicate<BillDetail> billPredicate = cmb -> cardNumber.equals(cmb.getBankAccountLast4Number())
                        && cmbPaymentModeList.contains(cmb.getTransactionType())
                        && (cmb.getAmount().compareTo(amountSum) == 0)
                        && cmb.getRemark().split("-")[0].equals("支付宝");

                List<BillDetail> targetBillDetails = targetBillInfo.getBillDetails();

                // 先用时间相等比较
                List<BillDetail> filterBillDetails = targetBillDetails.stream()
                        .filter(billPredicate)
                        .filter(cmb -> cmb.getTransactionTime().isEqual(transactionTime))
                        .toList();

                if (filterBillDetails.size() == 1) {
                    BillDetail targetBillDetail = filterBillDetails.getFirst();
                    setMerge(targetBillDetail, 1);
                    return;
                } else if (filterBillDetails.size() > 1) {
                    log.error("匹配到多个费用：{}， \n{}", sourceBillDetails, filterBillDetails);
                    return;
                }

                // 上面不行再用时间差5秒内比较
                List<BillDetail> filterBills2 = targetBillDetails.stream()
                        .filter(billPredicate)
                        .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 5)
                        .collect(Collectors.toList());
                if (filterBills2.size() == 1) {
                    BillDetail targetBillDetail = filterBills2.getFirst();
                    setMerge(targetBillDetail, 1);
                    sourceBillDetails.forEach(bill -> {
                    });
                    return;
                } else if (filterBills2.size() > 1) {
                    log.error("匹配到多个费用：{}， \n{}", sourceBillDetails, filterBills2);
                }

                // 上面不行再用时间差60秒内比较
                List<BillDetail> filterBills3 = targetBillDetails.stream()
                        .filter(billPredicate)
                        .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 60)
                        .toList();
                if (filterBills3.size() == 1) {
                    BillDetail targetBillDetail = filterBills3.getFirst();
                    setMerge(targetBillDetail, 1);
                    return;
                } else if (filterBills2.size() > 1) {
                    log.error("匹配到多个费用：{}， \n{}", sourceBillDetails, filterBills2);
                }

                // 上面不行再用时间差5分钟内比较
                List<BillDetail> filterBills4 = targetBillDetails.stream()
                        .filter(billPredicate)
                        .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 60 * 5)
                        .toList();
                if (filterBills4.isEmpty()) {
//                    log.error("找不到匹配的招商银行账单记录：{}", sourceBills);
                } else if (filterBills4.size() == 1) {
                    BillDetail targetBillDetail = filterBills4.getFirst();
                    setMerge(targetBillDetail, 1);
                } else {
                    log.error("匹配到多个费用：{}， \n{}", sourceBillDetails, filterBills2);
                }
            }

        }
    }

    public static void merge(AlipayBill sourceBillInfo, AlipayBill targetBillInfo) {
        if (sourceBillInfo == targetBillInfo) {
            // TODO: 2025/3/11 退款
            return;
        }
        sourceBillInfo.getBillDetails().stream()
                .filter(sourceBill -> !sourceBill.isMerge())
                .forEach(sourceBill -> targetBillInfo.getBillDetails().stream()
                        .filter(targetBill -> sourceBill.getBillNo().equals(targetBill.getBillNo()))
                        .forEach(targetBill -> setMerge(targetBill, 0)));
    }

    public static void merge(WxBill sourceBillInfo, CmbBill targetBillInfo) {
        sourceBillInfo.getBillDetails().stream()
                .filter(sourceBill -> BankEnum.CMB.equals(sourceBill.getBank()))
                .forEach(sourceBill -> {
                    String cardNumber = sourceBill.getBankAccountLast4Number();
                    if (!cardNumber.equals(targetBillInfo.getBankAccountLast4Number())) {
                        return;
                    }

                    List<BillDetail> cmbBillDetails = targetBillInfo.getBillDetails();

                    Predicate<BillDetail> billPredicate = cmb -> cardNumber.equals(cmb.getBankAccountLast4Number())
                            && cmbPaymentModeList.contains(cmb.getTransactionType())
                            && cmb.getAmount().compareTo(sourceBill.getAmount()) == 0
                            && cmb.getRemark().split("-")[0].equals("财付通");

                    // 先用时间相等比较
                    List<BillDetail> filterBillDetails = cmbBillDetails.stream()
                            .filter(billPredicate)
                            .filter(cmb -> cmb.getTransactionTime().isEqual(sourceBill.getTransactionTime()))
                            .toList();

                    if (filterBillDetails.size() == 1) {
                        BillDetail targetBillDetail = filterBillDetails.getFirst();
                        setMerge(targetBillDetail, 1);
                        return;
                    }

                    if (filterBillDetails.size() > 1) {
                        log.error("匹配到多个费用：{}， \n{}", sourceBill, filterBillDetails);
                        return;
                    }

                    // 上面不行再用时间差1秒内比较
                    List<BillDetail> filterBills2 = cmbBillDetails.stream()
                            .filter(billPredicate)
                            .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), sourceBill.getTransactionTime()).toSeconds()) <= 1)
                            .collect(Collectors.toList());
                    if (filterBills2.isEmpty()) {
//                        log.error("找不到匹配的招商银行账单记录：{}", sourceBill);
                    } else if (filterBills2.size() == 1) {
                        BillDetail targetBillDetail = filterBills2.getFirst();
                        setMerge(targetBillDetail, 1);
                    } else {
                        log.error("匹配到多个费用：{}， \n{}", sourceBill, filterBills2);
                    }
                });

    }

    public static void merge(WxBill sourceBillInfo, WxBill targetBillInfo) {
        if (sourceBillInfo == targetBillInfo) {
            // TODO: 2025/3/11 退款
            return;
        }
        sourceBillInfo.getBillDetails().stream()
                .filter(sourceBill -> !sourceBill.isMerge())
                .forEach(sourceBill -> targetBillInfo.getBillDetails().stream()
                        .filter(targetBill -> sourceBill.getBillNo().equals(targetBill.getBillNo()))
                        .forEach(targetBill -> setMerge(targetBill, 0)));
    }

    public static void setMerge(BillDetail billDetail, Integer mergeType) {
        billDetail.setMerge(true);
        billDetail.setMergeType(mergeType);
    }
}
