package com.z.billanalyzer.parser.old;

import java.io.File;
import java.util.List;

public interface BillExcelParser<T> {
    List<T> parse(File file);
}
