package com.procurement.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "mpr_vendor_mapping")
@Getter
@Setter

public class MprVendorMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long mprId;
    private Long mprDetailId;
    private Long vendorId;
    // getters & setters
}