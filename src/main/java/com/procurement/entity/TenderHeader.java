package com.procurement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "publish_tender_header")
@Data
public class TenderHeader extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tenderId;

    private Long mprId;

    @Column(unique = true, nullable = false)
    private String tenderNo;

    private String nitNumber;
    private String tenderTitle;
    private String tenderType;  // ✅ ADD THIS - Open/Limited/Single/Global
    private String tenderCategory;
    private String department;
    private String projectName;
    private String priority;

    // Tender Timeline Fields
    private LocalDate publishDate;
    private LocalDate bidStartDate;
    private LocalDate bidEndDate;
    private LocalTime bidEndTime;
    private LocalTime bidSubmissionEndTime;  // ✅ ADD THIS
    private LocalDate bidOpeningDate;
    private LocalTime bidOpeningTime;
    private LocalDate preBidMeetingDate;

    // Financial Fields
    private BigDecimal tenderFeeAmount;
    private String feeExemptionDetails;
    private BigDecimal emdAmount;
    private String emdType;
    private LocalDate emdValidityDate;

    private String tenderDescription;
    private String documentPath;


    // Status Fields
    private String tenderStatus; // DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, PUBLISHED, CLOSED, AWARDED
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String publishedBy;
    private LocalDateTime publishedAt;
    private String rejectionReason;
    private LocalDateTime rejectedAt;
    private String status;  // ✅ ADD THIS - For backward compatibility
}