package com.zzz.account;

import com.zzz.account.service.IBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class ZAccountWebApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ZAccountWebApplication.class, args);
        IBillService bean = context.getBean(IBillService.class);
        log.info("IBillService的实现类是{}", bean.getClass());
    }

}
