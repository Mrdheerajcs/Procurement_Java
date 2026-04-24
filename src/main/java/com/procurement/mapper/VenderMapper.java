package com.procurement.mapper;

import com.procurement.dto.request.VenderRegRequest;
import com.procurement.dto.responce.VenderDto;
import com.procurement.entity.Vendor;
import org.springframework.stereotype.Component;

@Component
public class VenderMapper {

    public VenderDto toDto(Vendor vendor) {
        if (vendor == null) return null;

        VenderDto dto = new VenderDto();
        dto.setVendorId(vendor.getVendorId());
        dto.setVendorCode(vendor.getVendorCode());
        dto.setVendorName(vendor.getVendorName());
        dto.setContactPerson(vendor.getContactPerson());
        dto.setMobileNo(vendor.getMobileNo());
        dto.setAlternateMobile(vendor.getAlternateMobile());
        dto.setEmailId(vendor.getEmailId());
        dto.setAddressLine1(vendor.getAddressLine1());
        dto.setAddressLine2(vendor.getAddressLine2());
        dto.setCity(vendor.getCity());
        dto.setState(vendor.getState());
        dto.setCountry(vendor.getCountry());
        dto.setPincode(vendor.getPincode());
        dto.setGstNo(vendor.getGstNo());
        dto.setPanNo(vendor.getPanNo());
        dto.setDrugLicenseNo(vendor.getDrugLicenseNo());
        dto.setLicenseValidTill(vendor.getLicenseValidTill());
        dto.setBankName(vendor.getBankName());
        dto.setAccountNo(vendor.getAccountNo());
        dto.setIfscCode(vendor.getIfscCode());
        dto.setPaymentTermsId(vendor.getPaymentTermsId());
        dto.setIsPreferred(vendor.getIsPreferred());
        dto.setIsBlacklisted(vendor.getIsBlacklisted());
        dto.setBlacklistReason(vendor.getBlacklistReason());
        dto.setStatus(vendor.getStatus());

        return dto;
    }

    public Vendor toEntity(VenderRegRequest request) {
        if (request == null) return null;

        Vendor vendor = new Vendor();
        vendor.setVendorName(request.getVendorName());
        vendor.setContactPerson(request.getContactPerson());
        vendor.setMobileNo(request.getMobileNo());
        vendor.setAlternateMobile(request.getAlternateMobile());
        vendor.setEmailId(request.getEmailId());
        vendor.setAddressLine1(request.getAddressLine1());
        vendor.setAddressLine2(request.getAddressLine2());
        vendor.setCity(request.getCity());
        vendor.setState(request.getState());
        vendor.setCountry(request.getCountry());
        vendor.setPincode(request.getPincode());
        vendor.setGstNo(request.getGstNo());
        vendor.setPanNo(request.getPanNo());
        vendor.setDrugLicenseNo(request.getRegistrationNo());
        vendor.setLicenseValidTill(request.getLicenseValidTill());
        vendor.setBankName(request.getBankName());
        vendor.setAccountNo(request.getAccountNo());
        vendor.setIfscCode(request.getIfscCode());
        vendor.setPaymentTermsId(request.getPaymentTermsId());
        vendor.setIsPreferred(request.getIsPreferred());
        vendor.setIsBlacklisted(request.getIsBlacklisted());
        vendor.setBlacklistReason(request.getBlacklistReason());
        vendor.setVendorTypeId(request.getVendorTypeId());
        vendor.setStatus("Y");

        return vendor;
    }
}