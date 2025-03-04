package com.zzz.account.parser;

import cn.hutool.core.io.IoUtil;
import com.zzz.account.util.BillConvertUtil;
import com.zzz.account.entity.BaseBillInfo;
import com.zzz.account.entity.Bill;

import java.io.*;
import java.util.List;

/**
 * @author z-latiao
 * @since: 2025/2/26 15:26
 */
public interface IBillExcelParser<T extends BaseBillInfo, R> {
    default T parse(File file) throws IOException {
        try (InputStream is1 = new BufferedInputStream(new FileInputStream(file))) {
            return parse(is1);
        }
    }

    @SuppressWarnings("unchecked")
    default T parse(InputStream is) {
        byte[] bytes = IoUtil.readBytes(is);
        ByteArrayInputStream is1 = new ByteArrayInputStream(bytes);
        ByteArrayInputStream is2 = new ByteArrayInputStream(bytes);
        T t = (T) parseInfo(is1).setBills(convert(parseRecords(is2)));
        afterParse(t);
        return t;
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

    default List<Bill> convert(List<R> billRecords) {
        return BillConvertUtil.convert(billRecords);
    }
}
