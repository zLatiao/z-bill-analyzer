package com.z.billanalyzer.util;

import com.z.billanalyzer.domain.parse.AlipayBillParseResult;
import com.z.billanalyzer.domain.parse.CmbBillParseResult;
import com.z.billanalyzer.domain.parse.WxBillParseResult;
import com.z.billanalyzer.domain.Bill;
import com.z.billanalyzer.enums.BillSourceEnum;
import com.z.billanalyzer.enums.AmountTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillConvertUtil {

    public static List<Bill> convert(List<?> bills) {
        return bills.stream().map(bill -> switch (bill) {
            case WxBillParseResult wxBillParseResult -> convert(wxBillParseResult);
            case AlipayBillParseResult alipayBillParseResult -> convert(alipayBillParseResult);
            case CmbBillParseResult cmbBillParseResult -> convert(cmbBillParseResult);
            case null, default ->
                    throw new RuntimeException("wrong type: " + bills.getClass());
        }).toList();
    }

    // todo 解析的时候就把银行卡号解析出来
    public static Bill convert(WxBillParseResult billDTO) {
        String billAmount = billDTO.getAmount();
        String amountStr;
        if (billAmount.contains("¥")) {
            amountStr = billAmount.substring(billAmount.indexOf('¥') + 1).replace(",", "");
        } else {
            amountStr = billAmount.substring(billAmount.indexOf('￥') + 1).replace(",", "");
        }

        Bill bill = new Bill();
        bill.setAmount(new BigDecimal(amountStr));
        bill.setAmountType(AmountTypeEnum.getEnum(billDTO.getIncomeOrExpense()).getType());
        bill.setTransactionType(billDTO.getTransactionType() == null ? "无" : billDTO.getTransactionType());
        bill.setSource(BillSourceEnum.WX.ordinal());
        bill.setTransactionTime(billDTO.getTransactionTime());
        bill.setCounterparty(billDTO.getCounterparty());
        bill.setProduct(billDTO.getProduct());
        bill.setPaymentMode(billDTO.getPaymentMethod());
        bill.setTransactionStatus(billDTO.getCurrentStatus());
        bill.setBillNo(billDTO.getTransactionNo());
        bill.setMerchantNo(billDTO.getMerchantNo());
        bill.setRemark(billDTO.getRemark());
        return bill;
    }

    public static List<Bill> convertListByWx(List<WxBillParseResult> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }


    public static Bill convert(AlipayBillParseResult billDTO) {
        Bill bill = new Bill();
        bill.setAmount(billDTO.getAmount());
        bill.setAmountType(AmountTypeEnum.getEnum(billDTO.getIncomeOrExpense()).getType());
        bill.setTransactionType(billDTO.getTransactionCategory());
        bill.setSource(BillSourceEnum.ALIPAY.ordinal());
        bill.setTransactionTime(billDTO.getTransactionTime());
        bill.setCounterparty(billDTO.getCounterparty());
        bill.setProduct(billDTO.getProduct());
        bill.setPaymentMode(billDTO.getPaymentMethod());
        bill.setTransactionStatus(billDTO.getTransactionStatus());
        bill.setBillNo(billDTO.getTransactionOrderId());
        bill.setMerchantNo(billDTO.getMerchantOrderId());
        bill.setRemark(billDTO.getRemark());
        return bill;
    }

    public static List<Bill> convertListByAlipay(List<AlipayBillParseResult> billDTOList) {
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
    public static Bill convert(CmbBillParseResult billDTO) {
        Bill bill = new Bill();
        if (billDTO.getIncome() != null) {
            bill.setAmount(billDTO.getIncome());
            bill.setAmountType(AmountTypeEnum.INCOME.getType());
        } else {
            bill.setAmount(billDTO.getExpense());
            bill.setAmountType(AmountTypeEnum.EXPENSE.getType());
        }
        bill.setTransactionType(billDTO.getTransactionType());
        bill.setSource(BillSourceEnum.CMB.ordinal());
        bill.setTransactionTime(LocalDateTime.parse((billDTO.getDate() + " " + billDTO.getTime()).replace("\t", ""), DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")));
        bill.setRemark(billDTO.getRemark());
        if (billDTO.getRemark() != null && !billDTO.getRemark().isBlank()) {
            String[] remarkArr = billDTO.getRemark().split("-");
            // 从备注里解析出交易对方
            bill.setCounterparty(remarkArr[remarkArr.length - 1]);
        }
        return bill;
    }

    public static List<Bill> convertListByCmb(List<CmbBillParseResult> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }
}
