package com.z.bill;

import com.z.bill.entity.*;
import com.z.bill.enums.BillSourceEnum;
import com.z.bill.parser.AlipayBillExcelParser;
import com.z.bill.parser.CmbBillExcelParser;
import com.z.bill.parser.IBillExcelParser;
import com.z.bill.parser.WxBillExcelParser;
import com.z.bill.util.BillMergeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ParserCore {
    private static EnumMap<BillSourceEnum, IBillExcelParser<? extends BaseBillInfo, ?>> enumMap = new EnumMap<>(BillSourceEnum.class);

    static {
        enumMap.put(BillSourceEnum.WX, new WxBillExcelParser());
        enumMap.put(BillSourceEnum.ALIPAY, new AlipayBillExcelParser());
        enumMap.put(BillSourceEnum.CMB, new CmbBillExcelParser());
    }

    public static List<BaseBillInfo> parse(List<BillExcelParseParam> params) {
        List<BaseBillInfo> billInfos = params.stream()
                .map(param -> enumMap.get(param.billSourceEnum()).parse(param.inputStream()).setFileName(param.fileName()))
                .collect(Collectors.toList());
        BillMergeUtil.merge(billInfos);
        return billInfos;
    }
}
