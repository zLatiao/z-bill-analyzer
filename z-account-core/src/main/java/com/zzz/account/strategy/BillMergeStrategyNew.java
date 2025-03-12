package com.zzz.account.strategy;

import com.zzz.account.entity.*;
import com.zzz.account.enums.BankEnum;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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
@Deprecated
public class BillMergeStrategyNew {
    static List<String> cmbPaymentModeList = List.of("网联退款", "网联协议支付", "银联快捷支付", "投资理财", "网联付款交易");

    public List<Bill> merge(List<BaseBillInfo> billInfos) {
        for (BaseBillInfo billInfo : billInfos) {
            List<BaseBillInfo> otherBillInfos = billInfos.stream().filter(info -> billInfo != info).toList();
            if (billInfo instanceof WxBillInfo wxBillInfo) {
                mergeWxBill(wxBillInfo, otherBillInfos);
            } else if (billInfo instanceof AlipayBillInfo alipayBillInfo) {
                mergeAlipayBill(alipayBillInfo, otherBillInfos);
            } else if (billInfo instanceof CmbBillInfo cmbBillInfo) {
                mergeCmbBill(cmbBillInfo, otherBillInfos);
            }
        }
        List<Bill> result = billInfos.stream().flatMap(x -> x.getBills().stream()).collect(Collectors.toList());
//        result.removeIf(bill -> BillSourceEnum.CMB.ordinal() == bill.getSource() && Boolean.TRUE.equals(bill.getIsMerge()));
        return result;
    }

    private void mergeCmbBill(CmbBillInfo cmbBillInfo, List<BaseBillInfo> otherBillInfos) {
        mergeCmbAndCmb(cmbBillInfo, otherBillInfos.stream().filter(x -> x instanceof CmbBillInfo).toList());

    }

    private void mergeCmbAndCmb(CmbBillInfo cmbBillInfo, List<BaseBillInfo> otherWxBillInfos) {
        if (cmbBillInfo == null || otherWxBillInfos.isEmpty()) {
            return;
        }

        List<Bill> otherBills = otherWxBillInfos.stream().flatMap(x -> x.getBills().stream()).toList();
        cmbBillInfo.getBills().stream()
                .filter(sourceBill -> !sourceBill.isMerge())
                .forEach(currBill -> otherBills.stream()
                        .filter(otherBill -> Objects.equals(currBill.getTransactionTime(), otherBill.getTransactionTime()))
                        .filter(otherBill -> Objects.equals(currBill.getAmountType(), otherBill.getAmountType()))
                        .filter(otherBill -> Objects.equals(currBill.getAmount(), otherBill.getAmount()))
                        .filter(otherBill -> Objects.equals(currBill.getTransactionType(), otherBill.getTransactionType()))
                        .filter(otherBill -> Objects.equals(currBill.getRemark(), otherBill.getRemark()))
                        .forEach(bill -> setMerge(bill, 0)));
    }

    private void mergeAlipayBill(AlipayBillInfo alipayBillInfo, List<BaseBillInfo> otherBillInfos) {

        mergeAlipayAndAlipay(alipayBillInfo, otherBillInfos.stream().filter(x -> x instanceof AlipayBillInfo).map(x -> (AlipayBillInfo) x).toList());


        mergeAlipayAndCmb(alipayBillInfo, otherBillInfos);
    }

    private void mergeAlipayAndAlipay(AlipayBillInfo alipayBillInfo, List<AlipayBillInfo> otherAlipayInfos) {
        if (otherAlipayInfos == null || otherAlipayInfos.isEmpty()) {
            return;
        }

        List<Bill> otherBills = otherAlipayInfos.stream().flatMap(x -> x.getBills().stream()).toList();

        alipayBillInfo.getBills().stream()
                .filter(sourceBill -> !sourceBill.isMerge())
                .forEach(currBill -> otherBills.stream()
                        .filter(otherBill -> currBill.getBillNo().equals(otherBill.getBillNo()))
                        .forEach(bill -> setMerge(bill, 0)));
    }

