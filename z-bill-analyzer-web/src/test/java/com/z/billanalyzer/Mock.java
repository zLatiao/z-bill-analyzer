package com.z.billanalyzer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.z.billanalyzer.domain.bill.WxBill;
import com.z.billanalyzer.domain.bill.WxBillDetail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author zzz
 * @since 2025/3/20 21:05
 */
public class Mock {
    public static void main(String[] args) throws IOException {
        WxBill wxBill = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).readValue(new File("D:\\files\\project\\my\\z-bill-analyzer\\z-bill-analyzer-core\\src\\main\\resources\\wx-bill.json"), new TypeReference<WxBill>() {
        });
        List<WxBillDetail> billDetails = wxBill.getBillDetails();

        System.out.println(wxBill);
    }
}
