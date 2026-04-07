package com.procurement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mpr_header")
@Getter
@Setter
public class MprHeader extends BaseEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "mpr_id")
        private Long mprId;

        @Column(name = "mpr_no")//, unique = true, nullable = false
        private String mprNo;

        @Column(name = "mpr_date", nullable = false)
        private LocalDate mprDate;

        // 🔗 Foreign Key - Department
        @ManyToOne
        @JoinColumn(name = "department_id", nullable = false)
        private Department department;

        @Column(name = "project_name")
        private String projectName;

        // 🔗 Foreign Key - MPR Type
        @ManyToOne
        @JoinColumn(name = "mpr_type_id")
        private MprType mprType;

        // 🔗 Foreign Key - Tender Type
        @ManyToOne
        @JoinColumn(name = "tender_type_id")
        private TenderType tenderType;

        @Enumerated(EnumType.STRING)
        @Column(name = "priority")
        private Priority priority;

        @Column(name = "required_by_date")
        private LocalDate requiredByDate;

        @Column(name = "delivery_schedule")
        private String deliverySchedule;

        @Column(name = "duration_days")
        private Integer durationDays;

        @Column(name = "special_notes")
        private String specialNotes;

        @Column(name = "justification")
        private String justification;

        @Column(name = "status")
        private String status;


        @Column(name = "created_at")
        private LocalDateTime createdAt;

    }
