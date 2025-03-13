package com.z.billanalyzer;

import com.z.billanalyzer.service.IBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class ZBillWebApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ZBillWebApplication.class, args);
        IBillService bean = context.getBean(IBillService.class);
        log.info("IBillService的实现类是{}", bean.getClass());
    }

}
