package com.z.billanalyzer;

import com.z.billanalyzer.domain.*;
import com.z.billanalyzer.domain.parse.BillExcelParseParam;
import com.z.billanalyzer.enums.BillSourceEnum;
import com.z.billanalyzer.parser.AlipayBillExcelParser;
import com.z.billanalyzer.parser.CmbBillExcelParser;
import com.z.billanalyzer.parser.IBillExcelParser;
import com.z.billanalyzer.parser.WxBillExcelParser;
import com.z.billanalyzer.util.BillMergeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ParserCore {
    private static EnumMap<BillSourceEnum, IBillExcelParser<? extends BaseBill<?>, ?, ?>> enumMap = new EnumMap<>(BillSourceEnum.class);

    static {
        enumMap.put(BillSourceEnum.WX, new WxBillExcelParser());
        enumMap.put(BillSourceEnum.ALIPAY, new AlipayBillExcelParser());
        enumMap.put(BillSourceEnum.CMB, new CmbBillExcelParser());
    }

    public static List<BaseBill<?>> parse(List<BillExcelParseParam> params) {
        List<BaseBill<?>> billInfos = params.stream()
                .map(param -> enumMap.get(param.billSourceEnum()).parse(param.inputStream()).setFileName(param.fileName()))
                .collect(Collectors.toList());
        BillMergeUtil.merge(billInfos);
        return billInfos;
    }
}
