package com.procurement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bid_financial", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tender_id", "vendor_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidFinancial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidFinancialId;

    @ManyToOne
    @JoinColumn(name = "tender_id", nullable = false)
    private TenderHeader tender;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @OneToOne
    @JoinColumn(name = "bid_technical_id", nullable = false)
    private BidTechnical bidTechnical;

    // Encrypted Financial Data
    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedTotalBidAmount;

    @Column(columnDefinition = "TEXT")
    private String encryptedGstPercent;

    @Column(columnDefinition = "TEXT")
    private String encryptedTotalCost;

    @Column(columnDefinition = "TEXT")
    private String encryptedBankName;

    @Column(columnDefinition = "TEXT")
    private String encryptedAccountNumber;

    @Column(columnDefinition = "TEXT")
    private String encryptedIfscCode;

    @Column(columnDefinition = "TEXT")
    private String encryptedEmdNumber;

    @Column(columnDefinition = "TEXT")
    private String encryptedEmdValue;

    private String emdExemptionDetails;

    // ✅ Financial Document paths
    private String boqFilePath;
    private String priceBreakupPath;
    private String emdReceiptPath;
    private String otherFinancialDocsPath;

    // Status
    private String isRevealed = "NO";
    private String revealedBy;
    private LocalDateTime revealedAt;

    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    // Helper method
    public void setAuditFields(String username, boolean isNew) {
        if (isNew) {
            this.submittedAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
}