package com.procurement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mpr_approval_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MprApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne
    @JoinColumn(name = "mpr_id", nullable = false)
    private MprHeader mprHeader;

    @Column(nullable = false)
    private String approvalLevel;  // MANAGER, FINANCE, DIRECTOR

    @Column(nullable = false)
    private String action;  // SUBMITTED, APPROVED, REJECTED

    @Column(nullable = false)
    private String actionBy;

    private String remarks;

    @Column(nullable = false)
    private LocalDateTime actionAt;
}