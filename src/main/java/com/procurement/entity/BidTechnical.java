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

    // Technical Evaluation Fields
    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String gstNumber;

    @Column(nullable = false)
    private String panNumber;

    @Column(nullable = false)
    private String makeIndiaClass;

    @Column(nullable = false)
    private BigDecimal bidderTurnover;

    @Column(nullable = false)
    private BigDecimal oemTurnover;

    @Column(nullable = false)
    private String oemName;

    @Column(nullable = false, length = 1000)
    private String authorizationDetails;

    private String submissionStatus;  // DRAFT, SUBMITTED

    private String msmeNumber;
    private Boolean isMsme = false;

    // ✅ Document paths (categorized)
    private String experienceCertPath;
    private String oemAuthPath;
    private String gstCertPath;
    private String panCardPath;
    private String msmeCertPath;
    private String otherDocsPath;

    // Evaluation fields
    private String evaluationStatus;  // PENDING, QUALIFIED, DISQUALIFIED, CLARIFICATION_NEEDED, WITHDRAWN
    private Integer evaluationScore;
    private String evaluationRemarks;
    private String evaluatedBy;
    private LocalDateTime evaluatedAt;

    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    // Clarification fields
    private String clarificationRequired;
    private String clarificationQuestion;
    private LocalDateTime clarificationDeadline;
    private String vendorResponse;
    private LocalDateTime respondedAt;
    private Integer resubmissionCount;
    private String clarificationDocumentPath;

    // Helper method
    public void setAuditFields(String username, boolean isNew) {
        if (isNew) {
            this.submittedAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
}