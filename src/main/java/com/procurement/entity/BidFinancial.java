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

    // 🔥 Financial Data (ENCRYPTED in database)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedTotalBidAmount;  // AES encrypted

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

    // 🔥 BOQ Data (encrypted JSON)
    @Column(columnDefinition = "LONGTEXT")
    private String encryptedBoqData;

    // 🔥 Document paths
    private String financialDocPath;
    private String boqFilePath;

    // 🔥 Status
    private String isRevealed = "NO";  // YES/NO - whether price is revealed
    private String revealedBy;
    private LocalDateTime revealedAt;

    // 🔥 Decrypted values (transient - not stored in DB)
    @Transient
    private BigDecimal decryptedTotalBidAmount;
    @Transient
    private BigDecimal decryptedGstPercent;
    @Transient
    private BigDecimal decryptedTotalCost;
    @Transient
    private String decryptedBankName;
    @Transient
    private String decryptedAccountNumber;
    @Transient
    private String decryptedIfscCode;
    @Transient
    private String decryptedEmdNumber;
    @Transient
    private BigDecimal decryptedEmdValue;

    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}