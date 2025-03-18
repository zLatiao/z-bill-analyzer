package com.z.billanalyzer.util;

import com.z.billanalyzer.domain.bill.AlipayBillDetail;
import com.z.billanalyzer.domain.bill.CmbBillDetail;
import com.z.billanalyzer.domain.bill.WxBillDetail;
import com.z.billanalyzer.domain.parse.AlipayBillExcelParseResult;
import com.z.billanalyzer.domain.parse.CmbBillExcelParseResult;
import com.z.billanalyzer.domain.parse.WxBillExcelParseResult;
import com.z.billanalyzer.enums.BillSourceEnum;
import com.z.billanalyzer.enums.AmountTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillConvertUtil {

    // todo 解析的时候就把银行卡号解析出来
    public static WxBillDetail convert(WxBillExcelParseResult parseResult) {
        String billAmount = parseResult.getAmount();
        String amountStr;
        if (billAmount.contains("¥")) {
            amountStr = billAmount.substring(billAmount.indexOf('¥') + 1).replace(",", "");
        } else {
            amountStr = billAmount.substring(billAmount.indexOf('￥') + 1).replace(",", "");
        }

        WxBillDetail billDetail = new WxBillDetail();
        billDetail.setAmount(new BigDecimal(amountStr));
        billDetail.setAmountType(AmountTypeEnum.getEnum(parseResult.getIncomeOrExpense()).getType());
        billDetail.setTransactionType(parseResult.getTransactionType() == null ? "无" : parseResult.getTransactionType());
        billDetail.setSource(BillSourceEnum.WX.ordinal());
        billDetail.setTransactionTime(parseResult.getTransactionTime());
        billDetail.setCounterparty(parseResult.getCounterparty());
        billDetail.setProduct(parseResult.getProduct());
        billDetail.setPaymentMethod(parseResult.getPaymentMethod());
        billDetail.setTransactionStatus(parseResult.getCurrentStatus());
        billDetail.setTransactionNo(parseResult.getTransactionNo());
        billDetail.setMerchantNo(parseResult.getMerchantNo());
        billDetail.setRemark(parseResult.getRemark());
        return billDetail;
    }

    public static List<WxBillDetail> convertListByWx(List<WxBillExcelParseResult> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }


    public static AlipayBillDetail convert(AlipayBillExcelParseResult parseResult) {
        AlipayBillDetail billDetail = new AlipayBillDetail();
        billDetail.setAmount(parseResult.getAmount());
        billDetail.setAmountType(AmountTypeEnum.getEnum(parseResult.getIncomeOrExpense()).getType());
        billDetail.setTransactionType(parseResult.getTransactionCategory());
        billDetail.setSource(BillSourceEnum.ALIPAY.ordinal());
        billDetail.setTransactionTime(parseResult.getTransactionTime());
        billDetail.setCounterparty(parseResult.getCounterparty());
        billDetail.setProduct(parseResult.getProduct());
        billDetail.setPaymentMethod(parseResult.getPaymentMethod());
        billDetail.setTransactionStatus(parseResult.getTransactionStatus());
        billDetail.setTransactionNo(parseResult.getTransactionOrderId());
        billDetail.setMerchantNo(parseResult.getMerchantOrderId());
        billDetail.setRemark(parseResult.getRemark());
        return billDetail;
    }

    public static List<AlipayBillDetail> convertListByAlipay(List<AlipayBillExcelParseResult> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }

    /**
     * 招行的交易备注有一些规律：
     * 交易备注普遍采用「支付平台-应用场景-商户名称」的三级结构（例如："美团-美团外卖App袁记云饺"），层级间用短横线分隔。其中：
     * 2级	平台-商户	支付宝-高德打车
     * 3级	平台-场景-商户	美团-美团外卖App袁记云饺
     * 4级	平台-支付方式-场景-商户	财付通-微信支付-停车场-捷顺
     *
     * @param parseResult
     * @return
     */
    public static CmbBillDetail convert(CmbBillExcelParseResult parseResult) {
        CmbBillDetail billDetail = new CmbBillDetail();
        if (parseResult.getIncome() != null) {
            billDetail.setAmount(parseResult.getIncome());
            billDetail.setAmountType(AmountTypeEnum.INCOME.getType());
        } else {
            billDetail.setAmount(parseResult.getExpense());
            billDetail.setAmountType(AmountTypeEnum.EXPENSE.getType());
        }
        billDetail.setBalance(parseResult.getBalance());
        billDetail.setTransactionType(parseResult.getTransactionType());
        billDetail.setSource(BillSourceEnum.CMB.ordinal());
        billDetail.setTransactionTime(LocalDateTime.parse((parseResult.getDate() + " " + parseResult.getTime()).replace("\t", ""), DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")));
        billDetail.setRemark(parseResult.getRemark());
        if (parseResult.getRemark() != null && !parseResult.getRemark().isBlank()) {
            String[] remarkArr = parseResult.getRemark().split("-");
            // 从备注里解析出交易对方
            billDetail.setCounterparty(remarkArr[remarkArr.length - 1]);
        }
        return billDetail;
    }

    public static List<CmbBillDetail> convertListByCmb(List<CmbBillExcelParseResult> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }
}
