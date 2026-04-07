package com.procurement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mpr_details")
@Getter
@Setter
public class MprDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mpr_detail_id")
    private Long mprDetailId;

    // 🔗 Foreign Key Mapping
    @ManyToOne
    @JoinColumn(name = "mpr_id", nullable = false)
    private MprHeader mprHeader;

    @Column(name = "sl_no")//nullable = false
    private Integer slNo;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "uom")
    private String uom;

    // ⚠️ DB me "specificationn" hai (typo), but Java me correct rakh rahe
    @Column(name = "specificationn")
    private String specification;

    @Column(name = "requested_qty", nullable = false)
    private BigDecimal requestedQty;

    @Column(name = "estimated_rate")
    private BigDecimal estimatedRate;

    @Column(name = "estimated_value")
    private BigDecimal estimatedValue;

    @Column(name = "stock_available")
    private BigDecimal stockAvailable;

    @Column(name = "avg_monthly_consumption")
    private BigDecimal avgMonthlyConsumption;

    @Column(name = "last_purchase_info")
    private String lastPurchaseInfo;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "status")
    private String status;

}