    private void mergeAlipayAndCmb(AlipayBillInfo alipayBillInfo, List<BaseBillInfo> billInfos) {
        /**
         * 1. 按过滤出CMB的按银行卡号分组
         * 2. 按照时间分组
         */
        List<Bill> bills = alipayBillInfo.getBills();
        // 按银行卡号分组
        LinkedHashMap<String, List<Bill>> bankMap = bills.stream()
                .filter(bill -> BankEnum.CMB.equals(bill.getBank()))
                .collect(Collectors.groupingBy(Bill::getBankAccountLast4Number, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<String, List<Bill>> entry : bankMap.entrySet()) {
            // 按照时间分组
            LinkedHashMap<LocalDateTime, List<Bill>> collect = entry.getValue().stream()
                    .collect(Collectors.groupingBy(Bill::getTransactionTime, LinkedHashMap::new, Collectors.toList()));
            for (Map.Entry<LocalDateTime, List<Bill>> subEntry : collect.entrySet()) {
                List<Bill> bills1 = subEntry.getValue();
                // TODO: 2025/3/4
                mergeAlipayAndCmb(bills1, billInfos);
            }

        }
    }

    // todo 支付宝账单时间和支付宝账单时间很有误差，甚至一分钟。
    //  支付宝账单金额和招商银行账单金额可能不一致，有可能招商一笔是支付宝多笔？可能是因为商品消费和运费险在支付宝里面是分开两笔的、淘宝一笔下单多个店铺的商品也会在支付宝多笔费用
    private void mergeAlipayAndCmb(List<Bill> bills, List<BaseBillInfo> billInfos) {
        Bill firstBill = bills.getFirst();
        String cardNumber = firstBill.getBankAccountLast4Number();
        LocalDateTime transactionTime = firstBill.getTransactionTime();
        BigDecimal amountSum = bills.stream().map(Bill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Optional<BaseBillInfo> first = billInfos.stream().filter(info -> {
            if (info instanceof CmbBillInfo cmbBillInfo) {
                return cardNumber.equals(cmbBillInfo.getBankAccountLast4Number());
            }
            return false;
        }).findFirst();

        if (first.isEmpty()) {
            log.error("找不到匹配的招商银行账单信息，卡号后四位：{}", cardNumber);
            return;
        }

        BaseBillInfo cmbBillInfo = first.get();
        List<Bill> cmbBills = cmbBillInfo.getBills();

        Predicate<Bill> billPredicate = cmb -> cardNumber.equals(cmb.getBankAccountLast4Number())
                && cmbPaymentModeList.contains(cmb.getTransactionType())
                && (cmb.getAmount().compareTo(amountSum) == 0)
                && cmb.getRemark().split("-")[0].equals("支付宝");

        // 先用时间相等比较
        List<Bill> filterBills = cmbBills.stream()
                .filter(billPredicate)
                .filter(cmb -> cmb.getTransactionTime().isEqual(transactionTime))
                .toList();

        if (filterBills.size() == 1) {
            Bill cmb = filterBills.getFirst();
            setMerge(cmb, 1);
//            bills.forEach(bill -> {
//                if (bill.getSameBills() == null) {
//                    bill.setSameBills(new ArrayList<>());
//                }
//                cmb.setIsMerge(true);
//                bill.setIsMerge(true);
////                bill.getSameBills().add(cmb);
//            });
            return;
        } else if (filterBills.size() > 1) {
            log.error("匹配到多个费用：{}， \n{}", bills, filterBills);
            return;
        }

        // 上面不行再用时间差5秒内比较
        List<Bill> filterBills2 = cmbBills.stream()
                .filter(billPredicate)
                .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 5)
                .collect(Collectors.toList());
        if (filterBills2.size() == 1) {
            Bill cmb = filterBills2.getFirst();
            setMerge(cmb, 1);
            bills.forEach(bill -> {
//                if (bill.getSameBills() == null) {
//                    bill.setSameBills(new ArrayList<>());
//                }
//                cmb.setIsMerge(true);
//                bill.setIsMerge(true);
//                bill.getSameBills().add(cmb);
            });
            return;
        } else if (filterBills2.size() > 1) {
            log.error("匹配到多个费用：{}， \n{}", bills, filterBills2);
        }

        // 上面不行再用时间差60秒内比较
        List<Bill> filterBills3 = cmbBills.stream()
                .filter(billPredicate)
                .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 60)
                .toList();
        if (filterBills3.size() == 1) {
            Bill cmb = filterBills3.getFirst();
            setMerge(cmb, 1);
//            bills.forEach(bill -> {
//                if (bill.getSameBills() == null) {
//                    bill.setSameBills(new ArrayList<>());
//                }
//                cmb.setIsMerge(true);
//                bill.setIsMerge(true);
//                bill.getSameBills().add(cmb);
//            });
            return;
        } else if (filterBills2.size() > 1) {
            log.error("匹配到多个费用：{}， \n{}", bills, filterBills2);
        }

        // 上面不行再用时间差5分钟内比较
        List<Bill> filterBills4 = cmbBills.stream()
                .filter(billPredicate)
                .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), transactionTime).toSeconds()) <= 60 * 5)
                .toList();
        if (filterBills4.isEmpty()) {
            log.error("找不到匹配的招商银行账单记录：{}", bills);
        } else if (filterBills4.size() == 1) {
            Bill cmb = filterBills4.getFirst();
            setMerge(cmb, 1);
//            bills.forEach(bill -> {
//                if (bill.getSameBills() == null) {
//                    bill.setSameBills(new ArrayList<>());
//                }
//                cmb.setIsMerge(true);
//                bill.setIsMerge(true);
//                bill.getSameBills().add(cmb);
//            });
        } else {
            log.error("匹配到多个费用：{}， \n{}", bills, filterBills2);
        }
    }


    private static void mergeWxBill(WxBillInfo wxBillInfo, List<BaseBillInfo> otherBillInfos) {

        mergeWxAndWx(wxBillInfo, otherBillInfos.stream().filter(x -> x instanceof WxBillInfo).toList());

        for (Bill bill : wxBillInfo.getBills()) {
            if (BankEnum.CMB.equals(bill.getBank())) {
                mergeWxAndCmb(bill, otherBillInfos);
            }
        }
    }

    /**
     * 合并微信和微信
     * 根据账单单号和商户单号
     *
     * @param wxBillInfo
     * @param otherWxBillInfos
     */
    private static void mergeWxAndWx(WxBillInfo wxBillInfo, List<BaseBillInfo> otherWxBillInfos) {
        if (otherWxBillInfos == null || otherWxBillInfos.isEmpty()) {
            return;
        }

        List<Bill> otherBills = otherWxBillInfos.stream().flatMap(x -> x.getBills().stream()).toList();

        wxBillInfo.getBills().stream()
                .filter(sourceBill -> !sourceBill.isMerge())
                .forEach(currBill -> otherBills.stream()
                        .filter(otherBill -> currBill.getBillNo().equals(otherBill.getBillNo()))
                        .forEach(bill -> setMerge(bill, 0)));
    }

    private static void setMerge(Bill bill, Integer mergeType) {
        bill.setMerge(true);
        bill.setMergeType(mergeType);
    }

    private static void mergeWxAndCmb(Bill bill, List<BaseBillInfo> billInfos) {
        String cardNumber = bill.getBankAccountLast4Number();
        Optional<BaseBillInfo> first = billInfos.stream().filter(info -> {
            if (info instanceof CmbBillInfo cmbBillInfo) {
                return cardNumber.equals(cmbBillInfo.getBankAccountLast4Number());
            }
            return false;
        }).findFirst();

        if (first.isEmpty()) {
            log.error("找不到匹配的招商银行账单信息，卡号后四位：{}", cardNumber);
            return;
        }

        BaseBillInfo cmbBillInfo = first.get();
        List<Bill> cmbBills = cmbBillInfo.getBills();

        Predicate<Bill> billPredicate = cmb -> cardNumber.equals(cmb.getBankAccountLast4Number())
                && cmbPaymentModeList.contains(cmb.getTransactionType())
                && cmb.getAmount().compareTo(bill.getAmount()) == 0
                && cmb.getRemark().split("-")[0].equals("财付通");

        // 先用时间相等比较
        List<Bill> filterBills = cmbBills.stream()
                .filter(billPredicate)
                .filter(cmb -> cmb.getTransactionTime().isEqual(bill.getTransactionTime()))
                .toList();

        if (filterBills.size() == 1) {
            Bill cmb = filterBills.getFirst();
            setMerge(cmb, 1);
            return;
        }

        if (filterBills.size() > 1) {
            log.error("匹配到多个费用：{}， \n{}", bill, filterBills);
            return;
        }

        // 上面不行再用时间差1秒内比较
        List<Bill> filterBills2 = cmbBills.stream()
                .filter(billPredicate)
                .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), bill.getTransactionTime()).toSeconds()) <= 1)
                .collect(Collectors.toList());
        if (filterBills2.isEmpty()) {
            log.error("找不到匹配的招商银行账单记录：{}", bill);
        } else if (filterBills2.size() == 1) {
            Bill cmb = filterBills2.getFirst();
            setMerge(cmb, 1);
        } else {
            log.error("匹配到多个费用：{}， \n{}", bill, filterBills2);
        }
    }
}
