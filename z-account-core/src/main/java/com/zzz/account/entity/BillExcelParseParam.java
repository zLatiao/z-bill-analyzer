package com.zzz.account.entity;


import com.zzz.account.enums.BillSourceEnum;

import java.io.InputStream;

public record BillExcelParseParam(BillSourceEnum billSourceEnum, String fileName, InputStream inputStream) {
}
