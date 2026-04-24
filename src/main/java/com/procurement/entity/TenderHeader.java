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
    private String tenderType;
    private String tenderCategory;
    private String department;
    private String projectName;
    private String priority;

    // 🔥 NEW - Bid Configuration Fields
    private String bidType;           // Single Bid / Two Bid
    private String boqType;           // Item Rate / Lump Sum
    private BigDecimal estimatedValue; // Estimated tender value
    private BigDecimal tenderFee;      // Tender fee amount
    private Integer bidValidity;       // Bid validity in days

    // Tender Timeline Fields
    private LocalDate publishDate;
    private LocalDate bidStartDate;
    private LocalDate bidEndDate;
    private LocalTime bidEndTime;
    private LocalTime bidSubmissionEndTime;
    private LocalDate bidOpeningDate;
    private LocalTime bidOpeningTime;
    private LocalDate preBidMeetingDate;

    // 🔥 NEW - Separate opening dates for two-bid system
    private LocalDate techBidOpenDate;   // Technical bid opening date
    private LocalDate finBidOpenDate;    // Financial bid opening date

    // Financial Fields
    private BigDecimal tenderFeeAmount;
    private String feeExemptionDetails;
    private BigDecimal emdAmount;
    private String emdType;
    private LocalDate emdValidityDate;

    private String tenderDescription;
    private String documentPath;

    // Status Fields
    private String tenderStatus;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String publishedBy;
    private LocalDateTime publishedAt;
    private String rejectionReason;
    private LocalDateTime rejectedAt;
    private String status;  // For backward compatibility

}