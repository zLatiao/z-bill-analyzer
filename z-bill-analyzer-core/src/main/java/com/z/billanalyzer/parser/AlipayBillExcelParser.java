package com.z.billanalyzer.parser;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.z.billanalyzer.constant.GlobalConstant;
import com.z.billanalyzer.domain.AlipayBillInfo;
import com.z.billanalyzer.domain.parse.AlipayBillParseResult;
import com.z.billanalyzer.domain.Bill;
import com.z.billanalyzer.enums.BankEnum;
import com.z.billanalyzer.listener.BillExcelListener;
import com.z.billanalyzer.util.ReUtil;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.z.billanalyzer.constant.GlobalConstant.DATE_TIME_FORMATTER;

/**
 * @author z-latiao
 * @since 2025/2/26 15:58
 */
public class AlipayBillExcelParser implements IBillExcelParser<AlipayBillInfo, AlipayBillParseResult> {
    private static final List<Integer> excelReadNumbers = List.of(2, 3, 4, 5, 6, 7, 8, 9, 10);

    private static final int excelStopNumber = 10;

    private static final Pattern namePattern = Pattern.compile("姓名：\\s*(.*)");
    private static final Pattern accountPattern = Pattern.compile("支付宝账户：\\s*(.*)");
    private static final Pattern timePattern = Pattern.compile("起始时间：\\[(.*?)] {4}终止时间：\\[(.*?)]");
    private static final Pattern exportTypePattern = Pattern.compile("导出交易类型：\\[(.*?)]");
    private static final Pattern exportTimePattern = Pattern.compile("导出时间：\\[(.*?)]");
    private static final Pattern countPattern = Pattern.compile("共(\\d+)笔记录");
    private static final Pattern incomePattern = Pattern.compile("收入：(\\d+)笔 (\\d+\\.\\d+)元");
    private static final Pattern outcomePattern = Pattern.compile("支出：(\\d+)笔 (\\d+\\.\\d+)元");
    private static final Pattern neutralTradingPattern = Pattern.compile("不计收支：(\\d+)笔 (\\d+\\.\\d+)元");
    private static final Map<Pattern, BiConsumer<Matcher, AlipayBillInfo>> patternBiConsumerMap = Map.ofEntries(
            Map.entry(namePattern, (matcher, alipayBillInfo) -> alipayBillInfo.setName(matcher.group(1))),
            Map.entry(accountPattern, (matcher, alipayBillInfo) -> alipayBillInfo.setAlipayAccount(matcher.group(1))),
            Map.entry(timePattern, (matcher, wxBillInfo) -> wxBillInfo.setStartTime(LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER)).setEndTime(LocalDateTime.parse(matcher.group(2), DATE_TIME_FORMATTER))),
            Map.entry(exportTypePattern, (matcher, alipayBillInfo) -> alipayBillInfo.setExportType(matcher.group(1))),
            Map.entry(exportTimePattern, (matcher, alipayBillInfo) -> alipayBillInfo.setExportTime(LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER))),
            Map.entry(countPattern, (matcher, alipayBillInfo) -> alipayBillInfo.setTransactionCount(Integer.valueOf(matcher.group(1)))),
            Map.entry(incomePattern, (matcher, alipayBillInfo) -> alipayBillInfo.setIncomeCount(Integer.valueOf(matcher.group(1))).setIncomeAmount(new BigDecimal(matcher.group(2)))),
            Map.entry(outcomePattern, (matcher, alipayBillInfo) -> alipayBillInfo.setExpenseCount(Integer.valueOf(matcher.group(1))).setExpenseAmount(new BigDecimal(matcher.group(2)))),
            Map.entry(neutralTradingPattern, (matcher, alipayBillInfo) -> alipayBillInfo.setNeutralCount(Integer.valueOf(matcher.group(1))).setNeutralAmount(new BigDecimal(matcher.group(2))))
    );

    @Override
    public List<AlipayBillParseResult> parseRecords(InputStream is) {
        return EasyExcel.read(is)
                .head(AlipayBillParseResult.class)
                .autoCloseStream(false)
                .charset(Charset.forName("GBK"))
                .excelType(ExcelTypeEnum.CSV)
                // todo 实际上是第25行，奇怪了
                .headRowNumber(23)
                .sheet()
                .doReadSync();
    }

    @Override
    public AlipayBillInfo parseInfo(InputStream is) {
        AlipayBillInfo result = new AlipayBillInfo();
        result.setIsBankBill(false);

        List<String> dataList = parseInfoByEasyExcel(is);
        for (String data : dataList) {
            for (Map.Entry<Pattern, BiConsumer<Matcher, AlipayBillInfo>> entry : patternBiConsumerMap.entrySet()) {
                Pattern key = entry.getKey();
                BiConsumer<Matcher, AlipayBillInfo> value = entry.getValue();
                if (ReUtil.findAll(key, data, matcher -> value.accept(matcher, result))) {
                    break;
                }
            }
        }

        return result;
    }

    private static List<String> parseInfoByEasyExcel(InputStream is) {
        List<String> dataList = new ArrayList<>();
        EasyExcel.read(is, new BillExcelListener(excelReadNumbers, excelStopNumber, dataList))
                .autoCloseStream(false)
                .charset(Charset.forName("GBK"))
                .excelType(ExcelTypeEnum.CSV)
                .sheet()
                .doRead();
        return dataList;
    }

    @Override
    public List<Bill> convert(List<AlipayBillParseResult> billRecords) {
        List<Bill> bills = IBillExcelParser.super.convert(billRecords);
        // TODO 2025/2/27 这里逻辑要不要改成到afterParse里去做
        for (Bill bill : bills) {
            if (bill.getPaymentMode() == null) {
                continue;
            }
            Matcher matcher = GlobalConstant.CMB_PATTERN.matcher(bill.getPaymentMode());
            if (matcher.find()) {
                bill.setBank(BankEnum.CMB);
                bill.setBankAccountLast4Number(matcher.group(1));
            }
        }
        return bills;
    }
}
