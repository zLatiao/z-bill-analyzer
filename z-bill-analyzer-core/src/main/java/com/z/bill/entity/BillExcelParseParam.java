package com.z.bill.entity;


import com.z.bill.enums.BillSourceEnum;

import java.io.InputStream;

public record BillExcelParseParam(BillSourceEnum billSourceEnum, String fileName, InputStream inputStream) {
}
