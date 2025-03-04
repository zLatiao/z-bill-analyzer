package com.zzz.account;

import com.zzz.account.entity.*;
import com.zzz.account.enums.BillSourceEnum;
import com.zzz.account.parser.AlipayBillExcelParser;
import com.zzz.account.parser.CmbBillExcelParser;
import com.zzz.account.parser.IBillExcelParser;
import com.zzz.account.parser.WxBillExcelParser;
import com.zzz.account.strategy.BillMergeStrategyNew;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
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
        List<BaseBillInfo> billInfos = params.stream().map(param -> enumMap.get(param.billSourceEnum()).parse(param.inputStream())).collect(Collectors.toList());
        List<Bill> bills = new BillMergeStrategyNew().merge(billInfos);
        return billInfos;
    }
}
