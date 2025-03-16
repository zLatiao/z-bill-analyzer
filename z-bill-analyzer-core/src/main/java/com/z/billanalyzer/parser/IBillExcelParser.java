package com.z.billanalyzer.parser;

import com.z.billanalyzer.domain.bill.BaseBill;
import com.z.billanalyzer.domain.bill.BaseBillDetail;

import java.io.*;
import java.util.List;

/**
 * @author z-latiao
 * @since: 2025/2/26 15:26
 */
public interface IBillExcelParser<T extends BaseBill<DETAIL_TYPE>, DETAIL_TYPE extends BaseBillDetail, RESULT_TYPE> {
    default T parse(File file) throws IOException {
        try (InputStream is1 = new BufferedInputStream(new FileInputStream(file))) {
            return parse(is1);
        }
    }

    default T parse(InputStream is) {
        byte[] bytes;
        try {
            bytes = is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ByteArrayInputStream is1 = new ByteArrayInputStream(bytes);
        ByteArrayInputStream is2 = new ByteArrayInputStream(bytes);
        List<RESULT_TYPE> parseResults = parseRecords(is1);
        T billInfo = parseInfo(is2);
        List<DETAIL_TYPE> convert = convert(parseResults);
        billInfo.setBillDetails(convert);
        afterParse(billInfo);
        return billInfo;
    }

    /**
     * 解析的后置处理
     *
     * @param billInfo
     */
    default void afterParse(T billInfo) {
    }

    List<RESULT_TYPE> parseRecords(InputStream is);

    T parseInfo(InputStream is);

    List<DETAIL_TYPE> convert(List<RESULT_TYPE> billRecords);
}
