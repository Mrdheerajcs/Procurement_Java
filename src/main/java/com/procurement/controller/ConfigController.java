package com.procurement.controller;

import com.procurement.config.VendorFeeConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final VendorFeeConfig feeConfig;

    @GetMapping("/fee")
    public Map<String, Object> getFeeConfig() {
        return Map.of(
                "enabled", feeConfig.isEnabled(),
                "amount", feeConfig.getAmount()
        );
    }
}