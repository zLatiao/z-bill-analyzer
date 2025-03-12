package com.zzz.account.util;

import com.zzz.account.entity.*;
import com.zzz.account.enums.BankEnum;
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

    public static void merge(List<BaseBillInfo> billInfos) {
        billInfos.forEach(sourceBillInfo -> billInfos.forEach(targetBillInfo -> merge(sourceBillInfo, targetBillInfo))
        );
    }

    public static void merge(BaseBillInfo sourceBillInfo, BaseBillInfo targetBillInfo) {
        if (targetBillInfo.getBills() == null || targetBillInfo.getBills().isEmpty()) {
            return;
        }
        switch (sourceBillInfo) {
            case WxBillInfo wxBillInfo -> {
                switch (targetBillInfo) {
                    case WxBillInfo wxBillInfo1 -> merge(wxBillInfo, wxBillInfo1);
                    case CmbBillInfo cmbBillInfo -> merge(wxBillInfo, cmbBillInfo);
                    default -> {
                    }
                }
            }
            case AlipayBillInfo alipayBillInfo -> {
                switch (targetBillInfo) {
                    case AlipayBillInfo alipayBillInfo1 -> merge(alipayBillInfo, alipayBillInfo1);
                    case CmbBillInfo cmbBillInfo -> merge(alipayBillInfo, cmbBillInfo);
                    default -> {
                    }
                }
            }
            case CmbBillInfo cmbBillInfo -> {
                switch (targetBillInfo) {
                    case CmbBillInfo cmbBillInfo1 -> merge(cmbBillInfo, cmbBillInfo1);
                    default -> {
                    }
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + sourceBillInfo.getClass());
        }
    }

    public static void merge(CmbBillInfo sourceBillInfo, CmbBillInfo targetBillInfo) {
        if (sourceBillInfo == targetBillInfo) {
            // TODO: 2025/3/11  朝朝宝赎回和支出可能会重复，要考虑怎么合并
        } else {
            sourceBillInfo.getBills().stream()
                    .filter(sourceBill -> !sourceBill.isMerge())
                    .forEach(sourceBill -> targetBillInfo.getBills().stream()
                            .filter(targetBill -> Objects.equals(sourceBill.getTransactionTime(), targetBill.getTransactionTime()))
                            .filter(targetBill -> Objects.equals(sourceBill.getAmountType(), targetBill.getAmountType()))
                            .filter(targetBill -> Objects.equals(sourceBill.getAmount(), targetBill.getAmount()))
                            .filter(targetBill -> Objects.equals(sourceBill.getTransactionType(), targetBill.getTransactionType()))
                            .filter(targetBill -> Objects.equals(sourceBill.getRemark(), targetBill.getRemark()))
                            .forEach(targetBill -> setMerge(targetBill, 0)));
        }

    }

    public static void merge(AlipayBillInfo sourceBillInfo, CmbBillInfo targetBillInfo) {
        /**
         * 1. 按过滤出CMB的按银行卡号分组
         * 2. 按照时间分组
         */
        // 按银行卡号分组
        LinkedHashMap<String, List<Bill>> bankMap = sourceBillInfo.getBills().stream()
                .filter(sourceBill -> BankEnum.CMB.equals(sourceBill.getBank()))
                .collect(Collectors.groupingBy(Bill::getBankAccountLast4Number, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<String, List<Bill>> entry : bankMap.entrySet()) {
            // 按照时间分组
            LinkedHashMap<LocalDateTime, List<Bill>> collect = entry.getValue().stream()
                    .collect(Collectors.groupingBy(Bill::getTransactionTime, LinkedHashMap::new, Collectors.toList()));
            for (Map.Entry<LocalDateTime, List<Bill>> subEntry : collect.entrySet()) {
                List<Bill> sourceBills = subEntry.getValue();
                String cardNumber = sourceBills.getFirst().getBankAccountLast4Number();
                LocalDateTime transactionTime = sourceBills.getFirst().getTransactionTime();

                if (!cardNumber.equals(targetBillInfo.getBankAccountLast4Number())) {
//                    log.error("找不到匹配的招商银行账单信息，卡号后四位：{}", cardNumber);
                    return;
                }

                BigDecimal amountSum = sourceBills.stream().map(Bill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

                Predicate<Bill> billPredicate = cmb -> cardNumber.equals(cmb.getBankAccountLast4Number())
                        && cmbPaymentModeList.contains(cmb.getTransactionType())
                        && (cmb.getAmount().compareTo(amountSum) == 0)
                        && cmb.getRemark().split("-")[0].equals("支付宝");

                List<Bill> targetBills = targetBillInfo.getBills();

                // 先用时间相等比较
                List<Bill> filterBills = targetBills.stream()
                        .filter(billPredicate)
                        .filter(cmb -> cmb.getTransactionTime().isEqual(transactionTime))
                        .toList();

                if (filterBills.size() == 1) {
                    Bill targetBill = filterBills.getFirst();
                    setMerge(targetBill, 1);
                    return;
                } else if (filterBills.size() > 1) {
                    log.error("匹配到多个费用：{}， \n{}", sourceBills, filterBills);
                    return;
                }

                // 上面不行再用时间差5秒内比较
                List<Bill> filterBills2 = targetBills.stream()
                        .filter(billPredicate)
                        .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 5)
                        .collect(Collectors.toList());
                if (filterBills2.size() == 1) {
                    Bill targetBill = filterBills2.getFirst();
                    setMerge(targetBill, 1);
                    sourceBills.forEach(bill -> {
                    });
                    return;
                } else if (filterBills2.size() > 1) {
                    log.error("匹配到多个费用：{}， \n{}", sourceBills, filterBills2);
                }

                // 上面不行再用时间差60秒内比较
                List<Bill> filterBills3 = targetBills.stream()
                        .filter(billPredicate)
                        .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 60)
                        .toList();
                if (filterBills3.size() == 1) {
                    Bill targetBill = filterBills3.getFirst();
                    setMerge(targetBill, 1);
                    return;
                } else if (filterBills2.size() > 1) {
                    log.error("匹配到多个费用：{}， \n{}", sourceBills, filterBills2);
                }

                // 上面不行再用时间差5分钟内比较
                List<Bill> filterBills4 = targetBills.stream()
                        .filter(billPredicate)
                        .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 60 * 5)
                        .toList();
                if (filterBills4.isEmpty()) {
                    log.error("找不到匹配的招商银行账单记录：{}", sourceBills);
                } else if (filterBills4.size() == 1) {
                    Bill targetBill = filterBills4.getFirst();
                    setMerge(targetBill, 1);
                } else {
                    log.error("匹配到多个费用：{}， \n{}", sourceBills, filterBills2);
                }
            }

        }
    }

    public static void merge(AlipayBillInfo sourceBillInfo, AlipayBillInfo targetBillInfo) {
        if (sourceBillInfo == targetBillInfo) {
            // TODO: 2025/3/11 退款
            return;
        }
        sourceBillInfo.getBills().stream()
                .filter(sourceBill -> !sourceBill.isMerge())
                .forEach(sourceBill -> targetBillInfo.getBills().stream()
                        .filter(targetBill -> sourceBill.getBillNo().equals(targetBill.getBillNo()))
                        .forEach(targetBill -> setMerge(targetBill, 0)));
    }

    public static void merge(WxBillInfo sourceBillInfo, CmbBillInfo targetBillInfo) {
        sourceBillInfo.getBills().stream()
                .filter(sourceBill -> BankEnum.CMB.equals(sourceBill.getBank()))
                .forEach(sourceBill -> {
                    String cardNumber = sourceBill.getBankAccountLast4Number();
                    if (!cardNumber.equals(targetBillInfo.getBankAccountLast4Number())) {
                        return;
                    }

                    List<Bill> cmbBills = targetBillInfo.getBills();

                    Predicate<Bill> billPredicate = cmb -> cardNumber.equals(cmb.getBankAccountLast4Number())
                            && cmbPaymentModeList.contains(cmb.getTransactionType())
                            && cmb.getAmount().compareTo(sourceBill.getAmount()) == 0
                            && cmb.getRemark().split("-")[0].equals("财付通");

                    // 先用时间相等比较
                    List<Bill> filterBills = cmbBills.stream()
                            .filter(billPredicate)
                            .filter(cmb -> cmb.getTransactionTime().isEqual(sourceBill.getTransactionTime()))
                            .toList();

                    if (filterBills.size() == 1) {
                        Bill targetBill = filterBills.getFirst();
                        setMerge(targetBill, 1);
                        return;
                    }

                    if (filterBills.size() > 1) {
                        log.error("匹配到多个费用：{}， \n{}", sourceBill, filterBills);
                        return;
                    }

                    // 上面不行再用时间差1秒内比较
                    List<Bill> filterBills2 = cmbBills.stream()
                            .filter(billPredicate)
                            .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), sourceBill.getTransactionTime()).toSeconds()) <= 1)
                            .collect(Collectors.toList());
                    if (filterBills2.isEmpty()) {
                        log.error("找不到匹配的招商银行账单记录：{}", sourceBill);
                    } else if (filterBills2.size() == 1) {
                        Bill targetBill = filterBills2.getFirst();
                        setMerge(targetBill, 1);
                    } else {
                        log.error("匹配到多个费用：{}， \n{}", sourceBill, filterBills2);
                    }
                });

    }

    public static void merge(WxBillInfo sourceBillInfo, WxBillInfo targetBillInfo) {
        if (sourceBillInfo == targetBillInfo) {
            // TODO: 2025/3/11 退款
            return;
        }
        sourceBillInfo.getBills().stream()
                .filter(sourceBill -> !sourceBill.isMerge())
                .forEach(sourceBill -> targetBillInfo.getBills().stream()
                        .filter(targetBill -> sourceBill.getBillNo().equals(targetBill.getBillNo()))
                        .forEach(targetBill -> setMerge(targetBill, 0)));
    }

    public static void setMerge(Bill bill, Integer mergeType) {
        bill.setMerge(true);
        bill.setMergeType(mergeType);
    }
}
