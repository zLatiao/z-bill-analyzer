package com.z.billanalyzer.util;

import com.z.billanalyzer.domain.bill.*;
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

    public static void merge(List<BaseBill<?>> billInfos) {
        billInfos.forEach(sourceBill -> billInfos.forEach(targetBill -> merge(sourceBill, targetBill))
        );
    }

    public static void merge(BaseBill<?> sourceBill, BaseBill<?> targetBill) {
        if (targetBill.getBillDetails() == null || targetBill.getBillDetails().isEmpty()) {
            return;
        }
        switch (sourceBill) {
            case WxBill wxBillInfo -> {
                switch (targetBill) {
                    case WxBill wxBillInfo1 -> merge(wxBillInfo, wxBillInfo1);
                    case CmbBill cmbBillInfo -> merge(wxBillInfo, cmbBillInfo);
                    default -> {
                    }
                }
            }
            case AlipayBill alipayBillInfo -> {
                switch (targetBill) {
                    case AlipayBill alipayBillInfo1 -> merge(alipayBillInfo, alipayBillInfo1);
                    case CmbBill cmbBillInfo -> merge(alipayBillInfo, cmbBillInfo);
                    default -> {
                    }
                }
            }
            case CmbBill cmbBillInfo -> {
                switch (targetBill) {
                    case CmbBill cmbBillInfo1 -> merge(cmbBillInfo, cmbBillInfo1);
                    default -> {
                    }
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + sourceBill.getClass());
        }
    }

    public static void merge(CmbBill sourceBill, CmbBill targetBill) {
        if (sourceBill == targetBill) {
            // TODO: 2025/3/11  朝朝宝赎回和支出可能会重复，要考虑怎么合并
        } else {
            sourceBill.getBillDetails().stream()
                    .filter(sourceDetail -> !sourceDetail.isMerge())
                    .forEach(sourceDetail -> targetBill.getBillDetails().stream()
                            .filter(targetDetail -> Objects.equals(sourceDetail.getTransactionTime(), targetDetail.getTransactionTime()))
                            .filter(targetDetail -> Objects.equals(sourceDetail.getAmountType(), targetDetail.getAmountType()))
                            .filter(targetDetail -> Objects.equals(sourceDetail.getAmount(), targetDetail.getAmount()))
                            .filter(targetDetail -> Objects.equals(sourceDetail.getTransactionType(), targetDetail.getTransactionType()))
                            .filter(targetDetail -> Objects.equals(sourceDetail.getRemark(), targetDetail.getRemark()))
                            .forEach(targetDetail -> setMerge(targetDetail, 0)));
        }

    }

    public static void merge(AlipayBill sourceBill, CmbBill targetBill) {
        /**
         * 1. 按过滤出CMB的按银行卡号分组
         * 2. 按照时间分组
         */
        // 按银行卡号分组
        LinkedHashMap<String, List<AlipayBillDetail>> bankMap = sourceBill.getBillDetails().stream()
                .filter(sourceDetail -> BankEnum.CMB.equals(sourceDetail.getPaymentModeBank()))
                .collect(Collectors.groupingBy(AlipayBillDetail::getBankAccountLast4Number, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<String, List<AlipayBillDetail>> entry : bankMap.entrySet()) {
            // 按照时间分组
            LinkedHashMap<LocalDateTime, List<AlipayBillDetail>> collect = entry.getValue().stream()
                    .collect(Collectors.groupingBy(AlipayBillDetail::getTransactionTime, LinkedHashMap::new, Collectors.toList()));
            for (Map.Entry<LocalDateTime, List<AlipayBillDetail>> subEntry : collect.entrySet()) {
                List<AlipayBillDetail> sourceBillDetails = subEntry.getValue();
                String cardNumber = sourceBillDetails.getFirst().getBankAccountLast4Number();
                LocalDateTime transactionTime = sourceBillDetails.getFirst().getTransactionTime();

                if (!cardNumber.equals(targetBill.getBankAccountLast4Number())) {
//                    log.error("找不到匹配的招商银行账单信息，卡号后四位：{}", cardNumber);
                    return;
                }

                BigDecimal amountSum = sourceBillDetails.stream().map(AlipayBillDetail::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

                Predicate<CmbBillDetail> billPredicate = cmb -> cardNumber.equals(cmb.getBankAccountLast4Number())
                        && cmbPaymentModeList.contains(cmb.getTransactionType())
                        && (cmb.getAmount().compareTo(amountSum) == 0)
                        && cmb.getRemark().split("-")[0].equals("支付宝");

                List<CmbBillDetail> targetBillDetails = targetBill.getBillDetails();

                // 先用时间相等比较
                List<CmbBillDetail> filterBillDetails = targetBillDetails.stream()
                        .filter(billPredicate)
                        .filter(cmb -> cmb.getTransactionTime().isEqual(transactionTime))
                        .toList();

                if (filterBillDetails.size() == 1) {
                    CmbBillDetail targetDetail = filterBillDetails.getFirst();
                    setMerge(targetDetail, 1);
                    return;
                } else if (filterBillDetails.size() > 1) {
                    log.error("匹配到多个费用：{}， \n{}", sourceBillDetails, filterBillDetails);
                    return;
                }

                // 上面不行再用时间差5秒内比较
                List<CmbBillDetail> filterBills2 = targetBillDetails.stream()
                        .filter(billPredicate)
                        .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 5)
                        .collect(Collectors.toList());
                if (filterBills2.size() == 1) {
                    CmbBillDetail targetDetail = filterBills2.getFirst();
                    setMerge(targetDetail, 1);
                    sourceBillDetails.forEach(bill -> {
                    });
                    return;
                } else if (filterBills2.size() > 1) {
                    log.error("匹配到多个费用：{}， \n{}", sourceBillDetails, filterBills2);
                }

                // 上面不行再用时间差60秒内比较
                List<CmbBillDetail> filterBills3 = targetBillDetails.stream()
                        .filter(billPredicate)
                        .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 60)
                        .toList();
                if (filterBills3.size() == 1) {
                    CmbBillDetail targetDetail = filterBills3.getFirst();
                    setMerge(targetDetail, 1);
                    return;
                } else if (filterBills2.size() > 1) {
                    log.error("匹配到多个费用：{}， \n{}", sourceBillDetails, filterBills2);
                }

                // 上面不行再用时间差5分钟内比较
                List<CmbBillDetail> filterBills4 = targetBillDetails.stream()
                        .filter(billPredicate)
                        .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 60 * 5)
                        .toList();
                if (filterBills4.isEmpty()) {
//                    log.error("找不到匹配的招商银行账单记录：{}", sourceBills);
                } else if (filterBills4.size() == 1) {
                    CmbBillDetail targetDetail = filterBills4.getFirst();
                    setMerge(targetDetail, 1);
                } else {
                    log.error("匹配到多个费用：{}， \n{}", sourceBillDetails, filterBills2);
                }
            }

        }
    }

    public static void merge(AlipayBill sourceBill, AlipayBill targetBill) {
        if (sourceBill == targetBill) {
            // TODO: 2025/3/11 退款
            return;
        }
        sourceBill.getBillDetails().stream()
                .filter(sourceDetail -> !sourceDetail.isMerge())
                .forEach(sourceDetail -> targetBill.getBillDetails().stream()
                        .filter(targetDetail -> sourceDetail.getTransactionNo().equals(targetDetail.getTransactionNo()))
                        .forEach(targetDetail -> setMerge(targetDetail, 0)));
    }

    public static void merge(WxBill sourceBill, CmbBill targetBill) {
        sourceBill.getBillDetails().stream()
                .filter(sourceDetail -> BankEnum.CMB.equals(sourceDetail.getPaymentModeBank()))
                .forEach(sourceDetail -> {
                    String cardNumber = sourceDetail.getBankAccountLast4Number();
                    if (!cardNumber.equals(targetBill.getBankAccountLast4Number())) {
                        return;
                    }

                    List<CmbBillDetail> cmbBillDetails = targetBill.getBillDetails();

                    Predicate<CmbBillDetail> billPredicate = cmb -> cardNumber.equals(cmb.getBankAccountLast4Number())
                            && cmbPaymentModeList.contains(cmb.getTransactionType())
                            && cmb.getAmount().compareTo(sourceDetail.getAmount()) == 0
                            && cmb.getRemark().split("-")[0].equals("财付通");

                    // 先用时间相等比较
                    List<CmbBillDetail> filterBillDetails = cmbBillDetails.stream()
                            .filter(billPredicate)
                            .filter(cmb -> cmb.getTransactionTime().isEqual(sourceDetail.getTransactionTime()))
                            .toList();

                    if (filterBillDetails.size() == 1) {
                        CmbBillDetail targetDetail = filterBillDetails.getFirst();
                        setMerge(targetDetail, 1);
                        return;
                    }

                    if (filterBillDetails.size() > 1) {
                        log.error("匹配到多个费用：{}， \n{}", sourceDetail, filterBillDetails);
                        return;
                    }

                    // 上面不行再用时间差1秒内比较
                    List<CmbBillDetail> filterBills2 = cmbBillDetails.stream()
                            .filter(billPredicate)
                            .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), sourceDetail.getTransactionTime()).toSeconds()) <= 1)
                            .collect(Collectors.toList());
                    if (filterBills2.isEmpty()) {
//                        log.error("找不到匹配的招商银行账单记录：{}", sourceDetail);
                    } else if (filterBills2.size() == 1) {
                        CmbBillDetail targetDetail = filterBills2.getFirst();
                        setMerge(targetDetail, 1);
                    } else {
                        log.error("匹配到多个费用：{}， \n{}", sourceDetail, filterBills2);
                    }
                });

    }

    public static void merge(WxBill sourceBill, WxBill targetBill) {
        if (sourceBill == targetBill) {
            // TODO: 2025/3/11 退款
            return;
        }
        sourceBill.getBillDetails().stream()
                .filter(sourceDetail -> !sourceDetail.isMerge())
                .forEach(sourceDetail -> targetBill.getBillDetails().stream()
                        .filter(targetDetail -> sourceDetail.getTransactionNo().equals(targetDetail.getTransactionNo()))
                        .forEach(targetDetail -> setMerge(targetDetail, 0)));
    }

    public static void setMerge(BaseBillDetail billDetail, Integer mergeType) {
        billDetail.setMerge(true);
        billDetail.setMergeType(mergeType);
    }
}
