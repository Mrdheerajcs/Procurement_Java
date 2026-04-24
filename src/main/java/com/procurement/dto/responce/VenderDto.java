package com.procurement.dto.responce;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VenderDto {
    private Long vendorId;
    private String vendorCode;
    private String vendorName;
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
    private String drugLicenseNo;
    private LocalDate licenseValidTill;
    private String bankName;
    private String accountNo;
    private String ifscCode;
    private Long paymentTermsId;
    private String isPreferred;
    private String isBlacklisted;
    private String blacklistReason;
    private String status;
}