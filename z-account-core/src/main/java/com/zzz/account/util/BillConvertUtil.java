package com.zzz.account.util;

import com.zzz.account.entity.AlipayBillRecord;
import com.zzz.account.entity.CmbBillRecord;
import com.zzz.account.entity.WxBillRecord;
import com.zzz.account.entity.Bill;
import com.zzz.account.enums.BillSourceEnum;
import com.zzz.account.enums.AmountTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillConvertUtil {

    public static List<Bill> convert(List<?> bills) {
        return bills.stream().map(bill -> switch (bill) {
            case WxBillRecord wxBillRecord -> convert(wxBillRecord);
            case AlipayBillRecord alipayBillRecord -> convert(alipayBillRecord);
            case CmbBillRecord cmbBillRecord -> convert(cmbBillRecord);
            case null, default ->
                    throw new RuntimeException("wrong type: " + bills.getClass());
        }).toList();
    }

    // todo 解析的时候就把银行卡号解析出来
    public static Bill convert(WxBillRecord billDTO) {
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

    public static List<Bill> convertListByWx(List<WxBillRecord> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }


    public static Bill convert(AlipayBillRecord billDTO) {
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

    public static List<Bill> convertListByAlipay(List<AlipayBillRecord> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }

    // todo 解析的时候就把银行卡号解析出来
    public static Bill convert(CmbBillRecord billDTO) {
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
        return bill;
    }

    public static List<Bill> convertListByCmb(List<CmbBillRecord> billDTOList) {
        return billDTOList.stream().map(BillConvertUtil::convert).toList();
    }
}
