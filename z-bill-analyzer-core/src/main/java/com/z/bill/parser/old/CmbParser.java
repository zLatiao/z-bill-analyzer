package com.z.bill.parser.old;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.z.bill.entity.CmbBillRecord;
import com.z.bill.listener.BillExcelListener;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CmbParser implements BillExcelParser<CmbBillRecord> {

    static List<Integer> recordNumberList2 = List.of(1, 2, 3, 4, 5);
    static int stopNumber2 = 5;

    public static void main(String[] args) {
        List<String> strings = parseInfoByEasyExcel(new File("C:\\Users\\anjun\\Desktop\\账单\\CMB_6214--------9016_20230516_20240516.csv"));
        System.out.println();
    }

    @Override
    public List<CmbBillRecord> parse(File file) {
        return EasyExcel
                .read(file)
                .head(CmbBillRecord.class)
                .charset(Charset.forName("GBK"))
                .excelType(ExcelTypeEnum.CSV)
                .headRowNumber(8)
                .sheet()
                .doReadSync();
    }

    private static List<String> parseInfoByEasyExcel(File file) {
        List<String> dataList = new ArrayList<>();
        EasyExcel.read(file, new BillExcelListener(recordNumberList2, stopNumber2, dataList))
                .charset(Charset.forName("GBK"))
                .excelType(ExcelTypeEnum.CSV)
                .sheet()
                .doRead();
        return dataList;
    }

}
