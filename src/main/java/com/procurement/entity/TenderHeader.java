package com.procurement.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "publish_tender_header")
@Data
public class TenderHeader extends BaseEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tenderId;

    private Long mprId;
    private String tenderNo;
    private String tenderTitle;
    private String tenderType;

    private String department;
    private String projectName;
    private String priority;

    private LocalDate publishDate;
    private LocalDate closingDate;
    private LocalTime bidSubmissionEndTime;

    private BigDecimal emdAmount;

    private String tenderDescription;
    private String documentPath;

    private String status;
}