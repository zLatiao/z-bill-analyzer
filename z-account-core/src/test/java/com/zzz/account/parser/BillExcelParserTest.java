package com.zzz.account.parser;

import com.zzz.account.entity.AlipayBillRecord;
import com.zzz.account.parser.old.AlipayParser;

import java.io.File;
import java.util.List;

public class BillExcelParserTest {

    public void test_alipay_parser() {
        String path = "C:\\Users\\anjun\\Desktop\\账单\\alipay_record_20240516_211204.csv";
        List<AlipayBillRecord> dtos = new AlipayParser().parse(new File(path));
        System.out.println(dtos);
    }

}