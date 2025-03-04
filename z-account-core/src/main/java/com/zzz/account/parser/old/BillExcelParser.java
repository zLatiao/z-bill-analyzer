package com.zzz.account.parser.old;

import java.io.File;
import java.util.List;

public interface BillExcelParser<T> {
    List<T> parse(File file);
}
