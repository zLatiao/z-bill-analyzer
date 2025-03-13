package com.z.bill.parser.old;

import java.io.File;
import java.util.List;

public interface BillExcelParser<T> {
    List<T> parse(File file);
}
