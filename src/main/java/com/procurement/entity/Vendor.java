package com.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "mas_vendor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "vendor_code", nullable = false)
    private String vendorCode;

    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(name = "vendor_type_id", nullable = false)
    private Long vendorTypeId;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "alternate_mobile")
    private String alternateMobile;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "gst_no")
    private String gstNo;

    @Column(name = "pan_no")
    private String panNo;

    @Column(name = "drug_license_no")
    private String drugLicenseNo;

    @Column(name = "license_valid_till")
    private LocalDate licenseValidTill;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "payment_terms_id")
    private Long paymentTermsId;

    @Column(name = "is_preferred", columnDefinition = "char(1)")
    private String isPreferred = "N";

    @Column(name = "is_blacklisted", columnDefinition = "char(1)")
    private String isBlacklisted = "N";

    @Column(name = "blacklist_reason")
    private String blacklistReason;

    @Column(name = "status", columnDefinition = "char(1)")
    private String status = "Y";
}