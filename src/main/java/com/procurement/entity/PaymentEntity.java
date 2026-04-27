package com.procurement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private Long tenderId;
    private Long contractId;
    private Long vendorId;
    private String vendorEmail;
    private String vendorName;

    private String paymentType;
    private BigDecimal amount;
    private String currency;

    private String status;
    private String receipt;
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}