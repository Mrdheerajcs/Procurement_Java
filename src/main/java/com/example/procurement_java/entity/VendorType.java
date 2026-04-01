package com.example.procurement_java.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mas_vendor_type")
@Getter
@Setter
public class VendorType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendorTypeId;

    private String vendorTypeCode;
    private String vendorTypeName;
}