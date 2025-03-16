package com.z.billanalyzer;

import com.z.billanalyzer.domain.*;
import com.z.billanalyzer.domain.parse.AlipayBillParseResult;
import com.z.billanalyzer.domain.parse.CmbBillParseResult;
import com.z.billanalyzer.domain.parse.WxBillParseResult;
import com.z.billanalyzer.parser.AlipayBillExcelParser;
import com.z.billanalyzer.parser.CmbBillExcelParser;
import com.z.billanalyzer.parser.IBillExcelParser;
import com.z.billanalyzer.parser.WxBillExcelParser;
import com.z.billanalyzer.util.BillMergeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author z-latiao
 * @since 2025/2/26 17:25
 */
@Slf4j
public class MainNew {
    public static void main(String[] args) throws IOException {
        ArrayList<BaseBillInfo> list = new ArrayList<>();

        IBillExcelParser<WxBillInfo, WxBillParseResult> wxBillParser = new WxBillExcelParser();
        list.add(wxBillParser.parse(new File("C:\\Users\\Administrator\\Desktop\\账单\\微信支付账单(20240101-20240401)——【解压密码可在微信支付公众号查看】.csv")));
        list.add(wxBillParser.parse(new File("C:\\Users\\Administrator\\Desktop\\账单\\微信支付账单(20240304-20240604)——【解压密码可在微信支付公众号查看】.csv")));
        list.add(wxBillParser.parse(new File("C:\\Users\\Administrator\\Desktop\\账单\\微信支付账单(20240604-20240904)——【解压密码可在微信支付公众号查看】.csv")));
        list.add(wxBillParser.parse(new File("C:\\Users\\Administrator\\Desktop\\账单\\微信支付账单(20240904-20241204)——【解压密码可在微信支付公众号查看】.csv")));
        list.add(wxBillParser.parse(new File("C:\\Users\\Administrator\\Desktop\\账单\\微信支付账单(20241204-20250304)——【解压密码可在微信支付公众号查看】.csv")));

        IBillExcelParser<AlipayBillInfo, AlipayBillParseResult> alipayBillParser = new AlipayBillExcelParser();
        list.add(alipayBillParser.parse(new File("C:\\Users\\Administrator\\Desktop\\账单\\alipay_record_20250304_210655.csv")));
        list.add(alipayBillParser.parse(new File("C:\\Users\\Administrator\\Desktop\\账单\\alipay_record_20250304_210728.csv")));

        IBillExcelParser<CmbBillInfo, CmbBillParseResult> cmbBillParser = new CmbBillExcelParser();
        CmbBillInfo cmbBillInfo = cmbBillParser.parse(new File("C:\\Users\\Administrator\\Desktop\\账单\\CMB_6214--------9016_20240101_20250331.csv"));
        list.add(cmbBillInfo);

        BillMergeUtil.merge(list);
        log.debug("123123");

    }
}
