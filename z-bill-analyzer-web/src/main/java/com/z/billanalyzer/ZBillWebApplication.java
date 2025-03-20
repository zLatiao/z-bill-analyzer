package com.z.billanalyzer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.z.billanalyzer.domain.bill.AlipayBill;
import com.z.billanalyzer.domain.bill.BaseBill;
import com.z.billanalyzer.domain.bill.CmbBill;
import com.z.billanalyzer.domain.bill.WxBill;
import com.z.billanalyzer.service.IBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Slf4j
public class ZBillWebApplication {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(ZBillWebApplication.class, args);
        IBillService bean = context.getBean(IBillService.class);

        log.info("IBillService的实现类是{}", bean.getClass());

        mockBills(context);
    }

    private static void mockBills(ConfigurableApplicationContext context) throws IOException {
        ClassPathResource resource1 = new ClassPathResource("alipay-bill.json");
        ClassPathResource resource2 = new ClassPathResource("cmb-bill.json");
        ClassPathResource resource3 = new ClassPathResource("wx-bill.json");

        String alipayBillJson = resource1.getContentAsString(StandardCharsets.UTF_8);
        String cmbBillJson = resource2.getContentAsString(StandardCharsets.UTF_8);
        String wxBillJson = resource3.getContentAsString(StandardCharsets.UTF_8);

        List<BaseBill<?>> bills = new ArrayList<>();

        AlipayBill alipayBill = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .readValue(alipayBillJson, new TypeReference<>() {
                });
        bills.add(alipayBill);

        CmbBill cmbBill = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .readValue(cmbBillJson, new TypeReference<>() {
                });
        bills.add(cmbBill);

        WxBill wxBill = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .readValue(wxBillJson, new TypeReference<>() {
                });
        bills.add(wxBill);

        context.getBean(IBillService.class).saveBills(bills);
    }

}
