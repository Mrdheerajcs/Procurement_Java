package com.example.procurement_java.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mas_vendor")
@Getter
@Setter
public class Vendor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendorId;

    private String vendorCode;
    private String vendorName;

    @ManyToOne
    @JoinColumn(name = "vendor_type_id")
    private VendorType vendorType;

    private String contactPerson;
    private String mobileNo;
    private String emailId;

    private String city;
    private String state;
}