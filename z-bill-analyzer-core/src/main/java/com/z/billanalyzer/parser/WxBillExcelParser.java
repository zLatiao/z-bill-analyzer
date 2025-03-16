package com.z.billanalyzer.parser;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.z.billanalyzer.constant.GlobalConstant;
import com.z.billanalyzer.domain.WxBill;
import com.z.billanalyzer.domain.WxBillDetail;
import com.z.billanalyzer.domain.parse.WxBillExcelParseResult;
import com.z.billanalyzer.enums.BankEnum;
import com.z.billanalyzer.listener.BillExcelListener;
import com.z.billanalyzer.util.BillConvertUtil;
import com.z.billanalyzer.util.ReUtil;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author z-latiao
 * @since 2025/2/26 15:28
 */
public class WxBillExcelParser implements IBillExcelParser<WxBill, WxBillDetail, WxBillExcelParseResult> {

    private static final List<Integer> excelReadNumbers = List.of(1, 2, 3, 4, 6, 7, 8, 9);

    private static final int excelStopNumber = 9;

    private static final Pattern namePattern = Pattern.compile("微信昵称：\\[(.*?)]");
    private static final Pattern timePattern = Pattern.compile("起始时间：\\[(.*?)] 终止时间：\\[(.*?)]");
    private static final Pattern exportTypePattern = Pattern.compile("导出类型：\\[(.*?)]");
    private static final Pattern exportTimePattern = Pattern.compile("导出时间：\\[(.*?)]");
    private static final Pattern countPattern = Pattern.compile("共(\\d+)笔记录");
    private static final Pattern incomePattern = Pattern.compile("收入：(\\d+)笔 (\\d+\\.\\d+)元");
    private static final Pattern outcomePattern = Pattern.compile("支出：(\\d+)笔 (\\d+\\.\\d+)元");
    private static final Pattern neutralTradingPattern = Pattern.compile("中性交易：(\\d+)笔 (\\d+\\.\\d+)元");
    private static final Map<Pattern, BiConsumer<Matcher, WxBill>> patternBiConsumerMap = Map.ofEntries(
            Map.entry(namePattern, (matcher, wxBillInfo) -> wxBillInfo.setWechatNickname(matcher.group(1))),
            Map.entry(timePattern, (matcher, wxBillInfo) -> wxBillInfo.setStartTime(LocalDateTime.parse(matcher.group(1), GlobalConstant.DATE_TIME_FORMATTER)).setEndTime(LocalDateTime.parse(matcher.group(2), GlobalConstant.DATE_TIME_FORMATTER))),
            Map.entry(exportTypePattern, (matcher, wxBillInfo) -> wxBillInfo.setExportType(matcher.group(1))),
            Map.entry(exportTimePattern, (matcher, wxBillInfo) -> wxBillInfo.setExportTime(LocalDateTime.parse(matcher.group(1), GlobalConstant.DATE_TIME_FORMATTER))),
            Map.entry(countPattern, (matcher, wxBillInfo) -> wxBillInfo.setTransactionCount(Integer.valueOf(matcher.group(1)))),
            Map.entry(incomePattern, (matcher, wxBillInfo) -> wxBillInfo.setIncomeCount(Integer.valueOf(matcher.group(1))).setIncomeAmount(new BigDecimal(matcher.group(2)))),
            Map.entry(outcomePattern, (matcher, wxBillInfo) -> wxBillInfo.setExpenseCount(Integer.valueOf(matcher.group(1))).setExpenseAmount(new BigDecimal(matcher.group(2)))),
            Map.entry(neutralTradingPattern, (matcher, wxBillInfo) -> wxBillInfo.setNeutralCount(Integer.valueOf(matcher.group(1))).setNeutralAmount(new BigDecimal(matcher.group(2))))
    );

    @Override
    public List<WxBillExcelParseResult> parseRecords(InputStream is) {
        // todo 这里可能要加个文件名判断是新的还是旧的
        return EasyExcel.read(is)
                .autoCloseStream(false)
                .head(WxBillExcelParseResult.class)
//                .charset(Charset.forName("GBK"))
                .excelType(ExcelTypeEnum.CSV)
                .headRowNumber(17)
                .sheet()
                .doReadSync();
    }

    @Override
    public WxBill parseInfo(InputStream is) {
        WxBill result = new WxBill();
        result.setIsBankBill(false);

        List<String> dataList = parseInfoByEasyExcel(is);
        for (String data : dataList) {
            // todo 可以用个类封装起来
            for (Map.Entry<Pattern, BiConsumer<Matcher, WxBill>> entry : patternBiConsumerMap.entrySet()) {
                Pattern key = entry.getKey();
                BiConsumer<Matcher, WxBill> value = entry.getValue();
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
//                .charset(Charset.forName("GBK"))
                .excelType(ExcelTypeEnum.CSV)
                .sheet()
                .doRead();
        return dataList;
    }

    @Override
    public List<WxBillDetail> convert(List<WxBillExcelParseResult> billRecords) {
        List<WxBillDetail> billDetails = billRecords.stream().map(BillConvertUtil::convert).toList();
        // TODO 2025/2/27 这里逻辑要不要改成到afterParse里去做
        for (WxBillDetail billDetail : billDetails) {
            if (billDetail.getPaymentMethod() == null) {
                continue;
            }
            Matcher matcher = GlobalConstant.CMB_PATTERN.matcher(billDetail.getPaymentMethod());
            if (matcher.find()) {
                billDetail.setPaymentModeBank(BankEnum.CMB);
                billDetail.setBankAccountLast4Number(matcher.group(1));
            }
        }
        return billDetails;
    }
}
