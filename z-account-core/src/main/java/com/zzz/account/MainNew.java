package com.zzz.account;

import com.zzz.account.entity.*;
import com.zzz.account.parser.AlipayBillExcelParser;
import com.zzz.account.parser.CmbBillExcelParser;
import com.zzz.account.parser.IBillExcelParser;
import com.zzz.account.parser.WxBillExcelParser;
import com.zzz.account.strategy.BillMergeStrategyNew;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author z-latiao
 * @since 2025/2/26 17:25
 */
@Slf4j
public class MainNew {
    public static void main(String[] args) throws IOException {
        IBillExcelParser<WxBillInfo, WxBillRecord> wxBillParser = new WxBillExcelParser();
        WxBillInfo wxBillInfo = wxBillParser.parse(new File("C:\\Users\\anjun\\Desktop\\账单\\微信支付账单(20240216-20240516).csv"));
        log.info("{}", wxBillInfo);

        IBillExcelParser<AlipayBillInfo, AlipayBillRecord> alipayBillParser = new AlipayBillExcelParser();
        AlipayBillInfo alipayBillInfo = alipayBillParser.parse(new File("C:\\Users\\anjun\\Desktop\\账单\\alipay_record_20250221_103151.csv"));
        log.info("{}", alipayBillInfo);


        IBillExcelParser<CmbBillInfo, CmbBillRecord> cmbBillParser = new CmbBillExcelParser();
        CmbBillInfo cmbBillInfo = cmbBillParser.parse(new File("C:\\Users\\anjun\\Desktop\\账单\\CMB_6214--------9016_20230516_20240516.csv"));
        log.info("{}", cmbBillInfo);

        List<BaseBillInfo> billInfos = List.of(wxBillInfo, alipayBillInfo, cmbBillInfo);
        List<Bill> bills = new BillMergeStrategyNew().merge(billInfos);
        log.debug("123123");

    }
}
