package com.procurement.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.procurement.dto.ApiResponse;
import com.procurement.dto.request.VenderRegRequest;
import com.procurement.dto.responce.AppsetupResponse;
import com.procurement.entity.Vendor;
import com.procurement.repository.VendorRepository;
import com.procurement.service.VendorRegService;

import com.procurement.util.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class VenderRegServiceImpl implements VendorRegService {
    @Autowired
    VendorRepository vendorRepository;
    private static final Logger log = LoggerFactory.getLogger(VenderRegServiceImpl.class);
    @Override
    @Transactional
    public ApiResponse<AppsetupResponse> venReg(VenderRegRequest venderRegRequestReq) {
        AppsetupResponse res = new AppsetupResponse();
        log.info("Starting lab registration process");
        Vendor vendor = new Vendor();
        vendor.setVendorName(venderRegRequestReq.getVendorName());
        vendorRepository.save(vendor);

        res.setMsg("Success");
        log.info("vender registration completed successfully");
        return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});
    }

}
