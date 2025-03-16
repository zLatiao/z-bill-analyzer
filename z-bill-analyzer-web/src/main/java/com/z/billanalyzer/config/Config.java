package com.z.billanalyzer.config;

import com.z.billanalyzer.service.IBillService;
import com.z.billanalyzer.service.impl.DefaultBillServiceImpl;
import com.z.billanalyzer.service.impl.MockBillServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzz
 * @since 2025/3/16 20:45
 */
@Configuration
public class Config {
    @Bean
    @ConditionalOnProperty(name = "bill-service", havingValue = "default", matchIfMissing = true)
    public IBillService defaultBillService() {
        return new DefaultBillServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "bill-service", havingValue = "mock")
    public IBillService mockBillService() {
        return new MockBillServiceImpl();
    }
}
