package com.z.billanalyzer.domain.parse;


import com.z.billanalyzer.enums.BillSourceEnum;

import java.io.InputStream;

public record BillExcelParseParam(BillSourceEnum billSourceEnum, String fileName, InputStream inputStream) {
}
