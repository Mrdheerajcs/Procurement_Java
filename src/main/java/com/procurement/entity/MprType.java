package com.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mas_mpr_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MprType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "type_code", nullable = false, unique = true)
    private String typeCode;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Column(name = "status", columnDefinition = "char(1)")
    private String status = "Y";
}