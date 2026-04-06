package com.procurement.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class VenderRegRequest {

    private String vendorCode;
    private String vendorName;
    private Long vendorTypeId;

    private String contactPerson;
    private String mobileNo;
    private String alternateMobile;
    private String emailId;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String pincode;

    private String gstNo;
    private String panNo;
    private String registrationNo;
    private LocalDate licenseValidTill;

    private String bankName;
    private String accountNo;
    private String ifscCode;
    private  String bankAddress;

    private Long paymentTermsId; //

    private String isPreferred;
    private String isBlacklisted;
    private String blacklistReason;
}
