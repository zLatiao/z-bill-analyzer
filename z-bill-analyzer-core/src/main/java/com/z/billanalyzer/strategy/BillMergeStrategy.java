//package com.z.billanalyzer.strategy;
//
//import cn.hutool.json.JSONUtil;
//import com.z.billanalyzer.constant.GlobalConstant;
//import com.z.billanalyzer.entity.Bill;
//import com.z.billanalyzer.enums.BillSourceEnum;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.regex.Matcher;
//
///**
// * 微信/支付宝账单和银行账单可能会有重合记录，需要合并/去重
// * <p>
// * 微信账单：
// *  可以通过支付方式判断是零钱支付还是银行卡支付，银行卡支付的话用来去重。
// *  收入：支付方式为/
// *  支出：支付方式有 零钱、招商银行储蓄卡(xxxx)、中国银行储蓄卡(xxxx)
// * 支付宝账单：
// *  收/付款方式同上
// *  TODO 退款的也是不计收支
// *  退款和理财的要合并。退款的订单号好像是有关联_
// * 招商银行：交易备注里面或许可以解析出是微信还是支付宝的、交易类型未知。支付用朝朝宝的话，朝朝宝赎回有一笔收入，支付有一笔支出。可能会重复，这点也要考虑合并。
// * <p>
// * 招商银行的备注和微信的交易对方/不同，例如：招行的备注是【财付通-广东罗森】，微信的交易对方是【LAWSON壹方中心店消费】
// */
//public class BillMergeStrategy {
//    public static void main(String[] args) throws IOException {
//        String s = Files.readString(Path.of("C:\\Users\\anjun\\Desktop\\账单\\新建文本文档.txt"));
//        List<Bill> list = JSONUtil.toList(s, Bill.class);
//
//        List<Bill> wxBills = list.stream().filter(x -> BillSourceEnum.WX.ordinal() == x.getSource()).toList();
//        List<Bill> alipayBills = list.stream().filter(x -> BillSourceEnum.ALIPAY.ordinal() == x.getSource()).toList();
//        List<Bill> cmbBills = list.stream().filter(x -> BillSourceEnum.CMB.ordinal() == x.getSource()).peek(x -> x.setBankAccountLast4Number("9016")).toList();
//        mergeWxAndCMB(wxBills, cmbBills);
//        System.out.println();
//
//        wxBills.forEach(bill -> {
//            if (bill.getPaymentMode() == null) {
//                return;
//            }
//            Matcher matcher = GlobalConstant.CMB_PATTERN.matcher(bill.getPaymentMode());
//            if (matcher.find()) {
//                String cardNumber = matcher.group(1);
//                if (cardNumber == null || cardNumber.isBlank()) {
//                    return;
//                }
////                System.out.println(cardNumber);
//                Optional<Bill> first = cmbBills.stream()
//                        .filter(cmb -> cardNumber.equals(cmb.getBankAccountLast4Number()))
//                        .filter(x -> x.getTransactionTime().isEqual(bill.getTransactionTime()))
//                        .findFirst();
//            }
//        });
//    }
//
//    static boolean validateAllTimeRight(List<Bill> wxBills, List<Bill> cmbBills) {
//        boolean b = wxBills.stream()
//                .filter(wx -> wx.getPaymentMode() != null)
//                .allMatch(wx -> {
//                    Matcher matcher = GlobalConstant.CMB_PATTERN.matcher(wx.getPaymentMode());
//                    if (!matcher.find()) {
//                        return true;
//                    }
//                    // 先用时间==比较
//                    String cardNumber = matcher.group(1);
//                    Optional<Bill> optional = cmbBills.stream()
//                            .filter(cmb -> cardNumber.equals(cmb.getBankAccountLast4Number()))
//                            .filter(x -> x.getTransactionTime().isEqual(wx.getTransactionTime()))
//                            .filter(x -> x.getAmount().compareTo(wx.getAmount()) == 0)
//                            .findFirst();
//                    if (optional.isPresent()) {
//                        Bill cmb = optional.get();
//                        if (wx.getSimilarBills() == null) {
//                            wx.setSimilarBills(new ArrayList<>());
//                        }
//                        wx.getSimilarBills().add(cmb);
//                        return true;
//                    }
//
//                    // 上面不行再用时间差1秒内比较
//                    Optional<Bill> optional1 = cmbBills.stream()
//                            .filter(cmb -> cardNumber.equals(cmb.getBankAccountLast4Number()))
//                            .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), wx.getTransactionTime()).toSeconds()) <= 1)
//                            .filter(x -> x.getAmount().compareTo(wx.getAmount()) == 0)
//                            .findFirst();
//                    boolean present = optional1.isPresent();
//                    if (!present) {
//                        System.out.println();
//                    } else {
//                        Bill cmb = optional1.get();
//                        if (wx.getSimilarBills() == null) {
//                            wx.setSimilarBills(new ArrayList<>());
//                        }
//                        wx.getSimilarBills().add(cmb);
//                    }
//                    return present;
//                });
//        System.out.println(b);
//        return b;
//    }
//
//    static List<String> cmbPaymentModeList = List.of("网联退款", "网联协议支付", "银联快捷支付");
//
//    /**
//     * 微信和招商银行
//     *  1. 时间
//     *  2. 支付方式
//     *  3. 银行卡号
//     *  4. 金额
//     *  5. 备注
//     * @param wxBills
//     * @param cmbBills
//     */
//    public static void mergeWxAndCMB(List<Bill> wxBills, List<Bill> cmbBills) {
//        wxBills.stream()
//                .filter(wx -> wx.getPaymentMode() != null)
//                .forEach(wxBill -> {
//                    Matcher matcher = GlobalConstant.CMB_PATTERN.matcher(wxBill.getPaymentMode());
//                    if (!matcher.find()) {
//                        return;
//                    }
//                    // 先用时间==比较
//                    String cardNumber = matcher.group(1);
//                    Optional<Bill> optional = cmbBills.stream()
//                            .filter(cmb -> cardNumber.equals(cmb.getBankAccountLast4Number()))
//                            .filter(x -> cmbPaymentModeList.contains(x.getPaymentMode()))
//                            .filter(x -> x.getTransactionTime().isEqual(wxBill.getTransactionTime()))
//                            .filter(x -> x.getAmount().compareTo(wxBill.getAmount()) == 0)
//                            .filter(x->x.getRemark().split("-")[0].equals("财付通"))
//                            .findFirst();
//                    if (optional.isPresent()) {
//                        Bill cmb = optional.get();
//                        if (wxBill.getSimilarBills() == null) {
//                            wxBill.setSimilarBills(new ArrayList<>());
//                        }
//                        wxBill.getSimilarBills().add(cmb);
//                        return;
//                    }
//
//                    // 上面不行再用时间差1秒内比较
//                    Optional<Bill> optional1 = cmbBills.stream()
//                            .filter(cmb -> cardNumber.equals(cmb.getBankAccountLast4Number()))
//                            .filter(x -> cmbPaymentModeList.contains(x.getPaymentMode()))
//                            .filter(x -> Math.abs(Duration.between(x.getTransactionTime(), wxBill.getTransactionTime()).toSeconds()) <= 1)
//                            .filter(x -> x.getAmount().compareTo(wxBill.getAmount()) == 0)
//                            .filter(x->x.getRemark().split("-")[0].equals("财付通"))
//                            .findFirst();
//                    boolean present = optional1.isPresent();
//                    if (!present) {
//                        System.out.println();
//                    } else {
//                        Bill cmb = optional1.get();
//                        if (wxBill.getSimilarBills() == null) {
//                            wxBill.setSimilarBills(new ArrayList<>());
//                        }
//                        wxBill.getSimilarBills().add(cmb);
//                    }
//                });
//    }
//}
