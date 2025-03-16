package com.z.billanalyzer.parser;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.z.billanalyzer.domain.CmbBill;
import com.z.billanalyzer.domain.CmbBillDetail;
import com.z.billanalyzer.domain.parse.CmbBillExcelParseResult;
import com.z.billanalyzer.listener.BillExcelListener;
import com.z.billanalyzer.util.BillConvertUtil;
import com.z.billanalyzer.util.ReUtil;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.z.billanalyzer.constant.GlobalConstant.DATE_FORMATTER;
import static com.z.billanalyzer.constant.GlobalConstant.DATE_TIME_FORMATTER;

/**
 * @author z-latiao
 * @since 2025/2/26 17:43
 */
public class CmbBillExcelParser implements IBillExcelParser<CmbBill, CmbBillDetail, CmbBillExcelParseResult> {

    private static final List<Integer> excelReadNumbers = List.of(1, 2, 3, 4, 5);

    private static final int excelStopNumber = 5;

    private static final Pattern accountPattern = Pattern.compile("# 账 {4}号: \\[(.*?)]");
    private static final Pattern currencyPattern = Pattern.compile("# 币 {4}种: \\[ {25}(.*?)]");
    private static final Pattern timePattern = Pattern.compile("# 起始日期: \\[(.*?)] {3}终止日期: \\[(.*?)]");
    private static final Pattern exportTimePattern = Pattern.compile("# 导出时间: \\[ {12}(.*?)]");
    private static final Pattern filterSettingsPattern = Pattern.compile("# 过滤设置: \\s*(.*)");
    private static final Pattern accountLast4NumberPattern = Pattern.compile(".*\\*+(\\d{4})");


    private static final Map<Pattern, BiConsumer<Matcher, CmbBill>> patternBiConsumerMap = Map.ofEntries(
            Map.entry(accountPattern, (matcher, cmbBillInfo) -> cmbBillInfo.setBankAccountNumber(matcher.group(1))),
            Map.entry(timePattern, (matcher, wxBillInfo) -> wxBillInfo.setStartTime(LocalDateTime.of(LocalDate.parse(matcher.group(1), DATE_FORMATTER), LocalTime.MIN)).setEndTime(LocalDateTime.of(LocalDate.parse(matcher.group(2), DATE_FORMATTER), LocalTime.MAX))),
            Map.entry(currencyPattern, (matcher, cmbBillInfo) -> cmbBillInfo.setCurrency(matcher.group(1))),
            Map.entry(exportTimePattern, (matcher, cmbBillInfo) -> cmbBillInfo.setExportTime(LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER))),
            Map.entry(filterSettingsPattern, (matcher, cmbBillInfo) -> cmbBillInfo.setFilterSettings(matcher.group(1)))
    );


    @Override
    public List<CmbBillExcelParseResult> parseRecords(InputStream is) {
        List<CmbBillExcelParseResult> cmbBillExcelParseResults = EasyExcel
                .read(is)
                .head(CmbBillExcelParseResult.class)
                .excelType(ExcelTypeEnum.CSV)
                .headRowNumber(7)
                .sheet()
                .doReadSync();
        // 最后两行是收入合计、支出合计
        return cmbBillExcelParseResults.subList(0, cmbBillExcelParseResults.size() - 2);
    }

    @Override
    public CmbBill parseInfo(InputStream is) {
        CmbBill result = new CmbBill();
        result.setIsBankBill(true);

        List<String> dataList = parseInfoByEasyExcel(is);
        for (String data : dataList) {
            for (Map.Entry<Pattern, BiConsumer<Matcher, CmbBill>> entry : patternBiConsumerMap.entrySet()) {
                Pattern key = entry.getKey();
                BiConsumer<Matcher, CmbBill> value = entry.getValue();
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
                .excelType(ExcelTypeEnum.CSV)
                .sheet()
                .doRead();
        return dataList;
    }

    @Override
    public void afterParse(CmbBill billInfo) {
        String bankAccountNumber = billInfo.getBankAccountNumber();
        Matcher matcher = accountLast4NumberPattern.matcher(bankAccountNumber);
        if (matcher.find()) {
            String group = matcher.group(1);
            billInfo.setBankAccountLast4Number(group);
            if (billInfo.getBillDetails() != null) {
                billInfo.getBillDetails().forEach(x -> x.setBankAccountLast4Number(group));
            }
        }
    }

    @Override
    public List<CmbBillDetail> convert(List<CmbBillExcelParseResult> billRecords) {
        return billRecords.stream().map(BillConvertUtil::convert).toList();
    }
}
