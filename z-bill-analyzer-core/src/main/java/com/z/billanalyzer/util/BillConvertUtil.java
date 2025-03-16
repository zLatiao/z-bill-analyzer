package com.z.billanalyzer.util;

import com.z.billanalyzer.domain.BillDetail;
import com.z.billanalyzer.domain.parse.AlipayBillParseResult;
import com.z.billanalyzer.domain.parse.CmbBillParseResult;
import com.z.billanalyzer.domain.parse.WxBillParseResult;
import com.z.billanalyzer.enums.BillSourceEnum;
import com.z.billanalyzer.enums.AmountTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillConvertUtil {

    public static List<BillDetail> convert(List<?> bills) {
        return bills.stream().map(bill -> switch (bill) {
            case WxBillParseResult wxBillParseResult -> convert(wxBillParseResult);
            case AlipayBillParseResult alipayBillParseResult -> convert(alipayBillParseResult);
            case CmbBillParseResult cmbBillParseResult -> convert(cmbBillParseResult);
            case null, default ->
                    throw new RuntimeException("wrong type: " + bills.getClass());
        }).toList();
    }

    // todo 解析的时候就把银行卡号解析出来
    public static BillDetail convert(WxBillParseResult billDTO) {
        String billAmount = billDTO.getAmount();
        String amountStr;
        if (billAmount.contains("¥")) {
            amountStr = billAmount.substring(billAmount.indexOf('¥') + 1).replace(",", "");
        } else {
            amountStr = billAmount.substring(billAmount.indexOf('￥') + 1).replace(",", "");
        }

        BillDetail billDetail = new BillDetail();
        billDetail.setAmount(new BigDecimal(amountStr));
        billDetail.setAmountType(AmountTypeEnum.getEnum(billDTO.getIncomeOrExpense()).getType());
        billDetail.setTransactionType(billDTO.getTransactionType() == null ? "无" : billDTO.getTransactionType());
        billDetail.setSource(BillSourceEnum.WX.ordinal());
        billDetail.setTransactionTime(billDTO.getTransactionTime());
        billDetail.setCounterparty(billDTO.getCounterparty());
        billDetail.setProduct(billDTO.getProduct());
        billDetail.setPaymentMode(billDTO.getPaymentMethod());
        billDetail.setTransactionStatus(billDTO.getCurrentStatus());
        billDetail.setBillNo(billDTO.getTransactionNo());
        billDetail.setMerchantNo(billDTO.getMerchantNo());
        billDetail.setRemark(billDTO.getRemark());
        return billDetail;
    }

    public static List<BillDetail> convertListByWx(List<WxBillParseResult> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }


    public static BillDetail convert(AlipayBillParseResult billDTO) {
        BillDetail billDetail = new BillDetail();
        billDetail.setAmount(billDTO.getAmount());
        billDetail.setAmountType(AmountTypeEnum.getEnum(billDTO.getIncomeOrExpense()).getType());
        billDetail.setTransactionType(billDTO.getTransactionCategory());
        billDetail.setSource(BillSourceEnum.ALIPAY.ordinal());
        billDetail.setTransactionTime(billDTO.getTransactionTime());
        billDetail.setCounterparty(billDTO.getCounterparty());
        billDetail.setProduct(billDTO.getProduct());
        billDetail.setPaymentMode(billDTO.getPaymentMethod());
        billDetail.setTransactionStatus(billDTO.getTransactionStatus());
        billDetail.setBillNo(billDTO.getTransactionOrderId());
        billDetail.setMerchantNo(billDTO.getMerchantOrderId());
        billDetail.setRemark(billDTO.getRemark());
        return billDetail;
    }

    public static List<BillDetail> convertListByAlipay(List<AlipayBillParseResult> billDTOList) {
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
    public static BillDetail convert(CmbBillParseResult billDTO) {
        BillDetail billDetail = new BillDetail();
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

    public static List<BillDetail> convertListByCmb(List<CmbBillParseResult> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }
}
