package com.z.billanalyzer.parser.old;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.z.billanalyzer.entity.WxBillRecord;
import com.z.billanalyzer.listener.BillExcelListener;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信账单解析器
 * TODO csv文件如果保存过，解析时间会报错
 */
public class WxParser implements BillExcelParser<WxBillRecord> {

    static List<Long> recordNumberList = List.of(2L, 3L, 4L, 5L, 7L, 8L, 9L,
            10L);

    static List<Integer> recordNumberList2 = List.of(1, 2, 3, 4, 6, 7, 8, 9);

    static long stopNumber = 10;
    static int stopNumber2 = 9;

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\anjun\\Desktop\\账单\\微信支付账单(20240216-20240516).csv");
        List<WxBillRecord> bills = parseBillRecords(file);
        Map<String, String> infoMap = parseBillInfo(parseInfoByEasyExcel(file));
        System.out.println(1);
    }

    static Pattern namePattern = Pattern.compile("微信昵称：\\[(.*?)]");
    static Pattern timePattern = Pattern.compile("起始时间：\\[(.*?)] 终止时间：\\[(.*?)]");
    static Pattern exportTypePattern = Pattern.compile("导出类型：\\[(.*?)]");
    static Pattern exportTimePattern = Pattern.compile("导出时间：\\[(.*?)]");
    static Pattern countPattern = Pattern.compile("共(\\d+)笔记录");
    static Pattern incomePattern = Pattern.compile("收入：(\\d+)笔 (\\d+\\.\\d+)元");
    static Pattern outcomePattern = Pattern.compile("支出：(\\d+)笔 (\\d+\\.\\d+)元");
    static Pattern neutralTradingPattern = Pattern.compile("中性交易：(\\d+)笔 (\\d+\\.\\d+)元");
    static List<Pattern> patterns = List.of(
            namePattern,
            timePattern,
            timePattern,
            exportTypePattern,
            exportTimePattern,
            countPattern,
            incomePattern,
            outcomePattern,
            neutralTradingPattern
    );


    private static Map<String, String> parseBillInfo(List<String> dataList) throws IOException {

        Map<String, String> map = new LinkedHashMap<>();
        for (String str : dataList) {
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    String str1 = matcher.group(1);
                    if (pattern == namePattern) {
                        map.put("微信昵称", str1);
                    } else if (pattern == timePattern) {
                        String str2 = matcher.group(2);
                        map.put("起始时间", str1);
                        map.put("终止时间", str2);
                    } else if (pattern == exportTypePattern) {
                        map.put("导出类型", str1);
                    } else if (pattern == exportTimePattern) {
                        map.put("导出时间", str1);
                    } else if (pattern == countPattern) {
                        map.put("明细数量", str1);
                    } else if (pattern == incomePattern) {
                        String str2 = matcher.group(2);
                        map.put("收入数量", str1);
                        map.put("收入费用", str2);
                    } else if (pattern == outcomePattern) {
                        String str2 = matcher.group(2);
                        map.put("支出数量", str1);
                        map.put("支出费用", str2);
                    } else if (pattern == neutralTradingPattern) {
                        String str2 = matcher.group(2);
                        map.put("中性交易数量", str1);
                        map.put("中性交易费用", str2);
                    }
                    break;
                }
            }
        }
        return map;
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

    private static ArrayList<String> parseInfoByApacheCommons(File file) throws IOException {
        ArrayList<String> objects = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("GBK")))) {
            CSVParser parse = CSVFormat.DEFAULT.parse(bufferedReader);
            for (CSVRecord csvRecord : parse) {
                long recordNumber = csvRecord.getRecordNumber();
                if (recordNumber > stopNumber) {
                    break;
                }
                if (recordNumberList.contains(recordNumber)) {
                    objects.add(csvRecord.get(0));
                }
            }
        }
        return objects;
    }

    private static List<WxBillRecord> parseBillRecords(File file) {
        return EasyExcel.read(file).head(WxBillRecord.class).charset(Charset.forName("GBK")).excelType(ExcelTypeEnum.CSV).headRowNumber(17).sheet().doReadSync();
    }

    @Override
    public List<WxBillRecord> parse(File file) {
        return EasyExcel.read(file)
                .head(WxBillRecord.class)
                .charset(Charset.forName("GBK"))
                .excelType(ExcelTypeEnum.CSV)
                .headRowNumber(17)
                .sheet()
                .doReadSync();
    }

}
