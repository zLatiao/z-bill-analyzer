package com.z.billanalyzer.util;

import com.z.billanalyzer.domain.*;
import com.z.billanalyzer.domain.parse.AlipayBillExcelParseResult;
import com.z.billanalyzer.domain.parse.CmbBillExcelParseResult;
import com.z.billanalyzer.domain.parse.WxBillExcelParseResult;
import com.z.billanalyzer.enums.BillSourceEnum;
import com.z.billanalyzer.enums.AmountTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BillConvertUtil {

    // todo 解析的时候就把银行卡号解析出来
    public static WxBillDetail convert(WxBillExcelParseResult billDTO) {
        String billAmount = billDTO.getAmount();
        String amountStr;
        if (billAmount.contains("¥")) {
            amountStr = billAmount.substring(billAmount.indexOf('¥') + 1).replace(",", "");
        } else {
            amountStr = billAmount.substring(billAmount.indexOf('￥') + 1).replace(",", "");
        }

        WxBillDetail billDetail = new WxBillDetail();
        billDetail.setAmount(new BigDecimal(amountStr));
        billDetail.setAmountType(AmountTypeEnum.getEnum(billDTO.getIncomeOrExpense()).getType());
        billDetail.setTransactionType(billDTO.getTransactionType() == null ? "无" : billDTO.getTransactionType());
        billDetail.setSource(BillSourceEnum.WX.ordinal());
        billDetail.setTransactionTime(billDTO.getTransactionTime());
        billDetail.setCounterparty(billDTO.getCounterparty());
        billDetail.setProduct(billDTO.getProduct());
        billDetail.setPaymentMethod(billDTO.getPaymentMethod());
        billDetail.setTransactionStatus(billDTO.getCurrentStatus());
        billDetail.setTransactionNo(billDTO.getTransactionNo());
        billDetail.setMerchantNo(billDTO.getMerchantNo());
        billDetail.setRemark(billDTO.getRemark());
        return billDetail;
    }

    public static List<WxBillDetail> convertListByWx(List<WxBillExcelParseResult> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }


    public static AlipayBillDetail convert(AlipayBillExcelParseResult billDTO) {
        AlipayBillDetail billDetail = new AlipayBillDetail();
        billDetail.setAmount(billDTO.getAmount());
        billDetail.setAmountType(AmountTypeEnum.getEnum(billDTO.getIncomeOrExpense()).getType());
        billDetail.setTransactionType(billDTO.getTransactionCategory());
        billDetail.setSource(BillSourceEnum.ALIPAY.ordinal());
        billDetail.setTransactionTime(billDTO.getTransactionTime());
        billDetail.setCounterparty(billDTO.getCounterparty());
        billDetail.setProduct(billDTO.getProduct());
        billDetail.setPaymentMethod(billDTO.getPaymentMethod());
        billDetail.setTransactionStatus(billDTO.getTransactionStatus());
        billDetail.setTransactionNo(billDTO.getTransactionOrderId());
        billDetail.setMerchantNo(billDTO.getMerchantOrderId());
        billDetail.setRemark(billDTO.getRemark());
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
     * @param billDTO
     * @return
     */
    public static CmbBillDetail convert(CmbBillExcelParseResult billDTO) {
        CmbBillDetail billDetail = new CmbBillDetail();
        if (billDTO.getIncome() != null) {
            billDetail.setAmount(billDTO.getIncome());
            billDetail.setAmountType(AmountTypeEnum.INCOME.getType());
        } else {
            billDetail.setAmount(billDTO.getExpense());
            billDetail.setAmountType(AmountTypeEnum.EXPENSE.getType());
        }
        billDetail.setTransactionType(billDTO.getTransactionType());
        billDetail.setSource(BillSourceEnum.CMB.ordinal());
        billDetail.setTransactionTime(LocalDateTime.parse((billDTO.getDate() + " " + billDTO.getTime()).replace("\t", ""), DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")));
        billDetail.setRemark(billDTO.getRemark());
        if (billDTO.getRemark() != null && !billDTO.getRemark().isBlank()) {
            String[] remarkArr = billDTO.getRemark().split("-");
            // 从备注里解析出交易对方
            billDetail.setCounterparty(remarkArr[remarkArr.length - 1]);
        }
        return billDetail;
    }

    public static List<CmbBillDetail> convertListByCmb(List<CmbBillExcelParseResult> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }
}
