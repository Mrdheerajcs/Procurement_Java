package com.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mas_mpr_type")
@Getter @Setter
public class MprType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long typeId;

    @Column(nullable = false, unique = true)
    private String typeCode;

    @Column(nullable = false)
    private String typeName;

    @Column(columnDefinition = "char(1)")
    private String status = "Y";
}