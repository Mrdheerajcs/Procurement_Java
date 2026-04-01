package com.procurement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "mpr_header")
@Getter
@Setter
public class MprHeader extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mprId;

    private String mprNo;
    private LocalDate mprDate;

    private String projectName;

    @ManyToOne
    @JoinColumn(name = "mpr_type_id")
    private MprType mprType;

    @ManyToOne
    @JoinColumn(name = "tender_type_id")
    private TenderType tenderType;

    private String priority;
    private LocalDate requiredByDate;

    private String status;
}