package com.procurement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vendor.registration.fee")
@Getter
@Setter
public class VendorFeeConfig {
    private boolean enabled;
    private double amount;
}