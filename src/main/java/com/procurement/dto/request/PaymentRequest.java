package com.procurement.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Long tenderId;
    private Long contractId;
    private String paymentType; // TENDER_FEE, EMD, CONTRACT_PAYMENT, VENDOR_REGISTRATION
    private BigDecimal amount;
    private String email;
    private String mobile;
    private String name;
}