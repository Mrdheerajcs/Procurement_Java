package com.procurement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contractId;

    @Column(unique = true, nullable = false)
    private String contractNo;

    private Long tenderId;
    private String tenderNo;
    private String tenderTitle;

    private Long vendorId;
    private String vendorName;

    private LocalDate awardDate;
    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal amount;

    private String status; // AWARDED, REJECTED, PENDING

    private String createdBy;
    private LocalDateTime createdAt;
}