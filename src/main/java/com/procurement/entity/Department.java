package com.procurement.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "department")
@Getter @Setter
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentId;

    @Column(nullable = false, unique = true)
    private String departmentName;

    @Column(nullable = false, unique = true)
    private String departmentCode;

    private String headOfDepartment;

    @Column(columnDefinition = "char(1)")
    private String isActive = "Y";

    private String description;
}