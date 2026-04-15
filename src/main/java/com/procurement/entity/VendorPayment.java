package com.procurement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vendor_payment")
@Getter
@Setter
public class VendorPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private String email;
    private Double amount;
    private String status; // PENDING, SUCCESS
    private String transactionId;
}