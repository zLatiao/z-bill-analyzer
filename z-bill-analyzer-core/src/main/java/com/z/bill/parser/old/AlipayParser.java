package com.z.bill.parser.old;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.z.bill.entity.AlipayBillRecord;
import com.z.bill.listener.BillExcelListener;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class AlipayParser implements BillExcelParser<AlipayBillRecord> {

    public static void main(String[] args) {
//        List<AlipayBillDTO> dtos = new AlipayParser().parse(new File("C:\\Users\\anjun\\Desktop\\账单\\alipay_record_20250221_103151.csv"));
//        System.out.println(dtos);

        List<String> x = parseInfoByEasyExcel(new File("C:\\Users\\anjun\\Desktop\\账单\\alipay_record_20250221_103151.csv"));
        // todo
        System.out.println(x);
    }

    @Override
    public List<AlipayBillRecord> parse(File file) {
        return EasyExcel.read(file)
                .head(AlipayBillRecord.class)
                .charset(Charset.forName("GBK"))
                .excelType(ExcelTypeEnum.CSV)
                // todo 实际上是第25行，奇怪了
                .headRowNumber(23)
                .sheet()
                .doReadSync();
    }

    private final static List<Integer> readRowList = List.of(2, 3, 4, 5, 6, 7, 8, 9, 10);

    private static List<String> parseInfoByEasyExcel(File file) {
        List<String> dataList = new ArrayList<>();
        EasyExcel.read(file, new BillExcelListener(readRowList, 10, dataList))
                .charset(Charset.forName("GBK"))
                .excelType(ExcelTypeEnum.CSV)
                .sheet()
                .doRead();
        return dataList;
    }


}
