package com.soukscan.admin.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    public MetricsConfig(MeterRegistry registry) {
        registry.counter("admin_service_startup_total").increment();
    }
}
