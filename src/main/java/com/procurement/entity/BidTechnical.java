package com.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bid_technical", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tender_id", "vendor_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidTechnical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidTechnicalId;

    @ManyToOne
    @JoinColumn(name = "tender_id", nullable = false)
    private TenderHeader tender;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    // 🔥 Technical Evaluation Fields
    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String gstNumber;

    @Column(nullable = false)
    private String panNumber;

    @Column(nullable = false)
    private String makeIndiaClass;  // Class 1, Class 2

    @Column(nullable = false)
    private BigDecimal bidderTurnover;

    @Column(nullable = false)
    private BigDecimal oemTurnover;

    @Column(nullable = false)
    private String oemName;

    @Column(nullable = false, length = 1000)
    private String authorizationDetails;

    private String msmeNumber;
    private Boolean isMsme = false;

    private String technicalDocPath;
    private String experienceDocPath;
    private String certificationDocPath;

    private String evaluationStatus;  // PENDING, QUALIFIED, DISQUALIFIED, CLARIFICATION_NEEDED
    private Integer evaluationScore;
    private String evaluationRemarks;
    private String evaluatedBy;
    private LocalDateTime evaluatedAt;

    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}