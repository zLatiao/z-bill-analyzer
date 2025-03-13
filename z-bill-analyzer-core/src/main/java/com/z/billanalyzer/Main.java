package com.z.billanalyzer;

import cn.hutool.json.JSONUtil;
import com.z.billanalyzer.entity.Bill;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author z-latiao
 * @since 2025/2/21 10:02
 */
public class Main {
    public static void main(String[] args) throws IOException {
//        List<BillParseDTO> billParseDTOS = List.of(new BillParseDTO(BillSourceEnum.WX, new File("C:\\Users\\anjun\\Desktop\\账单\\微信支付账单(20240216-20240516).csv")),
//                new BillParseDTO(BillSourceEnum.ALIPAY, new File("C:\\Users\\anjun\\Desktop\\账单\\alipay_record_20250221_103151.csv")),
//                new BillParseDTO(BillSourceEnum.CMB, new File("C:\\Users\\anjun\\Desktop\\账单\\CMB_6214--------9016_20230516_20240516.csv")));
//        List<Bill> list = billParseDTOS.stream().map(BillParser::parse).map(BillConvertor::covert).flatMap(Collection::stream).toList();

        String s = Files.readString(Path.of("C:\\Users\\anjun\\Desktop\\账单\\新建文本文档.txt"));
        List<Bill> list = JSONUtil.toList(s, Bill.class);

    }
}
