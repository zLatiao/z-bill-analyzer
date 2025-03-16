package com.z.billanalyzer.parser;

import com.z.billanalyzer.domain.bill.BaseBill;
import com.z.billanalyzer.domain.bill.BaseBillDetail;

import java.io.*;
import java.util.List;

/**
 * @author z-latiao
 * @since: 2025/2/26 15:26
 */
public interface IBillExcelParser<T extends BaseBill<C>, C extends BaseBillDetail, R> {
    default T parse(File file) throws IOException {
        try (InputStream is1 = new BufferedInputStream(new FileInputStream(file))) {
            return parse(is1);
        }
    }

    @SuppressWarnings("unchecked")
    default T parse(InputStream is) {
        byte[] bytes;
        try {
            bytes = is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ByteArrayInputStream is1 = new ByteArrayInputStream(bytes);
        ByteArrayInputStream is2 = new ByteArrayInputStream(bytes);
        List<R> billRecords = parseRecords(is1);
        T billInfo = parseInfo(is2);
        List<C> billDetails = billInfo.getBillDetails();
        List<C> convert = convert(billRecords);
        billInfo.setBillDetails(convert);
        afterParse(billInfo);
        return billInfo;
    }

    default void afterParse(T billInfo) {
    }

    default List<R> parseRecords(File file) throws FileNotFoundException {
        return parseRecords(new BufferedInputStream(new FileInputStream(file)));
    }

    List<R> parseRecords(InputStream is);

    default T parseInfo(File file) throws FileNotFoundException {
        return parseInfo(new BufferedInputStream(new FileInputStream(file)));
    }

    T parseInfo(InputStream is);

   List<C> convert(List<R> billRecords);
}
