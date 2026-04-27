package com.procurement.dto.responce;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String razorpayOrderId;
    private String razorpayKeyId;
    private Long amount;
    private String currency;
    private String receipt;
    private Integer status;
}