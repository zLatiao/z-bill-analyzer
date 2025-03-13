package com.z.bill.parser;

import com.z.bill.enums.BillSourceEnum;

public class FileNameParser {
    public static BillSourceEnum parse(String fileName) {
        if (fileName.contains("微信支付账单")) {
            return BillSourceEnum.WX;
        } else if (fileName.contains("alipay_record_")) {
            return BillSourceEnum.ALIPAY;
        } else if (fileName.contains("CMB_")) {
            return BillSourceEnum.CMB;
        } else {
            throw new RuntimeException("没有匹配的文件名");
        }
    }
}
