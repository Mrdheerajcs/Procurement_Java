package com.procurement.service;

import com.procurement.entity.Vendor;
import com.procurement.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    public Vendor createVendor(Vendor vendor) {

        // default values handle
        if (vendor.getIsPreferred() == null) {
            vendor.setIsPreferred("N");
        }
        if (vendor.getIsBlacklisted() == null) {
            vendor.setIsBlacklisted("N");
        }
        if (vendor.getStatus() == null) {
            vendor.setStatus("Y");
        }

        vendor.setLastUpdatedDt(LocalDateTime.now());

        return vendorRepository.save(vendor);
    }
}
