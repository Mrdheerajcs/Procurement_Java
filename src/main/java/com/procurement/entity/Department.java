package com.procurement.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "department")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", nullable = false, unique = true)
    private String departmentName;

    @Column(name = "department_code", nullable = false, unique = true)
    private String departmentCode;

    @Column(name = "head_of_department")
    private String headOfDepartment;

    @Column(name = "is_active", columnDefinition = "char(1)")
    private String isActive = "Y";

    @Column(name = "description")
    private String description;
}