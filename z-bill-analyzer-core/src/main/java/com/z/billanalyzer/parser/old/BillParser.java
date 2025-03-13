package com.z.billanalyzer.parser.old;

import cn.hutool.json.JSONUtil;
import com.z.billanalyzer.util.BillConvertUtil;
import com.z.billanalyzer.entity.BillParseDTO;
import com.z.billanalyzer.entity.Bill;
import com.z.billanalyzer.enums.BillSourceEnum;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class BillParser {
    public static void main(String[] args) throws IOException {
        List<BillParseDTO> billParseDTOS = List.of(new BillParseDTO(BillSourceEnum.WX, new File("C:\\Users\\anjun\\Desktop\\账单\\微信支付账单(20240216-20240516).csv")),
                new BillParseDTO(BillSourceEnum.ALIPAY, new File("C:\\Users\\anjun\\Desktop\\账单\\alipay_record_20250221_103151.csv")),
                new BillParseDTO(BillSourceEnum.CMB, new File("C:\\Users\\anjun\\Desktop\\账单\\CMB_6214--------9016_20230516_20240516.csv")));
        List<Bill> list = billParseDTOS.stream().map(BillParser::parse).map(BillConvertUtil::convert).flatMap(Collection::stream).toList();
        String stringPretty = JSONUtil.parse(list).toStringPretty();
        System.out.println(stringPretty);

    }

    public static List<?> parse(BillParseDTO billParseDTO) {
        return parse(billParseDTO.getBillSourceEnum(), billParseDTO.getFile());
    }

    public static List<?> parse(BillSourceEnum billSourceEnum, File file) {
        switch (billSourceEnum) {
            case WX -> {
                return new WxParser().parse(file);
            }
            case ALIPAY -> {
                return new AlipayParser().parse(file);

            }
            case CMB -> {
                return new CmbParser().parse(file);
            }
        }
        return null;
    }
}
