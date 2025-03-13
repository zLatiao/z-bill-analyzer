package com.z.billanalyzer.entity;


import com.z.billanalyzer.enums.BillSourceEnum;

import java.io.InputStream;

public record BillExcelParseParam(BillSourceEnum billSourceEnum, String fileName, InputStream inputStream) {
}
